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
                        year = it[WorkTable.year]
                    )
                }
        }
    }
}

data class WorkData(val id: Long, val title: String, val year: Int?) {
    /** "The Pursuit of God (1948)", or the bare title when the year isn't known exactly. */
    val displayTitle: String get() = if (year != null) "$title ($year)" else title
}
