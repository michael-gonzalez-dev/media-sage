package com.mediasage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mediasage.data.local.entity.WorkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDao {
    @Query("SELECT * FROM works ORDER BY id ASC")
    fun observeAll(): Flow<List<WorkEntity>>

    @Insert
    suspend fun insertAll(works: List<WorkEntity>)

    @Query("DELETE FROM works")
    suspend fun deleteAll()

    /** Swaps in the server's full list in one transaction, so the Library never sees it half-written. */
    @Transaction
    suspend fun replaceAll(works: List<WorkEntity>) {
        deleteAll()
        insertAll(works)
    }
}
