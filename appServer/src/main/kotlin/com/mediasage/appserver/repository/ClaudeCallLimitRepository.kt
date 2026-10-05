package com.mediasage.appserver.repository

import com.mediasage.appserver.db.ClaudeCallLimitTable
import com.mediasage.appserver.db.DailyCallLimitTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ClaudeCallLimitRepository(private val table: DailyCallLimitTable = ClaudeCallLimitTable) {

    /**
     * Atomically checks and increments today's Claude call count. Returns false once [dailyLimit] is already
     * reached. The increment is a single conditional `UPDATE ... WHERE call_count < dailyLimit` so the
     * check-and-increment can't race across concurrent requests regardless of transaction isolation level.
     */
    suspend fun tryConsumeCall(callDate: String, dailyLimit: Int): Boolean = withContext(Dispatchers.IO) {
        transaction {
            table.insertIgnore {
                it[table.callDate] = callDate
                it[table.callCount] = 0
            }

            val updatedRows = table.update({
                (table.callDate eq callDate) and (table.callCount less dailyLimit)
            }) {
                it.update(table.callCount, table.callCount + 1)
            }
            updatedRows > 0
        }
    }
}
