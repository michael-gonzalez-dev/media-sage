@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.bookmarks

import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.domain.model.Encouragement
import com.mediasage.domain.repository.EncouragementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarksViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleBookmarkLogsRemoveActionSinceThisScreenOnlyListsBookmarkedItems() = runTest(testDispatcher) {
        val encouragement = buildEncouragement(articleUrl = "https://example.com/article")
        val analyticsService = FakeAnalyticsServiceForBookmarksScreen()
        val vm = BookmarksViewModel(FakeEncouragementRepository(listOf(encouragement)), analyticsService)

        vm.onIntent(BookmarksContract.Intent.ToggleBookmark("https://example.com/article"))

        assertEquals(
            listOf(
                AnalyticsEvents.BOOKMARK_TOGGLED to mapOf(
                    AnalyticsEvents.Params.ACTION to AnalyticsEvents.Values.ACTION_REMOVE,
                    AnalyticsEvents.Params.SCREEN to AnalyticsEvents.Values.SCREEN_BOOKMARKS,
                ),
            ),
            analyticsService.loggedEvents,
        )
    }
}

private fun buildEncouragement(
    articleUrl: String = "https://example.com",
    headlineTitle: String = "Test Headline",
    figureName: String = "Augustine",
    figureRole: String = "Bishop",
    quoteText: String = "Test quote",
) = Encouragement(
    articleUrl = articleUrl,
    summary = null,
    quoteText = quoteText,
    figureName = figureName,
    figureRole = figureRole,
    scriptureReference = "John 3:16",
    scriptureText = "For God so loved the world",
    explanation = "Explanation",
    connectionThemes = listOf("faith"),
    matchTheme = "hope",
    tone = "gentle",
    figureImageUrl = null,
    headlineTitle = headlineTitle,
    headlineImageUrl = null,
    bookmarked = true,
)

private class FakeAnalyticsServiceForBookmarksScreen : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
    override fun logScreenView(screenName: String) = Unit
}

private class FakeEncouragementRepository(
    private val encouragements: List<Encouragement> = emptyList(),
) : EncouragementRepository {
    override fun observeAll(): Flow<List<Encouragement>> = MutableStateFlow(encouragements)
    override fun observeBookmarked(): Flow<List<Encouragement>> = MutableStateFlow(encouragements.filter { it.bookmarked })
    override fun observeCountByFigureName(): Flow<Map<String, Int>> = MutableStateFlow(emptyMap())
    override fun observeByFigureId(figureId: Long): Flow<List<Encouragement>> = MutableStateFlow(emptyList())
    override fun observeIsBookmarked(articleUrl: String): Flow<Boolean> = MutableStateFlow(false)
    override fun observeByEpochDay(epochDay: Long): Flow<List<Encouragement>> = MutableStateFlow(emptyList())
    override fun observeActiveEpochDays(): Flow<Set<Long>> = MutableStateFlow(emptySet())
    override suspend fun toggleBookmark(articleUrl: String) = Unit
    override suspend fun getEncouragement(
        headlineTitle: String,
        headlineSource: String,
        headlineImageUrl: String?,
        articleUrl: String?,
        articleSnippet: String?,
        headlineCategory: String,
        headlinePublishedAt: Long,
    ): Encouragement = error("not used in test")
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}
