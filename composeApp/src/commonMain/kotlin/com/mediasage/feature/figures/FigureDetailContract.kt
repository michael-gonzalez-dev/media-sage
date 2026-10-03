package com.mediasage.feature.figures

import com.mediasage.domain.model.LensFilter

object FigureDetailContract {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val figureName: String,
            val figureRole: String,
            val figureImageUrl: String?,
            val bio: String?,
            val quotes: List<FigureQuoteItem>,
            val isPinned: Boolean = false,
            val isLensPickerOpen: Boolean = false,
            val pendingReassignment: PendingReassignment? = null,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    /**
     * Awaiting user confirmation to schedule this figure and [lens] for today's weekday from next week,
     * because today already has a briefing. [isReporterChange] is false when only the lens changes.
     */
    data class PendingReassignment(
        val todayOrdinal: Int,
        val lens: LensFilter?,
        val isReporterChange: Boolean,
        val currentFigureName: String,
        val newFigureName: String,
        val nextWeekdayLabel: String,
    )

    sealed interface Intent {
        data object PinToHome : Intent
        data class LensSelected(val lens: LensFilter?) : Intent
        data object DismissLensPicker : Intent
        data object ConfirmReassignment : Intent
        data object CancelReassignment : Intent
        data class PinQuote(val quoteText: String) : Intent
    }
}

data class FigureQuoteItem(
    val quoteText: String,
    /** The work and year the quote comes from. */
    val source: String,
    val isPinned: Boolean = false,
)
