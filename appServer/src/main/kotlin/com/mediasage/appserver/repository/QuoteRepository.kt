package com.mediasage.appserver.repository

import com.mediasage.appserver.db.FigureTable
import com.mediasage.appserver.db.QuoteTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The whole quote library in one response. Always a full snapshot, never a "changed since" delta:
 * a phone drops any saved quote that isn't in it, which is how removed and unverified quotes reach it.
 */
@Serializable
data class QuotesResponse(val quotes: List<QuoteDto>)

@Serializable
data class QuoteDto(
    val figureId: Long,
    val text: String,
    val source: String,
    val themes: String
)

class QuoteRepository {

    /** Verified quotes of enabled figures, the same rule headline matching uses. */
    suspend fun getLibrary(): List<QuoteDto> = withContext(Dispatchers.IO) {
        transaction {
            QuoteTable.join(FigureTable, JoinType.INNER, onColumn = QuoteTable.figureId, otherColumn = FigureTable.id)
                .selectAll()
                .where { (QuoteTable.verified eq true) and (FigureTable.isEnabled eq true) }
                .orderBy(QuoteTable.id)
                .map { row ->
                    QuoteDto(
                        figureId = row[QuoteTable.figureId],
                        text = row[QuoteTable.text],
                        source = row[QuoteTable.sourceText],
                        themes = row[QuoteTable.themes]
                    )
                }
        }
    }
}
