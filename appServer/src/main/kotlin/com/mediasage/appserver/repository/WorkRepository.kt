package com.mediasage.appserver.repository

import com.mediasage.appserver.db.FigureTable
import com.mediasage.appserver.db.WorkTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class WorkRepository {
    /** The works a briefing may draw on: the figure's bibliography minus records kept only for quotes to cite. */
    suspend fun getByFigureId(figureId: Long): List<WorkData> = withContext(Dispatchers.IO) {
        transaction {
            WorkTable.selectAll()
                .where { (WorkTable.figureId eq figureId) and (WorkTable.forQuotesOnly eq false) }
                .orderBy(WorkTable.id to SortOrder.ASC)
                .map {
                    WorkData(
                        id = it[WorkTable.id],
                        title = it[WorkTable.title],
                        year = it[WorkTable.year],
                        recordedBy = it[WorkTable.recordedBy],
                        isLifeOfAnother = it[WorkTable.isLifeOfAnother]
                    )
                }
        }
    }

    /**
     * Every work of every enabled figure, for the app's Library. Unlike [getByFigureId] it keeps the
     * quotes-only records: they are real books that preserve a figure's words, so readers can browse them.
     */
    suspend fun getAllForEnabledFigures(): List<WorkDto> = withContext(Dispatchers.IO) {
        transaction {
            WorkTable.join(FigureTable, JoinType.INNER, onColumn = WorkTable.figureId, otherColumn = FigureTable.id)
                .selectAll()
                .where { FigureTable.isEnabled eq true }
                .orderBy(WorkTable.id to SortOrder.ASC)
                .map {
                    WorkDto(
                        id = it[WorkTable.id],
                        figureId = it[WorkTable.figureId],
                        title = it[WorkTable.title],
                        year = it[WorkTable.year],
                        recordedBy = it[WorkTable.recordedBy],
                        coverUrl = it[WorkTable.coverUrl]
                    )
                }
        }
    }
}

@Serializable
data class WorksResponse(val works: List<WorkDto>)

@Serializable
data class WorkDto(
    val id: Long,
    val figureId: Long,
    val title: String,
    val year: Int?,
    val recordedBy: String?,
    val coverUrl: String?
)

data class WorkData(
    val id: Long,
    val title: String,
    val year: Int?,
    val recordedBy: String? = null,
    val isLifeOfAnother: Boolean = false,
) {
    /** A book someone else wrote that preserves the figure's words, rather than the figure's own work. */
    val isRecorded: Boolean get() = recordedBy != null

    /**
     * How the work is cited, without the year: the title, or for a recorded work
     * "words recorded by Sarah Bradford in Scenes in the Life of Harriet Tubman", so it reads naturally after "Based on".
     */
    val citation: String get() = recordedBy?.let { "words recorded by $it in $title" } ?: title

    /** "The Pursuit of God (1948)", or the bare citation when the year isn't known exactly. */
    val displayTitle: String get() = if (year != null) "$citation ($year)" else citation
}
