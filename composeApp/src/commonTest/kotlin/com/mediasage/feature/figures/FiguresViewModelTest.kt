@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.figures

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.mediasage.data.ReporterView
import com.mediasage.data.ReporterViewPreferencesRepository
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.repository.epochMillis
import com.mediasage.domain.model.BriefingDay
import com.mediasage.domain.model.DailyReflection
import com.mediasage.domain.model.DayAssignment
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.FigureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class FiguresViewModelTest {

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
    fun emitsLoadingInitially() {
        val figureRepo = FakeFigureRepository(MutableStateFlow(emptyList()))
        val vm = figuresViewModel(figureRepo)

        assertIs<FiguresContract.UiState.Success>(vm.state.value)
    }

    @Test
    fun emitsSuccessWithFiguresFromRepository() = runTest(testDispatcher) {
        val figures = listOf(buildFigure("Augustine", "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo)

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(1, state.figures.size)
        assertEquals("Augustine", state.figures[0].name)
        assertEquals("Bishop of Hippo", state.figures[0].role)
    }

    @Test
    fun refreshSetsIsRefreshingTrueThenFalse() = runTest(testDispatcher) {
        val figures = listOf(buildFigure("Augustine", "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo)

        vm.onIntent(FiguresContract.Intent.Refresh)

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(false, state.isRefreshing)
    }

    @Test
    fun refreshCallsSyncFigures() = runTest(testDispatcher) {
        val figures = listOf(buildFigure("Augustine", "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo)

        vm.onIntent(FiguresContract.Intent.Refresh)

        assertEquals(1, figureRepo.syncCallCount)
    }

    @Test
    fun filtersByNameCaseInsensitive() = runTest(testDispatcher) {
        val figures = listOf(
            buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"),
            buildFigure(id = 2L, name = "C.S. Lewis", role = "Author & Apologist")
        )
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo)

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("aug"))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(1, state.figures.size)
        assertEquals("Augustine", state.figures[0].name)
        assertEquals("aug", state.searchQuery)
    }

    @Test
    fun filtersByRoleCaseInsensitive() = runTest(testDispatcher) {
        val figures = listOf(
            buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"),
            buildFigure(id = 2L, name = "C.S. Lewis", role = "Author & Apologist")
        )
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo)

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("APOLOGIST"))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(1, state.figures.size)
        assertEquals("C.S. Lewis", state.figures[0].name)
    }

    @Test
    fun clearingQueryRestoresFullList() = runTest(testDispatcher) {
        val figures = listOf(
            buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"),
            buildFigure(id = 2L, name = "C.S. Lewis", role = "Author & Apologist")
        )
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo)

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("aug"))
        assertEquals(1, assertIs<FiguresContract.UiState.Success>(vm.state.value).figures.size)

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged(""))
        assertEquals(2, assertIs<FiguresContract.UiState.Success>(vm.state.value).figures.size)
    }

    @Test
    fun searchingLogsFigureSearchEventOnceWhenQueryStartsNonBlank() = runTest(testDispatcher) {
        val figures = listOf(buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val analyticsService = FakeAnalyticsServiceForFiguresScreen()
        val vm = figuresViewModel(figureRepo, analyticsService = analyticsService)

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("a"))
        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("au"))

        assertEquals(listOf(AnalyticsEvents.FIGURE_SEARCH to emptyMap()), analyticsService.loggedEvents)
    }

    @Test
    fun clearingThenResearchingLogsFigureSearchEventAgain() = runTest(testDispatcher) {
        val figures = listOf(buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val analyticsService = FakeAnalyticsServiceForFiguresScreen()
        val vm = figuresViewModel(figureRepo, analyticsService = analyticsService)
        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("a"))
        vm.onIntent(FiguresContract.Intent.SearchQueryChanged(""))

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("b"))

        assertEquals(
            listOf(AnalyticsEvents.FIGURE_SEARCH to emptyMap(), AnalyticsEvents.FIGURE_SEARCH to emptyMap()),
            analyticsService.loggedEvents,
        )
    }

    @Test
    fun pinnedFigureSortsBeforeAlphabeticallyEarlierFigure() = runTest(testDispatcher) {
        val figures = listOf(
            buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"),
            buildFigure(id = 2L, name = "Zwingli", role = "Reformer")
        )
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        // Assign Zwingli (id=2) to every day so the test is day-of-week agnostic
        val dayAssignmentRepo = FakeDayAssignmentRepository(assignments = (0..6).associate { it to DayAssignment(figureId = 2L, lens = null) })
        val vm = figuresViewModel(figureRepo, dayAssignmentRepo)

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals("Zwingli", state.figures[0].name)
        assertEquals("Augustine", state.figures[1].name)
    }

    @Test
    fun unpinnedFiguresSortAlphabeticallyAfterTodaysPick() = runTest(testDispatcher) {
        val figures = listOf(
            buildFigure(id = 1L, name = "Zwingli", role = "Reformer"),
            buildFigure(id = 2L, name = "Augustine", role = "Bishop of Hippo"),
            buildFigure(id = 3L, name = "Calvin", role = "Reformer"),
            buildFigure(id = 4L, name = "Bonhoeffer", role = "Theologian & Martyr")
        )
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = figuresViewModel(figureRepo, FakeDayAssignmentRepository(everyDay(3L)))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("Calvin", "Augustine", "Bonhoeffer", "Zwingli"), state.figures.map { it.name })
    }

    @Test
    fun allErasSelectedByDefaultAndEveryReporterShown() = runTest(testDispatcher) {
        val vm = eraViewModel()

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertNull(state.selectedEra)
        assertEquals(4, state.figures.size)
    }

    @Test
    fun selectingEraShowsOnlyReportersFromThatEra() = runTest(testDispatcher) {
        val vm = eraViewModel()

        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS, state.selectedEra)
        assertEquals(listOf("Calvin", "Luther"), state.figures.map { it.name })
    }

    @Test
    fun selectingAnotherEraReplacesThePreviousOne() = runTest(testDispatcher) {
        val vm = eraViewModel()
        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS))

        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.EARLY_CHURCH))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FigureEra.EARLY_CHURCH, state.selectedEra)
        assertEquals(listOf("Augustine"), state.figures.map { it.name })
    }

    @Test
    fun selectingAllShowsEveryoneAgain() = runTest(testDispatcher) {
        val vm = eraViewModel()
        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.MODERN))

        vm.onIntent(FiguresContract.Intent.EraSelected(null))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertNull(state.selectedEra)
        assertEquals(4, state.figures.size)
    }

    @Test
    fun searchNarrowsWithinSelectedEra() = runTest(testDispatcher) {
        val vm = eraViewModel()
        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS))

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("luth"))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("Luther"), state.figures.map { it.name })
        assertEquals(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS, state.selectedEra)
    }

    @Test
    fun searchDoesNotReachOutsideSelectedEra() = runTest(testDispatcher) {
        val vm = eraViewModel()
        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.MODERN))

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("o"))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("Bonhoeffer"), state.figures.map { it.name })
    }

    @Test
    fun eraAndSearchMatchingNoOneYieldsEmptyListWithBothFiltersKept() = runTest(testDispatcher) {
        val vm = eraViewModel()
        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.EARLY_CHURCH))

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("luther"))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(emptyList(), state.figures)
        assertEquals(FigureEra.EARLY_CHURCH, state.selectedEra)
        assertEquals("luther", state.searchQuery)
    }

    @Test
    fun pinnedReporterSortsFirstWithinSelectedEra() = runTest(testDispatcher) {
        // Luther (id=3) assigned to every day so the test is day-of-week agnostic
        val vm = eraViewModel(pinnedId = 3L)

        vm.onIntent(FiguresContract.Intent.EraSelected(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("Luther", "Calvin"), state.figures.map { it.name })
    }

    @Test
    fun reporterCarriesEraDerivedFromCentury() = runTest(testDispatcher) {
        val vm = eraViewModel()

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FigureEra.EARLY_CHURCH, state.figures.first { it.name == "Augustine" }.era)
        assertEquals(FigureEra.MODERN, state.figures.first { it.name == "Bonhoeffer" }.era)
    }

    @Test
    fun deckListsEveryShownReporterByNameWhileTheGridPinsTodaysPickFirst() = runTest(testDispatcher) {
        val vm = figuresViewModel(FakeFigureRepository(MutableStateFlow(abc())), FakeDayAssignmentRepository(everyDay(3L)))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(listOf("Calvin", "Augustine", "Bonhoeffer"), state.figures.map { it.name })
        assertEquals(listOf("Augustine", "Bonhoeffer", "Calvin"), state.deck.map { it.name })
    }

    @Test
    fun todayPick_isTodaysAssignmentWithItsLens() = runTest(testDispatcher) {
        val assignments = (0..6).associateWith { DayAssignment(figureId = 2L, lens = LensFilter.HOPE) }
        val vm = figuresViewModel(FakeFigureRepository(MutableStateFlow(abc())), FakeDayAssignmentRepository(assignments))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FiguresContract.ReporterPick(2L, LensFilter.HOPE), state.todayPick)
        assertNull(state.scheduledPick)
    }

    @Test
    fun todayPick_fallsBackToTheFirstReporterThroughNewsWhenTodayIsUnassigned() = runTest(testDispatcher) {
        val vm = figuresViewModel(FakeFigureRepository(MutableStateFlow(abc())))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FiguresContract.ReporterPick(1L, LensFilter.NEWS), state.todayPick)
        assertTrue(state.figures.first { it.id == 1L }.isPinned)
    }

    @Test
    fun todayPick_fallsBackWhenTodaysReporterIsNoLongerListed() = runTest(testDispatcher) {
        val vm = figuresViewModel(FakeFigureRepository(MutableStateFlow(abc())), FakeDayAssignmentRepository(everyDay(99L)))

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(1L, state.todayPick?.figureId)
    }

    @Test
    fun onceTodaysBriefingIsWritten_itsReporterStaysTodaysPickAndANewPickStartsNextWeek() = runTest(testDispatcher) {
        val assignments = (0..6).associateWith { DayAssignment(figureId = 3L, lens = LensFilter.GRACE) }
        val reflections = FakeDailyReflectionRepositoryForFigures(lockedFigureId = 1L, lockedLens = LensFilter.HOPE)
        val vm = figuresViewModel(
            FakeFigureRepository(MutableStateFlow(abc())),
            FakeDayAssignmentRepository(assignments),
            reflectionRepo = reflections,
        )

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FiguresContract.ReporterPick(1L, LensFilter.HOPE), state.todayPick)
        assertEquals(FiguresContract.ScheduledPick(3L, LensFilter.GRACE, todayWeekdayLabel), state.scheduledPick)
    }

    @Test
    fun lockedBriefingWithNoSavedLensMeansNews() = runTest(testDispatcher) {
        val reflections = FakeDailyReflectionRepositoryForFigures(lockedFigureId = 2L, lockedLens = null)
        val vm = figuresViewModel(FakeFigureRepository(MutableStateFlow(abc())), reflectionRepo = reflections)

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(FiguresContract.ReporterPick(2L, LensFilter.NEWS), state.todayPick)
        assertNull(state.scheduledPick)
    }

    @Test
    fun viewIsTheDeckUntilTheReaderPicksTheGridAndTheChoiceIsKept() = runTest(testDispatcher) {
        val dataStore = FakeReporterViewDataStore()
        val figureRepo = FakeFigureRepository(MutableStateFlow(abc()))
        val vm = figuresViewModel(figureRepo, dataStore = dataStore)
        assertEquals(ReporterView.DECK, assertIs<FiguresContract.UiState.Success>(vm.state.value).view)

        vm.onIntent(FiguresContract.Intent.ViewSelected(ReporterView.GRID))

        assertEquals(ReporterView.GRID, assertIs<FiguresContract.UiState.Success>(vm.state.value).view)
        val reopened = figuresViewModel(figureRepo, dataStore = dataStore)
        assertEquals(ReporterView.GRID, assertIs<FiguresContract.UiState.Success>(reopened.state.value).view)
    }

    @Test
    fun lensSelected_makesTheReporterTodaysPickAtOnceWhenTodayHasNoBriefingYet() = runTest(testDispatcher) {
        val dayAssignments = FakeDayAssignmentRepository()
        val analyticsService = FakeAnalyticsServiceForFiguresScreen()
        val vm = figuresViewModel(FakeFigureRepository(MutableStateFlow(abc())), dayAssignments, analyticsService = analyticsService)

        vm.onIntent(FiguresContract.Intent.LensSelected(3L, LensFilter.HOPE))

        assertEquals(listOf(Triple(todayOrdinal, 3L, LensFilter.HOPE as LensFilter?)), dayAssignments.assignCalls)
        assertNull(assertIs<FiguresContract.UiState.Success>(vm.state.value).pendingReassignment)
        assertEquals(listOf(AnalyticsEvents.FIGURE_PINNED to mapOf(AnalyticsEvents.Params.FIGURE_ID to "3")), analyticsService.loggedEvents)
    }

    @Test
    fun lensSelected_asksFirstWhenTodaysBriefingIsAnotherReporter() = runTest(testDispatcher) {
        val dayAssignments = FakeDayAssignmentRepository()
        val vm = lockedViewModel(dayAssignments)

        vm.onIntent(FiguresContract.Intent.LensSelected(3L, LensFilter.HOPE))

        assertTrue(dayAssignments.assignCalls.isEmpty())
        val pending = assertNotNull(assertIs<FiguresContract.UiState.Success>(vm.state.value).pendingReassignment)
        assertEquals("Augustine", pending.currentFigureName)
        assertEquals("Calvin", pending.newFigureName)
        assertEquals(LensFilter.HOPE, pending.lens)
        assertTrue(pending.isReporterChange)
        assertEquals(todayWeekdayLabel, pending.nextWeekdayLabel)
    }

    @Test
    fun confirmReassignment_assignsTheReporterAndLensAndClosesTheDialog() = runTest(testDispatcher) {
        val dayAssignments = FakeDayAssignmentRepository()
        val vm = lockedViewModel(dayAssignments)
        vm.onIntent(FiguresContract.Intent.LensSelected(3L, LensFilter.HOPE))

        vm.onIntent(FiguresContract.Intent.ConfirmReassignment)

        assertEquals(listOf(Triple(todayOrdinal, 3L, LensFilter.HOPE as LensFilter?)), dayAssignments.assignCalls)
        assertNull(assertIs<FiguresContract.UiState.Success>(vm.state.value).pendingReassignment)
    }

    @Test
    fun cancelReassignment_changesNothing() = runTest(testDispatcher) {
        val dayAssignments = FakeDayAssignmentRepository()
        val vm = lockedViewModel(dayAssignments)
        vm.onIntent(FiguresContract.Intent.LensSelected(3L, LensFilter.HOPE))

        vm.onIntent(FiguresContract.Intent.CancelReassignment)

        assertTrue(dayAssignments.assignCalls.isEmpty())
        assertNull(assertIs<FiguresContract.UiState.Success>(vm.state.value).pendingReassignment)
    }

    @Test
    fun lensSelected_onTodaysLockedReporterThroughTheirLockedLensAssignsWithoutAsking() = runTest(testDispatcher) {
        val dayAssignments = FakeDayAssignmentRepository()
        // No saved lens on the locked briefing means News.
        val vm = lockedViewModel(dayAssignments)

        vm.onIntent(FiguresContract.Intent.LensSelected(1L, LensFilter.NEWS))

        assertEquals(listOf(Triple(todayOrdinal, 1L, LensFilter.NEWS as LensFilter?)), dayAssignments.assignCalls)
        assertNull(assertIs<FiguresContract.UiState.Success>(vm.state.value).pendingReassignment)
    }

    @Test
    fun lensSelected_onTodaysLockedReporterThroughAnotherLensAsksAboutTheLensOnly() = runTest(testDispatcher) {
        val dayAssignments = FakeDayAssignmentRepository()
        val vm = lockedViewModel(dayAssignments)

        vm.onIntent(FiguresContract.Intent.LensSelected(1L, LensFilter.GRACE))

        assertTrue(dayAssignments.assignCalls.isEmpty())
        val pending = assertNotNull(assertIs<FiguresContract.UiState.Success>(vm.state.value).pendingReassignment)
        assertFalse(pending.isReporterChange)
    }

    /** Augustine (id 1) has already written today's briefing, through News. */
    private fun lockedViewModel(dayAssignments: FakeDayAssignmentRepository): FiguresViewModel = figuresViewModel(
        FakeFigureRepository(MutableStateFlow(abc())),
        dayAssignments,
        reflectionRepo = FakeDailyReflectionRepositoryForFigures(lockedFigureId = 1L, lockedLens = null),
    )

    private fun abc() = listOf(
        buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"),
        buildFigure(id = 2L, name = "Bonhoeffer", role = "Theologian & Martyr"),
        buildFigure(id = 3L, name = "Calvin", role = "Reformer")
    )

    // Assigned to every day so the test is day-of-week agnostic.
    private fun everyDay(figureId: Long) = (0..6).associateWith { DayAssignment(figureId = figureId, lens = null) }

    private fun figuresViewModel(
        figureRepo: FakeFigureRepository,
        dayAssignmentRepo: FakeDayAssignmentRepository = FakeDayAssignmentRepository(),
        analyticsService: FakeAnalyticsServiceForFiguresScreen = FakeAnalyticsServiceForFiguresScreen(),
        reflectionRepo: FakeDailyReflectionRepositoryForFigures = FakeDailyReflectionRepositoryForFigures(),
        dataStore: FakeReporterViewDataStore = FakeReporterViewDataStore(),
    ) = FiguresViewModel(
        figureRepo,
        dayAssignmentRepo,
        reflectionRepo,
        ReporterViewPreferencesRepository(dataStore),
        analyticsService,
    )

    private fun eraViewModel(pinnedId: Long? = null): FiguresViewModel {
        val figures = listOf(
            buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo", century = "4th"),
            buildFigure(id = 2L, name = "Calvin", role = "Reformer", century = "16th"),
            buildFigure(id = 3L, name = "Luther", role = "Reformer", century = "16th"),
            buildFigure(id = 4L, name = "Bonhoeffer", role = "Theologian & Martyr", century = "20th")
        )
        val assignments = pinnedId?.let { id -> (0..6).associate { it to DayAssignment(figureId = id, lens = null) } }
        return figuresViewModel(
            FakeFigureRepository(MutableStateFlow(figures)),
            FakeDayAssignmentRepository(assignments = assignments ?: emptyMap()),
        )
    }
    private companion object {
        val today = Instant.fromEpochMilliseconds(epochMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayOrdinal = today.dayOfWeek.ordinal
        val todayEpochDay = today.toEpochDays().toLong()
        val todayWeekdayLabel = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun buildFigure(name: String, role: String) = Figure(
    id = 1L,
    name = name,
    category = FigureCategory.THEOLOGIAN,
    century = "4th",
    role = role
)

private fun buildFigure(id: Long, name: String, role: String, century: String = "4th") = Figure(
    id = id,
    name = name,
    category = FigureCategory.THEOLOGIAN,
    century = century,
    role = role
)

private class FakeAnalyticsServiceForFiguresScreen : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
    override fun logScreenView(screenName: String) = Unit
}

private class FakeDayAssignmentRepository(
    private val assignments: Map<Int, DayAssignment> = emptyMap()
) : DayAssignmentRepository {
    val assignCalls = mutableListOf<Triple<Int, Long, LensFilter?>>()
    override fun observeAssignments(): Flow<Map<Int, DayAssignment>> = flowOf(assignments)
    override suspend fun assign(dayOfWeek: Int, figureId: Long, lens: LensFilter?) {
        assignCalls.add(Triple(dayOfWeek, figureId, lens))
    }
    override suspend fun clear(dayOfWeek: Int) = Unit
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolveReporter(epochDay: Long, dayOfWeek: Int): Long? = null
    override suspend fun resolve(userId: String?) = Unit
}

private class FakeFigureRepository(
    private val flow: MutableStateFlow<List<Figure>>
) : FigureRepository {
    var syncCallCount = 0
        private set

    override fun observeAllFigures(): Flow<List<Figure>> = flow
    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> = flow
    override suspend fun getFigureById(id: Long): Figure? = flow.value.firstOrNull { it.id == id }
    override suspend fun getFigureByName(name: String): Figure? = flow.value.firstOrNull { it.name == name }
    override suspend fun syncFigures() { syncCallCount++ }
}

/** Today's briefing, written by [lockedFigureId] through [lockedLens] (null means News); no briefing when [lockedFigureId] is null. */
private class FakeDailyReflectionRepositoryForFigures(
    private val lockedFigureId: Long? = null,
    private val lockedLens: LensFilter? = null,
) : DailyReflectionRepository {
    override suspend fun getOrFetch(
        figureId: Long,
        figureName: String,
        headlines: List<String>,
        tone: String,
        theme: String?,
    ): DailyReflection = throw UnsupportedOperationException()
    override fun observeByEpochDayRange(startEpochDay: Long, endEpochDay: Long): Flow<List<BriefingDay>> =
        flowOf(listOfNotNull(lockedFigureId?.let { BriefingDay(epochDay = startEpochDay, figureId = it) }))
    override suspend fun getForDay(epochDay: Long, tone: String): DailyReflection? = null
    override suspend fun getEarliestBriefingEpochDay(): Long? = null
    override suspend fun getLockedFigureId(epochDay: Long): Long? = lockedFigureId
    override suspend fun getLockedTheme(epochDay: Long): LensFilter? = lockedLens
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}

private class FakeReporterViewDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}
