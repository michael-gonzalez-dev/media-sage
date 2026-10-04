package com.mediasage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mediasage.data.local.entity.ParkedDailyReflectionEntity

@Dao
interface ParkedDailyReflectionDao {
    @Query("SELECT * FROM parked_daily_reflection WHERE userId = :userId")
    suspend fun getForUser(userId: String): List<ParkedDailyReflectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ParkedDailyReflectionEntity>)

    @Query("DELETE FROM parked_daily_reflection WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)
}
