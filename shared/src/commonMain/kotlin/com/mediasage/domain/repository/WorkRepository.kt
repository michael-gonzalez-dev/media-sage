package com.mediasage.domain.repository

import com.mediasage.domain.model.Work
import kotlinx.coroutines.flow.Flow

interface WorkRepository {
    fun observeAllWorks(): Flow<List<Work>>
    suspend fun syncWorks()
}
