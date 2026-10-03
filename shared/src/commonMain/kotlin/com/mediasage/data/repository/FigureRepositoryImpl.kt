package com.mediasage.data.repository

import com.mediasage.data.local.dao.FigureDao
import com.mediasage.data.local.dao.SyncMetaDao
import com.mediasage.data.local.entity.SyncMetaEntity
import com.mediasage.data.mapper.toDomain
import com.mediasage.data.mapper.toEntity
import com.mediasage.data.remote.MediaSageApi
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.repository.FigureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FigureRepositoryImpl(
    private val figureDao: FigureDao,
    private val syncMetaDao: SyncMetaDao,
    private val api: MediaSageApi
) : FigureRepository {

    override fun observeAllFigures(): Flow<List<Figure>> =
        figureDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> =
        figureDao.observeByCategory(category.name.lowercase()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getFigureById(id: Long): Figure? =
        figureDao.getById(id)?.toDomain()

    override suspend fun getFigureByName(name: String): Figure? =
        figureDao.getByName(name)?.toDomain()

    override suspend fun syncFigures() {
        val lastSyncAt = syncMetaDao.get()?.lastFigureSyncAt
        val isFullSync = lastSyncAt == null || currentTimeMillis() - lastSyncAt > FULL_SYNC_INTERVAL_MS
        val response = api.getFigures(since = if (isFullSync) null else lastSyncAt)
        val figures = response.figures.map { it.toEntity() }
        // Never delete a figure that's still enabled: deleting cascades to its saved quotes.
        // Removing a disabled figure is meant to drop them, including a memorized quote.
        val removedIds = if (isFullSync) {
            val enabledIds = figures.map { it.id }.toSet()
            figureDao.observeAll().first().map { it.id }.filterNot { it in enabledIds }
        } else {
            response.disabledIds
        }
        if (removedIds.isNotEmpty()) figureDao.deleteByIds(removedIds)
        if (figures.isNotEmpty()) figureDao.upsertAll(figures)
        syncMetaDao.upsert(SyncMetaEntity(lastFigureSyncAt = response.syncedAt))
    }

    companion object {
        private const val FULL_SYNC_INTERVAL_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
