package com.mediasage.appserver.repository

import com.mediasage.appserver.db.WorkTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class WorkRepository {
    suspend fun getByFigureId(figureId: Long): List<WorkData> = withContext(Dispatchers.IO) {
        transaction {
            WorkTable.selectAll()
                .where { WorkTable.figureId eq figureId }
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
}

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
