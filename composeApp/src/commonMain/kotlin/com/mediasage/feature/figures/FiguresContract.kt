package com.mediasage.feature.figures

import com.mediasage.domain.model.FigureEra

object FiguresContract {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val figures: List<VoiceFigureItem>,
            val searchQuery: String = "",
            /** Null means All. */
            val selectedEra: FigureEra? = null,
            val isRefreshing: Boolean = false
        ) : UiState
    }

    sealed interface Intent {
        data object LoadFigures : Intent
        data object Refresh : Intent
        data class FigureClicked(val figureId: Long) : Intent
        data class SearchQueryChanged(val query: String) : Intent
        data class EraSelected(val era: FigureEra?) : Intent
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
    val isPinned: Boolean = false
)
