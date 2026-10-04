package com.mediasage.feature.figures

import com.mediasage.data.ReporterView
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter

object FiguresContract {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            /** Today's pick first, then by name: the grid's order. */
            val figures: List<VoiceFigureItem>,
            val searchQuery: String = "",
            /** Null means All. */
            val selectedEra: FigureEra? = null,
            val isRefreshing: Boolean = false,
            val view: ReporterView = ReporterView.DECK,
            /** The reporter and lens of today's briefing. Null only when there are no reporters at all. */
            val todayPick: ReporterPick? = null,
            /** A different reporter picked for today's weekday after today's briefing was written, so it starts next week. */
            val scheduledPick: ScheduledPick? = null,
            val pendingReassignment: FigureDetailContract.PendingReassignment? = null,
        ) : UiState {
            /** By name, so picking a reporter never reorders the cards under the reader's finger. */
            val deck: List<VoiceFigureItem> get() = figures.sortedBy { it.name }
        }
    }

    data class ReporterPick(val figureId: Long, val lens: LensFilter)

    /** [weekdayLabel] is the weekday the pick starts on, e.g. "Wednesday". */
    data class ScheduledPick(val figureId: Long, val lens: LensFilter, val weekdayLabel: String)

    sealed interface Intent {
        data object LoadFigures : Intent
        data object Refresh : Intent
        data class FigureClicked(val figureId: Long) : Intent
        data class SearchQueryChanged(val query: String) : Intent
        data class EraSelected(val era: FigureEra?) : Intent
        data class ViewSelected(val view: ReporterView) : Intent
        /** A lens badge tapped on a card's back: make that reporter and lens today's pick. */
        data class LensSelected(val figureId: Long, val lens: LensFilter) : Intent
        data object ConfirmReassignment : Intent
        data object CancelReassignment : Intent
    }

    sealed interface SideEffect {
        data class ShowError(val message: String) : SideEffect
    }
}

data class VoiceFigureItem(
    val id: Long,
    val name: String,
    val role: String,
    val lifespan: String,
    val era: FigureEra? = null,
    val themes: List<String> = emptyList(),
    val imageUrl: String?,
    /** Today's pick: shown with an amber border. */
    val isPinned: Boolean = false,
    val knownFor: String = "",
)
