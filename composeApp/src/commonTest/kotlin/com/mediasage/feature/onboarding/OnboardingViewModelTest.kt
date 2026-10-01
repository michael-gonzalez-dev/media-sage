package com.mediasage.feature.onboarding

import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureCategory
import com.mediasage.domain.repository.FigureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stepsRunBriefingThenHeadlinesThenReader() {
        assertEquals(
            listOf(OnboardingContract.Step.BRIEFING, OnboardingContract.Step.HEADLINES, OnboardingContract.Step.READER),
            OnboardingViewModel(figureRepository).state.value.steps,
        )
    }

    @Test
    fun continueMovesToTheNextStep() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }

        viewModel.onIntent(OnboardingContract.Intent.Continue)

        assertEquals(OnboardingContract.Step.HEADLINES, viewModel.state.value.currentStep)
    }

    @Test
    fun backReturnsToThePreviousStep() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onIntent(OnboardingContract.Intent.Continue)

        viewModel.onIntent(OnboardingContract.Intent.Back)

        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
    }

    @Test
    fun backOnTheFirstStepStaysOnItWithoutFinishing() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }
        val effects = mutableListOf<OnboardingContract.SideEffect>()
        backgroundScope.launch { viewModel.sideEffects.collect { effects.add(it) } }

        viewModel.onIntent(OnboardingContract.Intent.Back)

        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
        assertEquals(emptyList(), effects)
    }

    @Test
    fun skipIsIgnoredOnTheBriefingStep() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }
        val effects = mutableListOf<OnboardingContract.SideEffect>()
        backgroundScope.launch { viewModel.sideEffects.collect { effects.add(it) } }

        viewModel.onIntent(OnboardingContract.Intent.Skip)

        assertEquals(OnboardingContract.Step.BRIEFING, viewModel.state.value.currentStep)
        assertEquals(emptyList(), effects)
    }

    @Test
    fun skipOnTheHeadlinesStepFinishes() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onIntent(OnboardingContract.Intent.Continue)

        viewModel.onIntent(OnboardingContract.Intent.Skip)

        assertEquals(OnboardingContract.SideEffect.Finished, viewModel.sideEffects.first())
    }

    @Test
    fun continueOnTheLastStepFinishes() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }
        repeat(2) { viewModel.onIntent(OnboardingContract.Intent.Continue) }
        assertEquals(OnboardingContract.Step.READER, viewModel.state.value.currentStep)

        viewModel.onIntent(OnboardingContract.Intent.Continue)

        assertEquals(OnboardingContract.SideEffect.Finished, viewModel.sideEffects.first())
    }

    @Test
    fun sampleFiguresCarryCachedPortraits() = runTest(testDispatcher) {
        figureRepository.figures.value = listOf(
            Figure(id = 1, name = "Corrie ten Boom", category = FigureCategory.MISSIONARY, century = "20th", portraitUrl = "https://x/a.png"),
        )
        val viewModel = OnboardingViewModel(figureRepository)
        backgroundScope.launch { viewModel.state.collect {} }

        val figures = viewModel.state.value.sampleFigures
        assertEquals(OnboardingContract.SAMPLE_FIGURE_NAMES, figures.map { it.name })
        assertEquals("https://x/a.png", figures.first().portraitUrl)
        // Not yet synced: falls back to initials rather than dropping the reporter.
        assertNull(figures[1].portraitUrl)
    }
}

private class FakeOnboardingFigureRepository : FigureRepository {
    val figures = MutableStateFlow<List<Figure>>(emptyList())
    override fun observeAllFigures(): Flow<List<Figure>> = figures
    override fun observeFiguresByCategory(category: FigureCategory): Flow<List<Figure>> = figures
    override suspend fun getFigureById(id: Long): Figure? = null
    override suspend fun getFigureByName(name: String): Figure? = null
    override suspend fun syncFigures() = Unit
}
