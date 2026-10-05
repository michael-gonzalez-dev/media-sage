package com.mediasage.feature.figures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.analytics.figureScheduleParams
import com.mediasage.data.analytics.quoteMemorizedParams
import com.mediasage.data.repository.epochMillis
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class FigureDetailViewModel(
    private val figureId: Long,
    private val figureRepository: FigureRepository,
    private val dayAssignmentRepository: DayAssignmentRepository,
    private val dailyReflectionRepository: DailyReflectionRepository,
    private val quoteRepository: QuoteRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _state = MutableStateFlow<FigureDetailContract.UiState>(FigureDetailContract.UiState.Loading)
    val state: StateFlow<FigureDetailContract.UiState> = _state.asStateFlow()

    /** The only user selection this screen owns: the open lens picker and any reassignment awaiting confirmation. */
    private val input = MutableStateFlow(ScreenInput())

    init {
        load()
    }

    fun onIntent(intent: FigureDetailContract.Intent) {
        when (intent) {
            is FigureDetailContract.Intent.PinToHome -> handlePinToggle()
            is FigureDetailContract.Intent.LensSelected -> handleLensSelected(intent.lens)
            is FigureDetailContract.Intent.DismissLensPicker -> input.update { it.copy(isLensPickerOpen = false) }
            is FigureDetailContract.Intent.ConfirmReassignment -> handleConfirmReassignment()
            is FigureDetailContract.Intent.CancelReassignment -> input.update { it.copy(pendingReassignment = null) }
            is FigureDetailContract.Intent.PinQuote -> handlePinQuote(intent.quoteText)
        }
    }

    private fun handlePinQuote(quoteText: String) {
        viewModelScope.launch {
            quoteRepository.memorizeQuote(figureId, quoteText)
            val figureName = figureRepository.getFigureById(figureId)?.name
            val source = quoteRepository.observeQuotesByFigure(figureId).first().firstOrNull { it.text == quoteText }?.source
            analyticsService.logEvent(AnalyticsEvents.QUOTE_MEMORIZED, quoteMemorizedParams(figureId, figureName, quoteText, source))
        }
    }

    /** Unpins straight away; pinning first asks which lens the figure should brief through. */
    private fun handlePinToggle() {
        val current = _state.value as? FigureDetailContract.UiState.Success ?: return
        if (current.isPinned) {
            viewModelScope.launch { clearToday() }
            return
        }
        input.update { it.copy(isLensPickerOpen = true) }
    }

    /**
     * Guards today's locked-in briefing the same way the Reader tab's schedule does: a different figure
     * or lens than the one already briefed today needs confirmation, since it can only take effect next week.
     */
    private fun handleLensSelected(lens: LensFilter?) {
        val current = _state.value as? FigureDetailContract.UiState.Success ?: return
        input.update { it.copy(isLensPickerOpen = false) }
        val todayOrdinal = todayDayOfWeekOrdinal()
        viewModelScope.launch {
            val epochDay = todayEpochDay()
            val lockedFigureId = dailyReflectionRepository.getLockedFigureId(epochDay)
            val lockedLens = lockedFigureId?.let { dailyReflectionRepository.getLockedTheme(epochDay) }
            if (lockedFigureId == null || (lockedFigureId == figureId && lockedLens == lens)) {
                assignToday(todayOrdinal, lens)
                return@launch
            }
            val lockedFigureName = figureRepository.getFigureById(lockedFigureId)?.name ?: return@launch
            val pending = FigureDetailContract.PendingReassignment(
                todayOrdinal = todayOrdinal,
                lens = lens,
                isReporterChange = lockedFigureId != figureId,
                currentFigureName = lockedFigureName,
                newFigureName = current.figureName,
                nextWeekdayLabel = weekdayLabel(todayOrdinal),
            )
            input.update { it.copy(pendingReassignment = pending) }
        }
    }

    /** Clears the dialog immediately — the device write and its sync run in the background. */
    private fun handleConfirmReassignment() {
        val pending = input.value.pendingReassignment ?: return
        input.update { it.copy(pendingReassignment = null) }
        viewModelScope.launch { assignToday(pending.todayOrdinal, pending.lens) }
    }

    private suspend fun assignToday(todayOrdinal: Int, lens: LensFilter?) {
        dayAssignmentRepository.assign(todayOrdinal, figureId, lens)
        logScheduleEvent(AnalyticsEvents.FIGURE_PINNED, emptyMap(), todayOrdinal, lens)
    }

    /** Logged as a Reader-tab clear would be, since unpinning here empties today's weekday slot the same way. */
    private suspend fun clearToday() {
        val todayOrdinal = todayDayOfWeekOrdinal()
        val lens = dayAssignmentRepository.observeAssignments().first()[todayOrdinal]?.lens
        dayAssignmentRepository.clear(todayOrdinal)
        val action = mapOf(AnalyticsEvents.Params.ACTION to AnalyticsEvents.Values.ACTION_CLEAR)
        logScheduleEvent(AnalyticsEvents.FIGURE_DAY_ASSIGNMENT, action, todayOrdinal, lens)
    }

    private suspend fun logScheduleEvent(event: String, extraParams: Map<String, String>, dayOfWeekOrdinal: Int, lens: LensFilter?) {
        val figureName = figureRepository.getFigureById(figureId)?.name
        analyticsService.logEvent(event, extraParams + figureScheduleParams(figureId, figureName, dayOfWeekOrdinal, lens))
    }

    private fun load() {
        viewModelScope.launch {
            val figure = figureRepository.getFigureById(figureId) ?: return@launch
            combine(
                quoteRepository.observeQuotesByFigure(figure.id),
                dayAssignmentRepository.observeAssignments(),
                input,
                quoteRepository.observeMemorizedQuote(),
            ) { quotes, assignments, screenInput, memorizedQuote ->
                val todayOrdinal = todayDayOfWeekOrdinal()
                FigureDetailContract.UiState.Success(
                    figureName = figure.name,
                    figureRole = figure.role,
                    figureImageUrl = figure.portraitUrl,
                    bio = figure.bio,
                    quotes = quotes.map {
                        FigureQuoteItem(
                            quoteText = it.text,
                            source = it.source,
                            isPinned = memorizedQuote?.figureId == figure.id && memorizedQuote.text == it.text,
                        )
                    },
                    isPinned = assignments[todayOrdinal]?.figureId == figureId,
                    isLensPickerOpen = screenInput.isLensPickerOpen,
                    pendingReassignment = screenInput.pendingReassignment,
                )
            }.collect { _state.value = it }
        }
    }

    private fun weekdayLabel(dayOfWeekOrdinal: Int): String =
        DayOfWeek.entries[dayOfWeekOrdinal].name.lowercase().replaceFirstChar { it.uppercase() }

    private fun todayEpochDay(): Long =
        Instant.fromEpochMilliseconds(epochMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toLong()

    private fun todayDayOfWeekOrdinal(): Int =
        Instant.fromEpochMilliseconds(epochMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.ordinal

    private data class ScreenInput(
        val isLensPickerOpen: Boolean = false,
        val pendingReassignment: FigureDetailContract.PendingReassignment? = null,
    )
}
