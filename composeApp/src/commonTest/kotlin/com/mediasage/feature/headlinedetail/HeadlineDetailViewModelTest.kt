@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.headlinedetail

import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.domain.model.Encouragement
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.model.Headline
import com.mediasage.domain.repository.EncouragementRepository
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.HeadlineRepository
import com.mediasage.domain.repository.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HeadlineDetailViewModelTest {

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
    fun emitsSuccessAfterSuccessfulMatch() = runTest(testDispatcher) {
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            figure = buildFigure(name = "Augustine"),
        )

        assertIs<HeadlineDetailContract.UiState.Success>(vm.state.value)
    }

    @Test
    fun marksHeadlineAsReadWhenDetailIsOpened() = runTest(testDispatcher) {
        val headlineRepo = FakeHeadlineRepository(buildHeadline())
        HeadlineDetailViewModel(
            articleUrl = "https://example.com/article",
            headlineRepository = headlineRepo,
            encouragementRepository = FakeEncouragementRepository(buildEncouragement()),
            figureRepository = FakeFigureRepository(null),
            quoteRepository = FakeQuoteRepository(),
            analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen(),
        )

        assertEquals(listOf("https://example.com/article"), headlineRepo.markReadCalls)
    }

    @Test
    fun savesQuoteWithCorrectParametersWhenFigureIsFound() = runTest(testDispatcher) {
        val figure = buildFigure(id = 42L, name = "Augustine")
        val encouragement = buildEncouragement(
            figureName = "Augustine",
            quoteText = "Our heart is restless",
            scriptureReference = "Confessions 1.1",
            connectionThemes = listOf("rest", "longing"),
        )
        val quoteRepo = FakeQuoteRepository()

        buildViewModel(
            headline = buildHeadline(),
            encouragement = encouragement,
            figure = figure,
            quoteRepository = quoteRepo,
        )

        assertEquals(1, quoteRepo.savedQuotes.size)
        val saved = quoteRepo.savedQuotes.first()
        assertEquals("Our heart is restless", saved.text)
        assertEquals("Confessions 1.1", saved.source)
        assertEquals(listOf("rest", "longing"), saved.themes)
        assertEquals(42L, saved.figureId)
    }

    @Test
    fun doesNotSaveQuoteWhenFigureIsNotFound() = runTest(testDispatcher) {
        val quoteRepo = FakeQuoteRepository()

        buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Unknown Figure"),
            figure = null,
            quoteRepository = quoteRepo,
        )

        assertEquals(0, quoteRepo.savedQuotes.size)
    }

    @Test
    fun stateRemainsSuccessWhenQuoteSaveFails() = runTest(testDispatcher) {
        val quoteRepo = FakeQuoteRepository(throwOnSave = true)

        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            figure = buildFigure(name = "Augustine"),
            quoteRepository = quoteRepo,
        )

        assertIs<HeadlineDetailContract.UiState.Success>(vm.state.value)
    }

    @Test
    fun emitsErrorWhenEncouragementFetchFails() = runTest(testDispatcher) {
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = null,
        )

        assertIs<HeadlineDetailContract.UiState.Error>(vm.state.value)
    }

    @Test
    fun showFigureProfileRevealsSheetWithBioWhenFigureIsFound() = runTest(testDispatcher) {
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            figure = buildFigure(name = "Augustine", bio = "Bishop of Hippo, author of Confessions."),
        )

        vm.onIntent(HeadlineDetailContract.Intent.ShowFigureProfile)

        val profile = (vm.state.value as HeadlineDetailContract.UiState.Success).figureProfile
        val visible = assertIs<HeadlineDetailContract.FigureProfileState.Visible>(profile)
        assertEquals("Augustine", visible.figureName)
        assertEquals("Bishop of Hippo, author of Confessions.", visible.bio)
    }

    @Test
    fun showFigureProfileFallsBackToEncouragementFieldsWhenFigureIsNotFound() = runTest(testDispatcher) {
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Unknown Figure"),
            figure = null,
        )

        vm.onIntent(HeadlineDetailContract.Intent.ShowFigureProfile)

        val profile = (vm.state.value as HeadlineDetailContract.UiState.Success).figureProfile
        val visible = assertIs<HeadlineDetailContract.FigureProfileState.Visible>(profile)
        assertEquals("Unknown Figure", visible.figureName)
        assertEquals("Bishop of Hippo", visible.figureRole)
        assertEquals(null, visible.bio)
    }

    @Test
    fun dismissFigureProfileHidesSheet() = runTest(testDispatcher) {
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            figure = buildFigure(name = "Augustine", bio = "Bishop of Hippo."),
        )

        vm.onIntent(HeadlineDetailContract.Intent.ShowFigureProfile)
        vm.onIntent(HeadlineDetailContract.Intent.DismissFigureProfile)

        val profile = (vm.state.value as HeadlineDetailContract.UiState.Success).figureProfile
        assertIs<HeadlineDetailContract.FigureProfileState.Hidden>(profile)
    }

    @Test
    fun retryMatchLogsContentRetryEvent() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            figure = buildFigure(name = "Augustine"),
            analyticsService = analyticsService,
        )

        vm.onIntent(HeadlineDetailContract.Intent.RetryMatch)

        assertEquals(
            listOf(mapOf(AnalyticsEvents.Params.SURFACE to AnalyticsEvents.Values.SURFACE_HEADLINE_MATCH)),
            analyticsService.paramsOf(AnalyticsEvents.CONTENT_RETRY),
        )
    }

    @Test
    fun toggleBookmarkLogsAddActionWhenNotYetBookmarked() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            figure = buildFigure(name = "Augustine"),
            analyticsService = analyticsService,
        )

        vm.onIntent(HeadlineDetailContract.Intent.ToggleBookmark)

        assertEquals(
            listOf(
                expectedHeadlineParams + mapOf(
                    AnalyticsEvents.Params.ACTION to AnalyticsEvents.Values.ACTION_ADD,
                    AnalyticsEvents.Params.SCREEN to AnalyticsEvents.Values.SCREEN_HEADLINE_DETAIL,
                ),
            ),
            analyticsService.paramsOf(AnalyticsEvents.BOOKMARK_TOGGLED),
        )
    }

    @Test
    fun toggleBookmarkLogsRemoveActionWhenAlreadyBookmarked() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        val vm = buildViewModel(
            headline = buildHeadline(),
            encouragement = buildEncouragement(figureName = "Augustine"),
            analyticsService = analyticsService,
            isBookmarked = true,
        )

        vm.onIntent(HeadlineDetailContract.Intent.ToggleBookmark)

        assertEquals(
            listOf(
                expectedHeadlineParams + mapOf(
                    AnalyticsEvents.Params.ACTION to AnalyticsEvents.Values.ACTION_REMOVE,
                    AnalyticsEvents.Params.SCREEN to AnalyticsEvents.Values.SCREEN_HEADLINE_DETAIL,
                ),
            ),
            analyticsService.paramsOf(AnalyticsEvents.BOOKMARK_TOGGLED),
        )
    }

    @Test
    fun openingAHeadlineLogsHeadlineOpenedWithItsDetails() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        buildViewModel(headline = buildHeadline(), analyticsService = analyticsService)

        assertEquals(listOf(expectedHeadlineParams), analyticsService.paramsOf(AnalyticsEvents.HEADLINE_OPENED))
    }

    @Test
    fun loadedEncouragementLogsEncouragementLoadedWithHeadlineDetails() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        buildViewModel(headline = buildHeadline(), analyticsService = analyticsService)

        assertEquals(listOf(expectedHeadlineParams), analyticsService.paramsOf(AnalyticsEvents.ENCOURAGEMENT_LOADED))
        assertEquals(emptyList(), analyticsService.paramsOf(AnalyticsEvents.ENCOURAGEMENT_FAILED))
    }

    @Test
    fun failedEncouragementLogsEncouragementFailedWithHeadlineDetails() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        buildViewModel(headline = buildHeadline(), encouragement = null, analyticsService = analyticsService)

        assertEquals(listOf(expectedHeadlineParams), analyticsService.paramsOf(AnalyticsEvents.ENCOURAGEMENT_FAILED))
        assertEquals(emptyList(), analyticsService.paramsOf(AnalyticsEvents.ENCOURAGEMENT_LOADED))
    }

    @Test
    fun headlineEventsCarryNoTitleUrlOrArticleText() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForHeadlineDetailScreen()
        val headline = buildHeadline()
        val vm = buildViewModel(headline = headline, analyticsService = analyticsService)
        vm.onIntent(HeadlineDetailContract.Intent.ToggleBookmark)

        val loggedValues = analyticsService.loggedEvents.flatMap { (_, params) -> params.values }
        listOf(headline.title, headline.url, headline.snippet).forEach { forbidden ->
            assertEquals(false, forbidden in loggedValues, "logged $forbidden")
        }
    }

    private fun buildViewModel(
        headline: Headline? = null,
        encouragement: Encouragement? = buildEncouragement(),
        figure: Figure? = null,
        quoteRepository: QuoteRepository = FakeQuoteRepository(),
        articleUrl: String = "https://example.com/article",
        analyticsService: AnalyticsService = FakeAnalyticsServiceForHeadlineDetailScreen(),
        isBookmarked: Boolean = false,
    ) = HeadlineDetailViewModel(
        articleUrl = articleUrl,
        headlineRepository = FakeHeadlineRepository(headline),
        encouragementRepository = FakeEncouragementRepository(encouragement, isBookmarked),
        figureRepository = FakeFigureRepository(figure),
        quoteRepository = quoteRepository,
        analyticsService = analyticsService,
    )
}

private class FakeAnalyticsServiceForHeadlineDetailScreen : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
    override fun logScreenView(screenName: String) = Unit

    fun paramsOf(name: String): List<Map<String, String>> = loggedEvents.filter { it.first == name }.map { it.second }
}

private fun buildHeadline(url: String = "https://example.com/article") = Headline(
    id = 1L,
    title = "Test Headline",
    source = "Reuters",
    url = url,
    imageUrl = null,
    publishedAt = 0L,
    fetchedAt = 0L,
    snippet = "The opening paragraph of the article.",
    category = "world",
    uuid = "3f2c1a",
)

private val expectedHeadlineParams = mapOf(
    AnalyticsEvents.Params.HEADLINE_ID to "3f2c1a",
    AnalyticsEvents.Params.HEADLINE_SOURCE to "Reuters",
    AnalyticsEvents.Params.HEADLINE_TAB to "world",
)

private fun buildFigure(id: Long = 1L, name: String = "Augustine", bio: String = "") = Figure(
    id = id,
    name = name,
    category = FigureCategory.THEOLOGIAN,
    century = "4th",
    bio = bio,
)

private fun buildEncouragement(
    figureName: String = "Augustine",
    quoteText: String = "Our heart is restless until it rests in Thee",
    scriptureReference: String = "Confessions 1.1",
    connectionThemes: List<String> = listOf("peace"),
) = Encouragement(
    summary = null,
    quoteText = quoteText,
    figureName = figureName,
    figureRole = "Bishop of Hippo",
    scriptureReference = scriptureReference,
    scriptureText = "You have made us for yourself",
    explanation = "Matches because of themes of rest.",
    connectionThemes = connectionThemes,
    matchTheme = "peace",
    tone = "hopeful",
)

private data class SavedQuote(
    val text: String,
    val source: String,
    val themes: List<String>,
    val figureId: Long,
)

private class FakeQuoteRepository(private val throwOnSave: Boolean = false) : QuoteRepository {
    val savedQuotes = mutableListOf<SavedQuote>()

    override fun observeAllQuotes(): Flow<List<com.mediasage.domain.model.Quote>> = flowOf(emptyList())
    override fun observeQuotesByFigure(figureId: Long): Flow<List<com.mediasage.domain.model.Quote>> = flowOf(emptyList())
    override suspend fun getQuoteById(id: Long): com.mediasage.domain.model.Quote? = null
    override suspend fun getLatestQuoteForFigure(figureId: Long): com.mediasage.domain.model.Quote? = null

    override suspend fun saveQuote(text: String, source: String, themes: List<String>, figureId: Long) {
        if (throwOnSave) error("simulated save failure")
        savedQuotes.add(SavedQuote(text, source, themes, figureId))
    }

    override suspend fun syncLibrary() = Unit

    override fun observeMemorizedQuote(): Flow<com.mediasage.domain.model.Quote?> = flowOf(null)
    override suspend fun memorizeQuote(figureId: Long, text: String) = Unit
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}

private class FakeHeadlineRepository(private val headline: Headline?) : HeadlineRepository {
    val markReadCalls = mutableListOf<String>()

    override fun observeHeadlines(): Flow<List<Headline>> = flowOf(listOfNotNull(headline))
    override suspend fun getHeadlineById(id: Long): Headline? = headline?.takeIf { it.id == id }
    override suspend fun getHeadlineByUrl(url: String): Headline? = headline?.takeIf { it.url == url }
    override suspend fun refreshHeadlines() = Unit
    override suspend fun clearOldHeadlines(olderThanMillis: Long) = Unit
    override suspend fun markAsRead(url: String) {
        markReadCalls.add(url)
    }
}

private class FakeEncouragementRepository(
    private val encouragement: Encouragement?,
    private val isBookmarked: Boolean = false,
) : EncouragementRepository {
    override suspend fun getEncouragement(
        headlineTitle: String,
        headlineSource: String,
        headlineImageUrl: String?,
        articleUrl: String?,
        articleSnippet: String?,
        headlineCategory: String,
        headlinePublishedAt: Long,
    ): Encouragement = encouragement ?: error("simulated encouragement failure")

    override fun observeAll(): Flow<List<Encouragement>> = MutableStateFlow(emptyList())
    override fun observeBookmarked(): Flow<List<Encouragement>> = MutableStateFlow(emptyList())
    override fun observeCountByFigureName(): Flow<Map<String, Int>> = MutableStateFlow(emptyMap())
    override fun observeByFigureId(figureId: Long): Flow<List<Encouragement>> = MutableStateFlow(emptyList())
    override fun observeIsBookmarked(articleUrl: String): Flow<Boolean> = MutableStateFlow(isBookmarked)
    override fun observeByEpochDay(epochDay: Long): Flow<List<Encouragement>> = MutableStateFlow(emptyList())
    override fun observeActiveEpochDays(): Flow<Set<Long>> = MutableStateFlow(emptySet())
    override suspend fun toggleBookmark(articleUrl: String) = Unit
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}

private class FakeFigureRepository(private val figure: Figure?) : FigureRepository {
    override fun observeAllFigures(): Flow<List<Figure>> = flowOf(listOfNotNull(figure))
    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> = flowOf(listOfNotNull(figure))
    override suspend fun getFigureById(id: Long): Figure? = figure?.takeIf { it.id == id }
    override suspend fun getFigureByName(name: String): Figure? = figure?.takeIf { it.name == name }
    override suspend fun syncFigures() = Unit
}
