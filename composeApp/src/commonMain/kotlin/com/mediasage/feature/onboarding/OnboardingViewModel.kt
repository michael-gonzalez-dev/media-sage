package com.mediasage.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.repository.epochMillis
import com.mediasage.domain.model.DayAssignment
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.feature.figures.ReporterCardInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** What the reader has done on the onboarding screen; the UI state is derived from it plus the live figures and schedule. */
private data class OnboardingInput(
    val currentIndex: Int = 0,
    val selectedEra: FigureEra? = null,
    // The reader's own pick, kept apart from the schedule so a schedule that loads late never replaces it.
    val pick: OnboardingContract.PickSelection? = null,
)

class OnboardingViewModel(
    figureRepository: FigureRepository,
    private val dayAssignmentRepository: DayAssignmentRepository,
    private val todayDayOfWeek: () -> Int = ::localDayOfWeekOrdinal,
) : ViewModel() {

    private val input = MutableStateFlow(OnboardingInput())

    // Null until the schedule has settled, so the step never shows a default that is about to change.
    private val schedule = combine(
        dayAssignmentRepository.observeAssignments(),
        dayAssignmentRepository.isResolved,
    ) { assignments, resolved -> assignments.takeIf { resolved } }

    val state: StateFlow<OnboardingContract.UiState> = combine(
        input,
        figureRepository.observeAllFigures(),
        schedule,
    ) { userInput, figures, assignments ->
        val portraitsByName = figures.associate { it.name to it.portraitUrl }
        OnboardingContract.UiState(
            currentIndex = userInput.currentIndex,
            sampleFigures = OnboardingContract.SAMPLE_FIGURE_NAMES.map { name ->
                OnboardingContract.SampleFigure(name = name, portraitUrl = portraitsByName[name])
            },
            reporters = figures.sortedBy { it.name }.map { it.toPickReporter() },
            selectedEra = userInput.selectedEra,
            selection = assignments?.let { userInput.pick ?: scheduledSelection(it, figures) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingContract.UiState())

    private val _sideEffects = Channel<OnboardingContract.SideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private var finishJob: Job? = null

    fun onIntent(intent: OnboardingContract.Intent) {
        // Step navigation reads the input directly rather than state.value, which only stays current while the screen collects it.
        val current = OnboardingContract.UiState(currentIndex = input.value.currentIndex)
        when (intent) {
            is OnboardingContract.Intent.Continue ->
                if (current.isLastStep) finish() else moveTo(current.currentIndex + 1)
            // Going back from the first step stays put — it must never drop the reader into the main tabs.
            is OnboardingContract.Intent.Back ->
                if (!current.isFirstStep) moveTo(current.currentIndex - 1)
            is OnboardingContract.Intent.Skip ->
                if (current.currentStep.skippable) moveTo(current.steps.indexOf(OnboardingContract.Step.PICK))
            is OnboardingContract.Intent.GoToStep -> moveTo(intent.index.coerceIn(current.steps.indices))
            is OnboardingContract.Intent.SelectEra -> input.update { it.copy(selectedEra = intent.era) }
            is OnboardingContract.Intent.SelectReporter ->
                // Before the schedule settles there is no selection to change.
                if (dayAssignmentRepository.isResolved.value) {
                    input.update { it.copy(pick = OnboardingContract.PickSelection(intent.reporterId, intent.lens)) }
                }
        }
    }

    private fun moveTo(index: Int) {
        input.update { it.copy(currentIndex = index) }
    }

    /**
     * Saves the reader's pick as today's weekday, then opens the app. The save finishes first, so today's briefing is
     * written by the picked reporter. Nothing is saved when the reader kept the default, so a default shown from an
     * earlier schedule can never overwrite the real one.
     */
    private fun finish() {
        if (finishJob != null || !dayAssignmentRepository.isResolved.value) return
        val pick = input.value.pick
        finishJob = viewModelScope.launch {
            pick?.let { dayAssignmentRepository.assign(todayDayOfWeek(), it.reporterId, it.lens) }
            _sideEffects.send(OnboardingContract.SideEffect.Finished)
        }
    }

    // Matches the briefing: an unassigned day, or one assigned to a reporter since disabled, falls back to the first
    // figure, and no lens means News.
    private fun scheduledSelection(assignments: Map<Int, DayAssignment>, figures: List<Figure>): OnboardingContract.PickSelection? {
        val today = assignments[todayDayOfWeek()]
        val reporterId = today?.figureId?.takeIf { id -> figures.any { it.id == id } } ?: figures.firstOrNull()?.id ?: return null
        return OnboardingContract.PickSelection(reporterId, today?.lens ?: LensFilter.NEWS)
    }
}

private fun Figure.toPickReporter() = OnboardingContract.PickReporter(
    card = ReporterCardInfo(id = id, name = name, role = role, lifespan = lifespan, knownFor = knownFor, portraitUrl = portraitUrl),
    era = FigureEra.fromCentury(century),
)

private fun localDayOfWeekOrdinal(): Int =
    Instant.fromEpochMilliseconds(epochMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.ordinal
