package com.mediasage.feature.figures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.ReporterView
import com.mediasage.data.ReporterViewPreferencesRepository
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.repository.epochMillis
import com.mediasage.domain.model.DayAssignment
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.FigureRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class FiguresViewModel(
    private val figureRepository: FigureRepository,
    private val dayAssignmentRepository: DayAssignmentRepository,
    private val dailyReflectionRepository: DailyReflectionRepository,
    private val reporterViewPreferences: ReporterViewPreferencesRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _state = MutableStateFlow<FiguresContract.UiState>(FiguresContract.UiState.Loading)
    val state: StateFlow<FiguresContract.UiState> = _state.asStateFlow()

    private val _sideEffects = Channel<FiguresContract.SideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val input = MutableStateFlow(FiguresInput())

    init {
        viewModelScope.launch {
            val epochDay = todayEpochDay()
            combine(
                figureRepository.observeAllFigures(),
                dayAssignmentRepository.observeAssignments(),
                dailyReflectionRepository.observeByEpochDayRange(epochDay, epochDay),
                input,
                reporterViewPreferences.reporterView,
            ) { figures, assignments, todaysBriefings, screenInput, view ->
                // A briefing already written today locks its reporter and lens until tomorrow.
                val lockedFigureId = todaysBriefings.firstOrNull()?.figureId
                val schedule = TodaySchedule(
                    lockedFigureId = lockedFigureId,
                    lockedLens = lockedFigureId?.let { dailyReflectionRepository.getLockedTheme(epochDay) ?: LensFilter.NEWS },
                    assignment = assignments[todayDayOfWeekOrdinal()],
                )
                buildState(figures, schedule, screenInput, view)
            }.collect { state ->
                _state.value = state
            }
        }
    }

    fun onIntent(intent: FiguresContract.Intent) {
        when (intent) {
            is FiguresContract.Intent.LoadFigures -> { /* reactive — no manual reload needed */ }
            is FiguresContract.Intent.Refresh -> refresh()
            is FiguresContract.Intent.FigureClicked -> { /* handled via navigation callback */ }
            is FiguresContract.Intent.SearchQueryChanged -> {
                if (input.value.query.isBlank() && intent.query.isNotBlank()) {
                    analyticsService.logEvent(AnalyticsEvents.FIGURE_SEARCH)
                }
                input.update { it.copy(query = intent.query) }
            }
            is FiguresContract.Intent.EraSelected -> input.update { it.copy(era = intent.era) }
            is FiguresContract.Intent.ViewSelected -> viewModelScope.launch { reporterViewPreferences.setReporterView(intent.view) }
            is FiguresContract.Intent.LensSelected -> selectLens(intent.figureId, intent.lens)
            is FiguresContract.Intent.ConfirmReassignment -> confirmReassignment()
            is FiguresContract.Intent.CancelReassignment -> input.update { it.copy(pendingReassignment = null) }
        }
    }

    private fun refresh() {
        if (_state.value !is FiguresContract.UiState.Success) return
        input.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            runCatching { figureRepository.syncFigures() }
                .onFailure { e ->
                    _sideEffects.send(FiguresContract.SideEffect.ShowError(e.message ?: "Failed to sync voices"))
                }
            input.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * The same rule as pinning from a reporter's page: with no briefing yet today, the pick applies at once. Once today's
     * briefing is written, a different reporter or lens needs confirmation, since it can only start next week.
     */
    private fun selectLens(figureId: Long, lens: LensFilter) {
        viewModelScope.launch {
            val epochDay = todayEpochDay()
            val lockedFigureId = dailyReflectionRepository.getLockedFigureId(epochDay)
            val lockedLens = lockedFigureId?.let { dailyReflectionRepository.getLockedTheme(epochDay) ?: LensFilter.NEWS }
            if (lockedFigureId == null || (lockedFigureId == figureId && lockedLens == lens)) {
                assignToday(figureId, lens)
                return@launch
            }
            val currentName = figureRepository.getFigureById(lockedFigureId)?.name ?: return@launch
            val newName = figureRepository.getFigureById(figureId)?.name ?: return@launch
            val todayOrdinal = todayDayOfWeekOrdinal()
            val pending = FigureDetailContract.PendingReassignment(
                todayOrdinal = todayOrdinal,
                lens = lens,
                isReporterChange = lockedFigureId != figureId,
                currentFigureName = currentName,
                newFigureName = newName,
                nextWeekdayLabel = weekdayLabel(todayOrdinal),
            )
            input.update { it.copy(pendingReassignment = pending, pendingFigureId = figureId) }
        }
    }

    /** Clears the dialog at once; the write and its sync run in the background. */
    private fun confirmReassignment() {
        val pending = input.value.pendingReassignment ?: return
        val figureId = input.value.pendingFigureId ?: return
        input.update { it.copy(pendingReassignment = null, pendingFigureId = null) }
        viewModelScope.launch { assignToday(figureId, pending.lens ?: LensFilter.NEWS) }
    }

    private suspend fun assignToday(figureId: Long, lens: LensFilter) {
        dayAssignmentRepository.assign(todayDayOfWeekOrdinal(), figureId, lens)
        analyticsService.logEvent(AnalyticsEvents.FIGURE_PINNED, mapOf(AnalyticsEvents.Params.FIGURE_ID to figureId.toString()))
    }

    private fun buildState(
        figures: List<Figure>,
        schedule: TodaySchedule,
        screenInput: FiguresInput,
        view: ReporterView,
    ): FiguresContract.UiState.Success {
        val todayPick = todayPick(figures, schedule)
        val items = figures.map { it.toVoiceFigureItem(isPinned = it.id == todayPick?.figureId) }
        return FiguresContract.UiState.Success(
            figures = items.filter { it.matches(screenInput) }.sortedWith(PINNED_FIRST_THEN_NAME),
            searchQuery = screenInput.query,
            selectedEra = screenInput.era,
            isRefreshing = screenInput.isRefreshing,
            view = view,
            todayPick = todayPick,
            scheduledPick = scheduledPick(figures, schedule, todayPick),
            pendingReassignment = screenInput.pendingReassignment,
        )
    }

    /**
     * Matches the briefing: today's locked reporter, else today's weekday assignment, else (unassigned, or a reporter
     * since disabled) the first reporter. The lens is the locked one once today's briefing exists, and News by default.
     */
    private fun todayPick(figures: List<Figure>, schedule: TodaySchedule): FiguresContract.ReporterPick? {
        val figureId = (schedule.lockedFigureId ?: schedule.assignment?.figureId)
            ?.takeIf { id -> figures.any { it.id == id } }
            ?: figures.firstOrNull()?.id
            ?: return null
        val lens = if (schedule.lockedFigureId != null) schedule.lockedLens else schedule.assignment?.lens
        return FiguresContract.ReporterPick(figureId, lens ?: LensFilter.NEWS)
    }

    /** A different reporter assigned to today's weekday after today's briefing was written: they start next week. */
    private fun scheduledPick(
        figures: List<Figure>,
        schedule: TodaySchedule,
        todayPick: FiguresContract.ReporterPick?,
    ): FiguresContract.ScheduledPick? {
        val assignment = schedule.assignment ?: return null
        val isLaterChange = schedule.lockedFigureId != null && assignment.figureId != todayPick?.figureId
        if (!isLaterChange || figures.none { it.id == assignment.figureId }) return null
        return FiguresContract.ScheduledPick(assignment.figureId, assignment.lens ?: LensFilter.NEWS, weekdayLabel(todayDayOfWeekOrdinal()))
    }

    private fun weekdayLabel(dayOfWeekOrdinal: Int): String =
        DayOfWeek.entries[dayOfWeekOrdinal].name.lowercase().replaceFirstChar { it.uppercase() }

    private fun today(): LocalDate =
        Instant.fromEpochMilliseconds(epochMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun todayEpochDay(): Long = today().toEpochDays().toLong()

    private fun todayDayOfWeekOrdinal(): Int = today().dayOfWeek.ordinal
}

/**
 * What the reader has done on the Reporters tab: their search and era (null means All), a pull to refresh in flight,
 * and a pick awaiting confirmation. [pendingFigureId] is the reporter that pick is for.
 */
private data class FiguresInput(
    val query: String = "",
    val era: FigureEra? = null,
    val isRefreshing: Boolean = false,
    val pendingReassignment: FigureDetailContract.PendingReassignment? = null,
    val pendingFigureId: Long? = null,
)

/** Today's locked briefing (if one has been written) and today's weekday assignment. */
private data class TodaySchedule(val lockedFigureId: Long?, val lockedLens: LensFilter?, val assignment: DayAssignment?)

private val PINNED_FIRST_THEN_NAME =
    compareByDescending<VoiceFigureItem> { it.isPinned }.thenBy { it.name }

private fun Figure.toVoiceFigureItem(isPinned: Boolean) = VoiceFigureItem(
    id = id,
    name = name,
    role = role,
    lifespan = lifespan,
    era = FigureEra.fromCentury(century),
    themes = themes,
    imageUrl = portraitUrl,
    isPinned = isPinned,
    knownFor = knownFor,
)

private fun VoiceFigureItem.matches(filter: FiguresInput): Boolean =
    (filter.era == null || era == filter.era) && matchesQuery(filter.query)

private fun VoiceFigureItem.matchesQuery(query: String): Boolean =
    query.isBlank() ||
        name.contains(query, ignoreCase = true) ||
        role.contains(query, ignoreCase = true) ||
        lifespan.contains(query, ignoreCase = true) ||
        themes.any { it.contains(query, ignoreCase = true) }
