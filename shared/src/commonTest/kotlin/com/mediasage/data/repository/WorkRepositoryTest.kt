package com.mediasage.data.repository

import com.mediasage.data.local.dao.WorkDao
import com.mediasage.data.local.entity.WorkEntity
import com.mediasage.data.remote.AssignmentDefaultDto
import com.mediasage.data.remote.DailyReflectionRequestDto
import com.mediasage.data.remote.DailyReflectionResponseDto
import com.mediasage.data.remote.EncourageRequestDto
import com.mediasage.data.remote.EncourageResultDto
import com.mediasage.data.remote.FiguresResponse
import com.mediasage.data.remote.MatchRequestDto
import com.mediasage.data.remote.MatchResultDto
import com.mediasage.data.remote.MediaSageApi
import com.mediasage.data.remote.NewsArticleDto
import com.mediasage.data.remote.QuotesResponse
import com.mediasage.data.remote.ScripturePassageDto
import com.mediasage.data.remote.ScriptureVerseDto
import com.mediasage.data.remote.WorkDto
import com.mediasage.data.remote.WorksResponse
import com.mediasage.domain.model.Work
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkRepositoryTest {

    private val pursuitDto = WorkDto(id = 10, figureId = 1, title = "The Pursuit of God", year = 1948)
    private val recordedDto = WorkDto(
        id = 11,
        figureId = 2,
        title = "Scenes in the Life of Harriet Tubman",
        year = 1869,
        recordedBy = "Sarah Bradford",
        coverUrl = "https://example.com/cover.jpg"
    )

    @Test
    fun syncStoresEveryWorkTheServerSends() = runTest {
        val dao = FakeWorkDaoForLibrarySync()
        val api = FakeMediaSageApiForWorkSync(WorksResponse(listOf(pursuitDto, recordedDto)))

        WorkRepositoryImpl(dao, api).syncWorks()

        assertEquals(listOf(10L, 11L), dao.stored.value.map { it.id })
    }

    @Test
    fun syncDropsWorksNoLongerOnTheServer() = runTest {
        val stale = WorkEntity(id = 99, figureId = 1, title = "Removed Work")
        val dao = FakeWorkDaoForLibrarySync(listOf(stale))
        val api = FakeMediaSageApiForWorkSync(WorksResponse(listOf(pursuitDto)))

        WorkRepositoryImpl(dao, api).syncWorks()

        assertEquals(listOf(10L), dao.stored.value.map { it.id })
    }

    @Test
    fun syncedWorksKeepTheirRecorderAndCoverUrl() = runTest {
        val dao = FakeWorkDaoForLibrarySync()
        val repository = WorkRepositoryImpl(dao, FakeMediaSageApiForWorkSync(WorksResponse(listOf(pursuitDto, recordedDto))))

        repository.syncWorks()

        val expected = listOf(
            Work(id = 10, figureId = 1, title = "The Pursuit of God", year = 1948),
            Work(
                id = 11,
                figureId = 2,
                title = "Scenes in the Life of Harriet Tubman",
                year = 1869,
                recordedBy = "Sarah Bradford",
                coverUrl = "https://example.com/cover.jpg"
            ),
        )
        assertEquals(expected, repository.observeAllWorks().first())
    }
}

private class FakeWorkDaoForLibrarySync(works: List<WorkEntity> = emptyList()) : WorkDao {
    val stored = MutableStateFlow(works)

    override fun observeAll(): Flow<List<WorkEntity>> = stored

    override suspend fun insertAll(works: List<WorkEntity>) {
        stored.value = stored.value + works
    }

    override suspend fun deleteAll() {
        stored.value = emptyList()
    }
}

private class FakeMediaSageApiForWorkSync(private val response: WorksResponse) : MediaSageApi {
    override suspend fun getWorks(): WorksResponse = response

    override suspend fun getFigures(since: Long?): FiguresResponse = error("not used in this test")
    override suspend fun getQuotes(): QuotesResponse = error("not used in this test")
    override suspend fun getHeadlines(locale: String, limit: Int): List<NewsArticleDto> = error("not used in this test")
    override suspend fun searchNews(query: String, limit: Int): List<NewsArticleDto> = error("not used in this test")
    override suspend fun encourage(request: EncourageRequestDto): EncourageResultDto = error("not used in this test")
    override suspend fun matchQuote(request: MatchRequestDto): MatchResultDto = error("not used in this test")
    override suspend fun searchScripture(query: String, limit: Int): List<ScriptureVerseDto> =
        error("not used in this test")
    override suspend fun getPassage(passageId: String): ScripturePassageDto = error("not used in this test")
    override suspend fun getDailyReflection(request: DailyReflectionRequestDto): DailyReflectionResponseDto =
        error("not used in this test")
    override suspend fun getAssignmentDefaults(): List<AssignmentDefaultDto> = error("not used in this test")
}
