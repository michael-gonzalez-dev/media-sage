package com.mediasage.data.repository

import com.mediasage.data.local.dao.WorkDao
import com.mediasage.data.mapper.toDomain
import com.mediasage.data.mapper.toEntity
import com.mediasage.data.remote.MediaSageApi
import com.mediasage.domain.model.Work
import com.mediasage.domain.repository.WorkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkRepositoryImpl(
    private val workDao: WorkDao,
    private val api: MediaSageApi
) : WorkRepository {

    override fun observeAllWorks(): Flow<List<Work>> =
        workDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    // The server always sends the whole bibliography, so the local copy is replaced rather than merged.
    override suspend fun syncWorks() {
        workDao.replaceAll(api.getWorks().works.map { it.toEntity() })
    }
}
