@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.figures

import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.repository.epochMillis
import com.mediasage.domain.model.BriefingDay
import com.mediasage.domain.model.DailyReflection
import com.mediasage.domain.model.DayAssignment
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.model.Quote
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.QuoteRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class FigureDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val augustine = Figure(
        id = 1L, name = "Augustine of Hippo", category = FigureCategory.CHURCH_FATHER, century = "4th", role = "Bishop of Hippo"
    )
    private val lewis = Figure(id = 2L, name = "C.S. Lewis", category = FigureCategory.THEOLOGIAN, century = "20th", role = "Author")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun pinToHome_opensTheLensPickerWithoutAssigningAnything() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, analyticsService) =
            figureDetailViewModel(figureId = 2L, figures = listOf(augustine, lewis))

        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertTrue(state.isLensPickerOpen)
        assertTrue(dayAssignmentRepo.assignCalls.isEmpty())
        assertTrue(analyticsService.loggedEvents.isEmpty())
    }

    @Test
    fun lensSelected_assignsTodayWithTheChosenLensWhenTodayHasNoBriefingYet() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, analyticsService) =
            figureDetailViewModel(figureId = 2L, figures = listOf(augustine, lewis))
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(LensFilter.HOPE))

        assertEquals(listOf(Triple(todayOrdinal, 2L, LensFilter.HOPE as LensFilter?)), dayAssignmentRepo.assignCalls)
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertFalse(state.isLensPickerOpen)
        assertNull(state.pendingReassignment)
        assertEquals(
            listOf(AnalyticsEvents.FIGURE_PINNED to mapOf(AnalyticsEvents.Params.FIGURE_ID to "2")),
            analyticsService.loggedEvents,
        )
    }

    @Test
    fun dismissLensPicker_closesThePickerAndChangesNothing() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, _) = figureDetailViewModel(figureId = 2L, figures = listOf(augustine, lewis))
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        viewModel.onIntent(FigureDetailContract.Intent.DismissLensPicker)

        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertFalse(state.isLensPickerOpen)
        assertNull(state.pendingReassignment)
        assertTrue(dayAssignmentRepo.assignCalls.isEmpty())
    }

    @Test
    fun lensSelected_promptsConfirmationNamingTheLensWhenTodayAlreadyBriefedForADifferentFigure() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, analyticsService) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
        )
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(LensFilter.HOPE))

        assertTrue(dayAssignmentRepo.assignCalls.isEmpty())
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertFalse(state.isLensPickerOpen)
        val pending = assertNotNull(state.pendingReassignment)
        assertEquals("Augustine of Hippo", pending.currentFigureName)
        assertEquals("C.S. Lewis", pending.newFigureName)
        assertEquals(LensFilter.HOPE, pending.lens)
        assertTrue(pending.isReporterChange)
        assertTrue(analyticsService.loggedEvents.isEmpty())
    }

    @Test
    fun confirmReassignment_appliesTheChosenFigureAndLensAndClearsTheDialog() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, analyticsService) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
        )
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)
        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(LensFilter.HOPE))

        viewModel.onIntent(FigureDetailContract.Intent.ConfirmReassignment)

        assertEquals(listOf(Triple(todayOrdinal, 2L, LensFilter.HOPE as LensFilter?)), dayAssignmentRepo.assignCalls)
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertNull(state.pendingReassignment)
        assertEquals(
            listOf(AnalyticsEvents.FIGURE_PINNED to mapOf(AnalyticsEvents.Params.FIGURE_ID to "2")),
            analyticsService.loggedEvents,
        )
    }

    @Test
    fun cancelReassignment_leavesAssignmentUnchanged() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, _) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
        )
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)
        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(LensFilter.HOPE))

        viewModel.onIntent(FigureDetailContract.Intent.CancelReassignment)

        assertTrue(dayAssignmentRepo.assignCalls.isEmpty())
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertNull(state.pendingReassignment)
    }

    @Test
    fun pinToHome_unpinningAlreadyPinnedFigureClearsWithNoPickerOrDialog() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, analyticsService) = figureDetailViewModel(
            figureId = 1L,
            figures = listOf(augustine, lewis),
            assignments = mapOf(todayOrdinal to DayAssignment(figureId = 1L, lens = null)),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
        )

        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        assertEquals(listOf(todayOrdinal), dayAssignmentRepo.clearCalls)
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertFalse(state.isLensPickerOpen)
        assertNull(state.pendingReassignment)
        assertTrue(analyticsService.loggedEvents.isEmpty())
    }

    @Test
    fun lensSelected_repinningTodaysLockedFigureWithTodaysLensRestoresItWithNoDialog() = runTest(testDispatcher) {
        // The figure is already today's locked-in reporter; the weekday slot only points elsewhere.
        // Choosing today's lens must restore the slot as-is rather than prompt or start a second briefing.
        val (viewModel, dayAssignmentRepo, analyticsService) = figureDetailViewModel(
            figureId = 1L,
            figures = listOf(augustine, lewis),
            assignments = mapOf(todayOrdinal to DayAssignment(figureId = 2L, lens = null)),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
            lockedThemesByEpochDay = mapOf(todayEpochDay to LensFilter.HOPE),
        )
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(LensFilter.HOPE))

        assertEquals(listOf(Triple(todayOrdinal, 1L, LensFilter.HOPE as LensFilter?)), dayAssignmentRepo.assignCalls)
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertNull(state.pendingReassignment)
        assertEquals(listOf(AnalyticsEvents.FIGURE_PINNED), analyticsService.loggedEvents.map { it.first })
    }

    @Test
    fun lensSelected_repinningTodaysLockedFigureWithADifferentLensPromptsALensOnlyChange() = runTest(testDispatcher) {
        val (viewModel, dayAssignmentRepo, _) = figureDetailViewModel(
            figureId = 1L,
            figures = listOf(augustine, lewis),
            assignments = mapOf(todayOrdinal to DayAssignment(figureId = 2L, lens = null)),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
            lockedThemesByEpochDay = mapOf(todayEpochDay to LensFilter.HOPE),
        )
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)

        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(LensFilter.GRACE))

        assertTrue(dayAssignmentRepo.assignCalls.isEmpty())
        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        val pending = assertNotNull(state.pendingReassignment)
        assertFalse(pending.isReporterChange)
        assertEquals(LensFilter.GRACE, pending.lens)
    }

    @Test
    fun confirmReassignment_clearsDialogImmediatelyAndIgnoresASecondTapWhileSyncStillInProgress() = runTest(testDispatcher) {
        val gate = CompletableDeferred<Unit>()
        val (viewModel, dayAssignmentRepo, _) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            lockedFigureIdsByEpochDay = mapOf(todayEpochDay to 1L),
            holdWrites = gate,
        )
        viewModel.onIntent(FigureDetailContract.Intent.PinToHome)
        viewModel.onIntent(FigureDetailContract.Intent.LensSelected(null))

        viewModel.onIntent(FigureDetailContract.Intent.ConfirmReassignment)

        val stateAfterFirstTap = viewModel.state.value as FigureDetailContract.UiState.Success
        assertNull(stateAfterFirstTap.pendingReassignment)
        assertTrue(dayAssignmentRepo.assignCalls.isEmpty())

        viewModel.onIntent(FigureDetailContract.Intent.ConfirmReassignment)
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(Triple(todayOrdinal, 2L, null as LensFilter?)), dayAssignmentRepo.assignCalls)
    }

    @Test
    fun pinQuote_memorizesTheQuoteForThisFigure() = runTest(testDispatcher) {
        val quoteRepo = DetailFakeQuoteRepository()
        val (viewModel, _, analyticsService) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            quoteRepo = quoteRepo,
        )

        viewModel.onIntent(FigureDetailContract.Intent.PinQuote("You are never too old to dream."))

        assertEquals(listOf(2L to "You are never too old to dream."), quoteRepo.memorizeCalls)
        assertEquals(
            listOf(AnalyticsEvents.QUOTE_MEMORIZED to mapOf(AnalyticsEvents.Params.FIGURE_ID to "2")),
            analyticsService.loggedEvents,
        )
    }

    @Test
    fun quotes_listEveryLibraryQuoteOfTheFigureWithItsSource() = runTest(testDispatcher) {
        val library = listOf(
            Quote(id = 1L, figureId = 2L, text = "You are never too old to dream.", source = "Letters (1955)", themes = emptyList()),
            Quote(id = 2L, figureId = 2L, text = "Aim at heaven.", source = "Mere Christianity (1952)", themes = emptyList()),
        )
        val (viewModel, _, _) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            quoteRepo = DetailFakeQuoteRepository(library = library),
        )

        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertEquals(
            listOf(
                FigureQuoteItem("You are never too old to dream.", "Letters (1955)"),
                FigureQuoteItem("Aim at heaven.", "Mere Christianity (1952)"),
            ),
            state.quotes,
        )
    }

    @Test
    fun quotes_reflectWhicheverQuoteIsCurrentlyMemorized() = runTest(testDispatcher) {
        val memorized = Quote(id = 1L, figureId = 2L, text = "You are never too old to dream.", source = "", themes = emptyList())
        val (viewModel, _, _) = figureDetailViewModel(
            figureId = 2L,
            figures = listOf(augustine, lewis),
            quoteRepo = DetailFakeQuoteRepository(memorized, library = listOf(memorized)),
        )

        val state = viewModel.state.value as FigureDetailContract.UiState.Success
        assertTrue(state.quotes.single().isPinned)
    }

    /**
     * Builds the ViewModel and starts collecting its state. `_state` is a plain `MutableStateFlow`
     * fed by a `viewModelScope.launch` in `init { load() }`, which `UnconfinedTestDispatcher` runs
     * eagerly — no separate collector is required for `state.value` to reflect the pipeline output.
     */
    private fun TestScope.figureDetailViewModel(
        figureId: Long,
        figures: List<Figure>,
        assignments: Map<Int, DayAssignment> = emptyMap(),
        lockedFigureIdsByEpochDay: Map<Long, Long> = emptyMap(),
        lockedThemesByEpochDay: Map<Long, LensFilter> = emptyMap(),
        quoteRepo: DetailFakeQuoteRepository = DetailFakeQuoteRepository(),
        analyticsService: FakeAnalyticsServiceForFigureDetail = FakeAnalyticsServiceForFigureDetail(),
        holdWrites: CompletableDeferred<Unit>? = null,
    ): Triple<FigureDetailViewModel, DetailFakeDayAssignmentRepository, FakeAnalyticsServiceForFigureDetail> {
        val figureRepo = DetailFakeFigureRepository(figures)
        val dayAssignmentRepo = DetailFakeDayAssignmentRepository(MutableStateFlow(assignments), holdWrites)
        val reflectionRepo = FakeDailyReflectionRepository(lockedFigureIdsByEpochDay, lockedThemesByEpochDay)
        val viewModel = FigureDetailViewModel(
            figureId, figureRepo, dayAssignmentRepo, reflectionRepo, quoteRepo, analyticsService,
        )
        backgroundScope.launch(testDispatcher) { viewModel.state.collect {} }
        return Triple(viewModel, dayAssignmentRepo, analyticsService)
    }

    private companion object {
        val today = Instant.fromEpochMilliseconds(epochMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayOrdinal = today.dayOfWeek.ordinal
        val todayEpochDay = today.toEpochDays().toLong()
    }
}

private class DetailFakeFigureRepository(private val figures: List<Figure>) : FigureRepository {
    private val flow = MutableStateFlow(figures)
    override fun observeAllFigures(): Flow<List<Figure>> = flow
    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> = MutableStateFlow(emptyList())
    override suspend fun getFigureById(id: Long): Figure? = figures.firstOrNull { it.id == id }
    override suspend fun getFigureByName(name: String): Figure? = figures.firstOrNull { it.name == name }
    override suspend fun syncFigures() = Unit
}

private class DetailFakeDayAssignmentRepository(
    private val assignmentsFlow: MutableStateFlow<Map<Int, DayAssignment>>,
    private val holdWrites: CompletableDeferred<Unit>? = null,
) : DayAssignmentRepository {
    val assignCalls = mutableListOf<Triple<Int, Long, LensFilter?>>()
    val clearCalls = mutableListOf<Int>()
    override fun observeAssignments(): Flow<Map<Int, DayAssignment>> = assignmentsFlow
    override suspend fun assign(dayOfWeek: Int, figureId: Long, lens: LensFilter?) {
        holdWrites?.await()
        assignCalls.add(Triple(dayOfWeek, figureId, lens))
    }
    override suspend fun clear(dayOfWeek: Int) {
        clearCalls.add(dayOfWeek)
    }
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolveReporter(epochDay: Long, dayOfWeek: Int): Long? = null
    override suspend fun resolve(userId: String?) = Unit
}

private class DetailFakeQuoteRepository(
    private val memorizedQuote: Quote? = null,
    private val library: List<Quote> = emptyList(),
) : QuoteRepository {
    val memorizeCalls = mutableListOf<Pair<Long, String>>()
    override fun observeAllQuotes(): Flow<List<Quote>> = MutableStateFlow(library)
    override fun observeQuotesByFigure(figureId: Long): Flow<List<Quote>> =
        MutableStateFlow(library.filter { it.figureId == figureId })
    override suspend fun getQuoteById(id: Long): Quote? = memorizedQuote?.takeIf { it.id == id }
    override suspend fun getLatestQuoteForFigure(figureId: Long): Quote? = memorizedQuote
    override suspend fun saveQuote(text: String, source: String, themes: List<String>, figureId: Long) = Unit
    override suspend fun syncLibrary() = Unit
    override fun observeMemorizedQuote(): Flow<Quote?> = MutableStateFlow(memorizedQuote)
    override suspend fun memorizeQuote(figureId: Long, text: String) {
        memorizeCalls.add(figureId to text)
    }
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}

private class FakeAnalyticsServiceForFigureDetail : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
    override fun logScreenView(screenName: String) = Unit
}

private class FakeDailyReflectionRepository(
    private val lockedFigureIdsByEpochDay: Map<Long, Long> = emptyMap(),
    private val lockedThemesByEpochDay: Map<Long, LensFilter> = emptyMap(),
) : DailyReflectionRepository {
    override suspend fun getOrFetch(
        figureId: Long,
        figureName: String,
        headlines: List<String>,
        tone: String,
        theme: String?,
    ): DailyReflection = throw UnsupportedOperationException()
    override fun observeByEpochDayRange(startEpochDay: Long, endEpochDay: Long): Flow<List<BriefingDay>> =
        MutableStateFlow(emptyList())
    override suspend fun getForDay(epochDay: Long, tone: String): DailyReflection? = null
    override suspend fun getEarliestBriefingEpochDay(): Long? = null
    override suspend fun getLockedFigureId(epochDay: Long): Long? = lockedFigureIdsByEpochDay[epochDay]
    override suspend fun getLockedTheme(epochDay: Long): LensFilter? = lockedThemesByEpochDay[epochDay]
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}
