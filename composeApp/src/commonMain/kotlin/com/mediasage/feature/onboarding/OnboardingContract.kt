package com.mediasage.feature.onboarding

import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter
import com.mediasage.feature.figures.ReporterCardInfo

object OnboardingContract {

    // Names match the figures table so cached portraits resolve. The briefing reporter comes from the
    // reader's roster, while the headline reporter is deliberately outside it: a headline's quote is the
    // best match from across the whole library, not from the reporters the reader picked.
    const val BRIEFING_REPORTER = "C.S. Lewis"
    const val HEADLINE_REPORTER = "Corrie ten Boom" // the sample quote is theirs
    val ROSTER_REPORTERS = listOf("Mother Teresa", "Watchman Nee", BRIEFING_REPORTER)
    val SAMPLE_FIGURE_NAMES = listOf(HEADLINE_REPORTER) + ROSTER_REPORTERS

    /**
     * The steps in order. Each step's content is its own composable, so one step (e.g. the
     * briefing step becoming a reporter-and-lens pick) can be replaced without touching the
     * others, the progress indicator, or the onboarding gate.
     */
    enum class Step(val skippable: Boolean) {
        BRIEFING(skippable = false),
        HEADLINES(skippable = true),
        READER(skippable = true),

        // Skip on the earlier steps leads here, so every reader sees the pick.
        PICK(skippable = false),
    }

    /** A reporter in the pick step's deck. [era] is null when the century can't be placed, so it shows only under All. */
    data class PickReporter(val card: ReporterCardInfo, val era: FigureEra?)

    /** Today's reporter and the lens they brief through. */
    data class PickSelection(val reporterId: Long, val lens: LensFilter)

    /** A real reporter shown in the step animations; [portraitUrl] is null until figures have synced. */
    data class SampleFigure(val name: String, val portraitUrl: String? = null)

    data class UiState(
        val steps: List<Step> = Step.entries,
        val currentIndex: Int = 0,
        // Every reporter the steps show, in SAMPLE_FIGURE_NAMES order.
        val sampleFigures: List<SampleFigure> = SAMPLE_FIGURE_NAMES.map { SampleFigure(it) },
        // Every reporter, by name; the era chips narrow it to [deck].
        val reporters: List<PickReporter> = emptyList(),
        val selectedEra: FigureEra? = null,
        // Null until today's schedule has loaded, so a default that may still change is never shown.
        val selection: PickSelection? = null,
    ) {
        val currentStep: Step get() = steps[currentIndex]
        val isFirstStep: Boolean get() = currentIndex == 0
        val isLastStep: Boolean get() = currentIndex == steps.lastIndex
        val briefingFigure: SampleFigure get() = figureNamed(BRIEFING_REPORTER)
        val headlineFigure: SampleFigure get() = figureNamed(HEADLINE_REPORTER)
        val rosterFigures: List<SampleFigure> get() = ROSTER_REPORTERS.map(::figureNamed)
        val deck: List<PickReporter> get() = reporters.filter { selectedEra == null || it.era == selectedEra }
        val selectedReporter: PickReporter? get() = reporters.firstOrNull { it.card.id == selection?.reporterId }

        private fun figureNamed(name: String): SampleFigure = sampleFigures.first { it.name == name }
    }

    sealed interface Intent {
        data object Continue : Intent
        data object Back : Intent
        data object Skip : Intent
        /** A swipe settled on the step at [index]. */
        data class GoToStep(val index: Int) : Intent
        data class SelectEra(val era: FigureEra?) : Intent
        data class SelectReporter(val reporterId: Long, val lens: LensFilter) : Intent
    }

    sealed interface SideEffect {
        /** Finished the pick step: open the app on today's briefing. */
        data object Finished : SideEffect
    }
}
