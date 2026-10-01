package com.mediasage.feature.figures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.repository.epochMillis
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.EncouragementRepository
import com.mediasage.domain.repository.FigureRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class FiguresViewModel(
    private val figureRepository: FigureRepository,
    private val encouragementRepository: EncouragementRepository,
    private val dayAssignmentRepository: DayAssignmentRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _state = MutableStateFlow<FiguresContract.UiState>(FiguresContract.UiState.Loading)
    val state: StateFlow<FiguresContract.UiState> = _state.asStateFlow()

    private val _sideEffects = Channel<FiguresContract.SideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val _filter = MutableStateFlow(FiguresFilter())

    init {
        viewModelScope.launch {
            combine(
                figureRepository.observeAllFigures(),
                encouragementRepository.observeCountByFigureName(),
                dayAssignmentRepository.observeAssignments(),
                _filter
            ) { figures, counts, assignments, filter ->
                val todayFigureId = assignments[todayDayOfWeekOrdinal()]?.figureId
                val items = figures.map { it.toVoiceFigureItem(counts, todayFigureId) }
                FiguresContract.UiState.Success(
                    figures = items.filter { it.matches(filter) }.sortedWith(PINNED_FIRST_THEN_NAME),
                    searchQuery = filter.query,
                    selectedEra = filter.era
                )
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
                if (_filter.value.query.isBlank() && intent.query.isNotBlank()) {
                    analyticsService.logEvent(AnalyticsEvents.FIGURE_SEARCH)
                }
                _filter.update { it.copy(query = intent.query) }
            }
            is FiguresContract.Intent.EraSelected -> _filter.update { it.copy(era = intent.era) }
        }
    }

    private fun refresh() {
        val current = _state.value as? FiguresContract.UiState.Success ?: return
        _state.value = current.copy(isRefreshing = true)
        viewModelScope.launch {
            runCatching { figureRepository.syncFigures() }
                .onFailure { e ->
                    _sideEffects.send(FiguresContract.SideEffect.ShowError(e.message ?: "Failed to sync voices"))
                }
            val updated = _state.value as? FiguresContract.UiState.Success ?: return@launch
            _state.value = updated.copy(isRefreshing = false)
        }
    }

    private fun todayDayOfWeekOrdinal(): Int =
        Instant.fromEpochMilliseconds(epochMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.ordinal
}

/** The reader's narrowing of the Reporters tab: a search query and an era (null means All). */
private data class FiguresFilter(val query: String = "", val era: FigureEra? = null)

private val PINNED_FIRST_THEN_NAME =
    compareByDescending<VoiceFigureItem> { it.isPinned }.thenBy { it.name }

private fun Figure.toVoiceFigureItem(counts: Map<String, Int>, todayFigureId: Long?) = VoiceFigureItem(
    id = id,
    name = name,
    role = role,
    lifespan = lifespan,
    era = FigureEra.fromCentury(century),
    themes = themes,
    imageUrl = portraitUrl,
    quoteCount = counts[name] ?: 0,
    isPinned = id == todayFigureId
)

private fun VoiceFigureItem.matches(filter: FiguresFilter): Boolean =
    (filter.era == null || era == filter.era) && matchesQuery(filter.query)

private fun VoiceFigureItem.matchesQuery(query: String): Boolean =
    query.isBlank() ||
        name.contains(query, ignoreCase = true) ||
        role.contains(query, ignoreCase = true) ||
        lifespan.contains(query, ignoreCase = true) ||
        themes.any { it.contains(query, ignoreCase = true) }
