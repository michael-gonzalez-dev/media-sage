package com.mediasage.feature.onboarding

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val figureRepository = FakeOnboardingFigureRepository()
    private val dayAssignmentRepository = FakeOnboardingDayAssignmentRepository()

    private fun viewModel() = OnboardingViewModel(figureRepository, dayAssignmentRepository, todayDayOfWeek = { TODAY })

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stepsRunBriefingThenPickThenHeadlinesReaderAndReflect() {
        assertEquals(
            listOf(
                OnboardingContract.Step.BRIEFING,
                OnboardingContract.Step.PICK,
                OnboardingContract.Step.HEADLINES,
                OnboardingContract.Step.READER,
                OnboardingContract.Step.REFLECT,
            ),
            viewModel().state.value.steps,
        )
    }

    @Test
    fun continueMovesToTheNextStep() = runTest(testDispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }

        viewModel.onIntent(OnboardingContract.Intent.Continue)

        assertEquals(OnboardingContract.Step.PICK, viewModel.state.value.currentStep)
    }

    @Test
    fun backReturnsToThePreviousStep() = runTest(testDispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onIntent(OnboardingContract.Intent.Continue)

        viewModel.onIntent(OnboardingContract.Intent.Back)

        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
    }

    @Test
    fun backOnTheFirstStepStaysOnItWithoutFinishing() = runTest(testDispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        val effects = mutableListOf<OnboardingContract.SideEffect>()
        backgroundScope.launch { viewModel.sideEffects.collect { effects.add(it) } }

        viewModel.onIntent(OnboardingContract.Intent.Back)

        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
        assertEquals(emptyList(), effects)
    }

    @Test
    fun skipOnTheFirstStepFinishesOnTodaysDefaultWithoutSaving() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(TEN_BOOM.id, LensFilter.HOPE))
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        val effects = collectEffects(viewModel)

        viewModel.onIntent(OnboardingContract.Intent.Skip)

        assertEquals(listOf<OnboardingContract.SideEffect>(OnboardingContract.SideEffect.Finished), effects)
        assertEquals(emptyList<Triple<Int, Long, LensFilter?>>(), dayAssignmentRepository.assignCalls)
    }

    @Test
    fun skipFinishesFromEveryStep() = runTest(testDispatcher) {
        OnboardingContract.Step.entries.indices.forEach { index ->
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            val effects = collectEffects(viewModel)
            viewModel.onIntent(OnboardingContract.Intent.GoToStep(index))

            viewModel.onIntent(OnboardingContract.Intent.Skip)

            assertEquals(listOf<OnboardingContract.SideEffect>(OnboardingContract.SideEffect.Finished), effects, "step $index")
        }
    }

    @Test
    fun skipBeforeTheScheduleSettlesStillFinishes() = runTest(testDispatcher) {
        dayAssignmentRepository.resolved.value = false
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        val effects = collectEffects(viewModel)

        viewModel.onIntent(OnboardingContract.Intent.Skip)

        assertEquals(listOf<OnboardingContract.SideEffect>(OnboardingContract.SideEffect.Finished), effects)
    }

    @Test
    fun skipAfterPickingKeepsThePick() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(LEWIS.id, null))
        val viewModel = onPickStep()
        viewModel.onIntent(OnboardingContract.Intent.SelectReporter(TEN_BOOM.id, LensFilter.HOPE))
        viewModel.onIntent(OnboardingContract.Intent.Continue)

        viewModel.onIntent(OnboardingContract.Intent.Skip)

        assertEquals(listOf(Triple(TODAY, TEN_BOOM.id, LensFilter.HOPE as LensFilter?)), dayAssignmentRepository.assignCalls)
    }

    @Test
    fun continueOnThePickStepMovesOnAndBackReturnsToTheBriefing() = runTest(testDispatcher) {
        val viewModel = onPickStep()
        val effects = collectEffects(viewModel)

        viewModel.onIntent(OnboardingContract.Intent.Continue)
        assertEquals(OnboardingContract.Step.HEADLINES, viewModel.state.value.currentStep)
        assertEquals(emptyList(), effects)

        repeat(2) { viewModel.onIntent(OnboardingContract.Intent.Back) }
        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
    }

    @Test
    fun aSwipeMovesToTheStepItSettlesOnWithoutFinishing() = runTest(testDispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        val effects = collectEffects(viewModel)

        viewModel.onIntent(OnboardingContract.Intent.GoToStep(3))
        assertEquals(OnboardingContract.Step.READER, viewModel.state.value.currentStep)

        viewModel.onIntent(OnboardingContract.Intent.GoToStep(0))
        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
        assertEquals(emptyList(), effects)
    }

    @Test
    fun continueOnTheLastStepFinishesWithoutSavingWhenTheReaderKeptTheDefault() = runTest(testDispatcher) {
        val viewModel = onLastStep()

        viewModel.onIntent(OnboardingContract.Intent.Continue)

        assertEquals(OnboardingContract.SideEffect.Finished, viewModel.sideEffects.first())
        assertEquals(emptyList<Triple<Int, Long, LensFilter?>>(), dayAssignmentRepository.assignCalls)
    }

    @Test
    fun pickStepShowsNoSelectionUntilTheScheduleSettlesThenPreselectsToday() = runTest(testDispatcher) {
        dayAssignmentRepository.resolved.value = false
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        val viewModel = onPickStep()
        assertNull(viewModel.state.value.selection)

        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(TEN_BOOM.id, LensFilter.HOPE))
        dayAssignmentRepository.resolved.value = true

        assertEquals(OnboardingContract.PickSelection(TEN_BOOM.id, LensFilter.HOPE), viewModel.state.value.selection)
    }

    @Test
    fun todayAssignedToADisabledReporterPreselectsTheFirstFigureInstead() = runTest(testDispatcher) {
        // Augustine is today's reporter but was disabled on the server, so he's gone from figures.
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(AUGUSTINE.id, LensFilter.HOPE))
        val viewModel = onPickStep()

        assertEquals(OnboardingContract.PickSelection(LEWIS.id, LensFilter.HOPE), viewModel.state.value.selection)
        assertEquals(LEWIS.id, viewModel.state.value.selectedReporter?.card?.id)
    }

    @Test
    fun aLaterScheduleUpdatesTheDefaultWhileTheReaderHasNotPicked() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(LEWIS.id, null))
        val viewModel = onPickStep()
        assertEquals(OnboardingContract.PickSelection(LEWIS.id, LensFilter.NEWS), viewModel.state.value.selection)

        // The signed-in schedule replaces the fallback defaults moments later.
        dayAssignmentRepository.resolved.value = false
        assertNull(viewModel.state.value.selection)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(TEN_BOOM.id, LensFilter.WRITINGS))
        dayAssignmentRepository.resolved.value = true

        assertEquals(OnboardingContract.PickSelection(TEN_BOOM.id, LensFilter.WRITINGS), viewModel.state.value.selection)
    }

    @Test
    fun theReadersPickIsNeverReplacedByTheScheduleSettlingAfterwards() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(LEWIS.id, null))
        val viewModel = onPickStep()

        viewModel.onIntent(OnboardingContract.Intent.SelectReporter(TEN_BOOM.id, LensFilter.HOPE))
        dayAssignmentRepository.resolved.value = false
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(LEWIS.id, LensFilter.WRITINGS))
        dayAssignmentRepository.resolved.value = true

        assertEquals(OnboardingContract.PickSelection(TEN_BOOM.id, LensFilter.HOPE), viewModel.state.value.selection)
    }

    @Test
    fun aPickBeforeTheScheduleSettlesIsIgnoredSoFinishingSavesNothing() = runTest(testDispatcher) {
        dayAssignmentRepository.resolved.value = false
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        val viewModel = onPickStep()
        val effects = collectEffects(viewModel)

        viewModel.onIntent(OnboardingContract.Intent.SelectReporter(TEN_BOOM.id, LensFilter.HOPE))
        viewModel.onIntent(OnboardingContract.Intent.Skip)

        assertNull(viewModel.state.value.selection)
        assertEquals(listOf<OnboardingContract.SideEffect>(OnboardingContract.SideEffect.Finished), effects)
        assertEquals(emptyList<Triple<Int, Long, LensFilter?>>(), dayAssignmentRepository.assignCalls)
    }

    @Test
    fun finishingSavesThePickForTodayOnlyBeforeOpeningTheApp() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(LEWIS, TEN_BOOM)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(LEWIS.id, null))
        val viewModel = onPickStep()
        val effects = collectEffects(viewModel)

        viewModel.onIntent(OnboardingContract.Intent.SelectReporter(TEN_BOOM.id, LensFilter.HOPE))
        repeat(OnboardingContract.Step.entries.size - 1) { viewModel.onIntent(OnboardingContract.Intent.Continue) }

        assertEquals(listOf(Triple(TODAY, TEN_BOOM.id, LensFilter.HOPE as LensFilter?)), dayAssignmentRepository.assignCalls)
        assertEquals(listOf<OnboardingContract.SideEffect>(OnboardingContract.SideEffect.Finished), effects)
    }

    @Test
    fun eraChipsNarrowTheDeckButTheSelectionStaysShown() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(LEWIS, AUGUSTINE)
        dayAssignmentRepository.assignments.value = mapOf(TODAY to DayAssignment(LEWIS.id, null))
        val viewModel = onPickStep()

        viewModel.onIntent(OnboardingContract.Intent.SelectEra(FigureEra.EARLY_CHURCH))

        assertEquals(listOf(AUGUSTINE.id), viewModel.state.value.deck.map { it.card.id })
        assertEquals(LEWIS.name, viewModel.state.value.selectedReporter?.card?.name)
        assertEquals(2, viewModel.state.value.reporters.size)
    }

    private fun TestScope.onPickStep(): OnboardingViewModel {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onIntent(OnboardingContract.Intent.Continue)
        return viewModel
    }

    private fun TestScope.onLastStep(): OnboardingViewModel {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onIntent(OnboardingContract.Intent.GoToStep(OnboardingContract.Step.entries.lastIndex))
        return viewModel
    }

    private fun TestScope.collectEffects(viewModel: OnboardingViewModel): List<OnboardingContract.SideEffect> {
        val effects = mutableListOf<OnboardingContract.SideEffect>()
        backgroundScope.launch { viewModel.sideEffects.collect { effects.add(it) } }
        return effects
    }

    @Test
    fun sampleFiguresCarryCachedPortraits() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(
            Figure(id = 1, name = "Corrie ten Boom", category = FigureCategory.MISSIONARY, century = "20th", portraitUrl = "https://x/a.png"),
        )
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }

        val figures = viewModel.state.value.sampleFigures
        assertEquals(OnboardingContract.SAMPLE_FIGURE_NAMES, figures.map { it.name })
        assertEquals("https://x/a.png", figures.first().portraitUrl)
        // Not yet synced: falls back to initials rather than dropping the reporter.
        assertNull(figures[1].portraitUrl)
    }
}

private const val TODAY = 4
private val LEWIS = Figure(id = 1, name = "C.S. Lewis", category = FigureCategory.THEOLOGIAN, century = "20th")
private val TEN_BOOM = Figure(id = 2, name = "Corrie ten Boom", category = FigureCategory.MISSIONARY, century = "20th")
private val AUGUSTINE = Figure(id = 3, name = "Augustine of Hippo", category = FigureCategory.CHURCH_FATHER, century = "4th")

private class FakeOnboardingDayAssignmentRepository : DayAssignmentRepository {
    val assignments = MutableStateFlow<Map<Int, DayAssignment>>(emptyMap())
    val resolved = MutableStateFlow(true)
    val assignCalls = mutableListOf<Triple<Int, Long, LensFilter?>>()
    override fun observeAssignments(): Flow<Map<Int, DayAssignment>> = assignments
    override suspend fun assign(dayOfWeek: Int, figureId: Long, lens: LensFilter?) {
        assignCalls.add(Triple(dayOfWeek, figureId, lens))
    }
    override suspend fun clear(dayOfWeek: Int) = Unit
    override suspend fun resolveReporter(epochDay: Long, dayOfWeek: Int): Long? = null
    override val isResolved: StateFlow<Boolean> = resolved
    override suspend fun resolve(userId: String?) = Unit
}

private class FakeOnboardingFigureRepository : FigureRepository {
    val figures = MutableStateFlow<List<Figure>>(emptyList())
    override fun observeAllFigures(): Flow<List<Figure>> = figures
    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> = figures
    override suspend fun getFigureById(id: Long): Figure? = null
    override suspend fun getFigureByName(name: String): Figure? = null
    override suspend fun syncFigures() = Unit
}
