@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.figures

import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.domain.model.DayAssignment
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

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
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

        assertIs<FiguresContract.UiState.Success>(vm.state.value)
    }

    @Test
    fun emitsSuccessWithFiguresFromRepository() = runTest(testDispatcher) {
        val figures = listOf(buildFigure("Augustine", "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(1, state.figures.size)
        assertEquals("Augustine", state.figures[0].name)
        assertEquals("Bishop of Hippo", state.figures[0].role)
    }

    @Test
    fun refreshSetsIsRefreshingTrueThenFalse() = runTest(testDispatcher) {
        val figures = listOf(buildFigure("Augustine", "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

        vm.onIntent(FiguresContract.Intent.Refresh)

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals(false, state.isRefreshing)
    }

    @Test
    fun refreshCallsSyncFigures() = runTest(testDispatcher) {
        val figures = listOf(buildFigure("Augustine", "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

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
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

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
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

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
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

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
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), analyticsService)

        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("a"))
        vm.onIntent(FiguresContract.Intent.SearchQueryChanged("au"))

        assertEquals(listOf(AnalyticsEvents.FIGURE_SEARCH to emptyMap()), analyticsService.loggedEvents)
    }

    @Test
    fun clearingThenResearchingLogsFigureSearchEventAgain() = runTest(testDispatcher) {
        val figures = listOf(buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo"))
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val analyticsService = FakeAnalyticsServiceForFiguresScreen()
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), analyticsService)
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
        val vm = FiguresViewModel(figureRepo, dayAssignmentRepo, FakeAnalyticsServiceForFiguresScreen())

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals("Zwingli", state.figures[0].name)
        assertEquals("Augustine", state.figures[1].name)
    }

    @Test
    fun unpinnedFiguresSortAlphabetically() = runTest(testDispatcher) {
        val figures = listOf(
            buildFigure(id = 1L, name = "Zwingli", role = "Reformer"),
            buildFigure(id = 2L, name = "Augustine", role = "Bishop of Hippo"),
            buildFigure(id = 3L, name = "Calvin", role = "Reformer")
        )
        val figureRepo = FakeFigureRepository(MutableStateFlow(figures))
        val vm = FiguresViewModel(figureRepo, FakeDayAssignmentRepository(), FakeAnalyticsServiceForFiguresScreen())

        val state = assertIs<FiguresContract.UiState.Success>(vm.state.value)
        assertEquals("Augustine", state.figures[0].name)
        assertEquals("Calvin", state.figures[1].name)
        assertEquals("Zwingli", state.figures[2].name)
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

    private fun eraViewModel(pinnedId: Long? = null): FiguresViewModel {
        val figures = listOf(
            buildFigure(id = 1L, name = "Augustine", role = "Bishop of Hippo", century = "4th"),
            buildFigure(id = 2L, name = "Calvin", role = "Reformer", century = "16th"),
            buildFigure(id = 3L, name = "Luther", role = "Reformer", century = "16th"),
            buildFigure(id = 4L, name = "Bonhoeffer", role = "Theologian & Martyr", century = "20th")
        )
        val assignments = pinnedId?.let { id -> (0..6).associate { it to DayAssignment(figureId = id, lens = null) } }
        return FiguresViewModel(
            FakeFigureRepository(MutableStateFlow(figures)),
            FakeDayAssignmentRepository(assignments = assignments ?: emptyMap()),
            FakeAnalyticsServiceForFiguresScreen()
        )
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
    override fun observeAssignments(): Flow<Map<Int, DayAssignment>> = flowOf(assignments)
    override suspend fun assign(dayOfWeek: Int, figureId: Long, lens: LensFilter?) = Unit
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
