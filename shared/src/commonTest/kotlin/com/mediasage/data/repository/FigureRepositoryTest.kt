package com.mediasage.data.repository

import com.mediasage.data.local.dao.FigureDao
import com.mediasage.data.local.dao.SyncMetaDao
import com.mediasage.data.local.entity.FigureEntity
import com.mediasage.data.local.entity.SyncMetaEntity
import com.mediasage.data.remote.AssignmentDefaultDto
import com.mediasage.data.remote.DailyReflectionRequestDto
import com.mediasage.data.remote.DailyReflectionResponseDto
import com.mediasage.data.remote.EncourageRequestDto
import com.mediasage.data.remote.EncourageResultDto
import com.mediasage.data.remote.FigureDto
import com.mediasage.data.remote.FiguresResponse
import com.mediasage.data.remote.MatchRequestDto
import com.mediasage.data.remote.MatchResultDto
import com.mediasage.data.remote.MediaSageApi
import com.mediasage.data.remote.NewsArticleDto
import com.mediasage.data.remote.QuotesResponse
import com.mediasage.data.remote.ScripturePassageDto
import com.mediasage.data.remote.ScriptureVerseDto
import com.mediasage.data.remote.WorksResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FigureRepositoryTest {

    private val luther = FigureEntity(id = 1, name = "Martin Luther", category = "reformer", century = "16th", bio = "Old bio", serverId = 1)
    private val calvin = FigureEntity(id = 2, name = "John Calvin", category = "reformer", century = "16th", serverId = 2)

    private val lutherDto = FigureDto(id = 1, name = "Martin Luther", category = "reformer", century = "16th", bio = "Old bio")
    private val calvinDto = FigureDto(id = 2, name = "John Calvin", category = "reformer", century = "16th")

    private fun recentSync() = SyncMetaEntity(lastFigureSyncAt = currentTimeMillis() - ONE_HOUR_MS)

    @Test
    fun deltaSyncRemovesAFigureDisabledOnTheServer() = runTest {
        val dao = FakeFigureDaoForFigureSync(listOf(luther, calvin))
        val api = FakeMediaSageApiForFigureSync(FiguresResponse(syncedAt = 5L, figures = emptyList(), disabledIds = listOf(2L)))

        FigureRepositoryImpl(dao, FakeSyncMetaDaoForFigureSync(recentSync()), api).syncFigures()

        assertEquals(listOf(1L), dao.storedIds())
    }

    @Test
    fun deltaSyncAddsBackAFigureReEnabledOnTheServer() = runTest {
        val dao = FakeFigureDaoForFigureSync(listOf(luther))
        val api = FakeMediaSageApiForFigureSync(FiguresResponse(syncedAt = 5L, figures = listOf(calvinDto)))

        FigureRepositoryImpl(dao, FakeSyncMetaDaoForFigureSync(recentSync()), api).syncFigures()

        assertEquals(listOf(1L, 2L), dao.storedIds())
    }

    @Test
    fun deltaSyncUpdatesAnEditedFigureWithoutDeletingIt() = runTest {
        val dao = FakeFigureDaoForFigureSync(listOf(luther, calvin))
        val edited = lutherDto.copy(bio = "New bio")
        val api = FakeMediaSageApiForFigureSync(FiguresResponse(syncedAt = 5L, figures = listOf(edited)))

        FigureRepositoryImpl(dao, FakeSyncMetaDaoForFigureSync(recentSync()), api).syncFigures()

        assertEquals("New bio", dao.getById(1)?.bio)
        assertEquals(emptyList(), dao.deletedIds)
    }

    @Test
    fun fullSyncDeletesOnlyFiguresNoLongerEnabled() = runTest {
        val dao = FakeFigureDaoForFigureSync(listOf(luther, calvin))
        val api = FakeMediaSageApiForFigureSync(FiguresResponse(syncedAt = 5L, figures = listOf(lutherDto)))

        FigureRepositoryImpl(dao, FakeSyncMetaDaoForFigureSync(), api).syncFigures()

        assertNull(api.requestedSince)
        assertEquals(listOf(2L), dao.deletedIds)
        assertEquals(0, dao.deleteAllCalls)
        assertEquals(listOf(1L), dao.storedIds())
    }

    @Test
    fun syncRecordsTheServerSyncTime() = runTest {
        val syncMetaDao = FakeSyncMetaDaoForFigureSync(recentSync())
        val api = FakeMediaSageApiForFigureSync(FiguresResponse(syncedAt = 42L, figures = emptyList()))

        FigureRepositoryImpl(FakeFigureDaoForFigureSync(), syncMetaDao, api).syncFigures()

        assertEquals(42L, syncMetaDao.get()?.lastFigureSyncAt)
    }

    private companion object {
        const val ONE_HOUR_MS = 60 * 60 * 1000L
    }
}

private class FakeFigureDaoForFigureSync(figures: List<FigureEntity> = emptyList()) : FigureDao {
    private val store = figures.associateBy { it.id }.toMutableMap()
    val deletedIds = mutableListOf<Long>()
    var deleteAllCalls = 0

    fun storedIds(): List<Long> = store.keys.sorted()

    override suspend fun insert(figure: FigureEntity): Long {
        store[figure.id] = figure
        return figure.id
    }

    override suspend fun upsertAll(figures: List<FigureEntity>) {
        figures.forEach { store[it.id] = it }
    }

    override fun observeAll(): Flow<List<FigureEntity>> = flowOf(store.values.toList())

    override suspend fun getById(id: Long): FigureEntity? = store[id]

    override suspend fun getByServerId(serverId: Long): FigureEntity? =
        store.values.find { it.serverId == serverId }

    override fun observeByCategory(category: String): Flow<List<FigureEntity>> =
        flowOf(store.values.filter { it.category == category })

    override suspend fun getByName(name: String): FigureEntity? =
        store.values.find { it.name == name }

    override suspend fun getByNameIgnoreCase(name: String): FigureEntity? =
        store.values.find { it.name.lowercase() == name.lowercase() }

    override suspend fun deleteById(id: Long) { store.remove(id) }

    override suspend fun deleteByIds(ids: List<Long>) {
        deletedIds.addAll(ids)
        ids.forEach { store.remove(it) }
    }

    override suspend fun deleteAll() {
        deleteAllCalls++
        store.clear()
    }
}

private class FakeSyncMetaDaoForFigureSync(private var meta: SyncMetaEntity? = null) : SyncMetaDao {
    override suspend fun get(): SyncMetaEntity? = meta
    override suspend fun upsert(meta: SyncMetaEntity) { this.meta = meta }
}

private class FakeMediaSageApiForFigureSync(private val response: FiguresResponse) : MediaSageApi {
    var requestedSince: Long? = null

    override suspend fun getFigures(since: Long?): FiguresResponse {
        requestedSince = since
        return response
    }
    override suspend fun getQuotes(): QuotesResponse = QuotesResponse(quotes = emptyList())

    override suspend fun getHeadlines(locale: String, limit: Int): List<NewsArticleDto> = error("not used in this test")
    override suspend fun encourage(request: EncourageRequestDto): EncourageResultDto = error("not used in this test")
    override suspend fun matchQuote(request: MatchRequestDto): MatchResultDto = error("not used in this test")
    override suspend fun searchScripture(query: String, limit: Int): List<ScriptureVerseDto> =
        error("not used in this test")
    override suspend fun getPassage(passageId: String): ScripturePassageDto = error("not used in this test")
    override suspend fun getDailyReflection(request: DailyReflectionRequestDto): DailyReflectionResponseDto =
        error("not used in this test")
    override suspend fun getWorks(): WorksResponse = error("not used in this test")
    override suspend fun getAssignmentDefaults(): List<AssignmentDefaultDto> = error("not used in this test")
}
