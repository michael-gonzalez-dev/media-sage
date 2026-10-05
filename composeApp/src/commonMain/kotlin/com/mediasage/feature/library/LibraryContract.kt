package com.mediasage.feature.library

import com.mediasage.domain.model.FigureEra

object LibraryContract {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            /** One shelf per reporter with works matching the search and era, in name order. Empty until the first sync. */
            val sections: List<LibrarySectionItem>,
            val view: LibraryView = LibraryView.SHELF,
            val searchQuery: String = "",
            /** Null means All. */
            val selectedEra: FigureEra? = null,
            /** The book whose details sheet is open. */
            val selectedWork: LibraryWorkItem? = null,
        ) : UiState {
            val isFiltered: Boolean get() = searchQuery.isNotBlank() || selectedEra != null
        }
    }

    sealed interface Intent {
        data class ViewSelected(val view: LibraryView) : Intent
        data class SearchQueryChanged(val query: String) : Intent
        data class EraSelected(val era: FigureEra?) : Intent
        data class WorkSelected(val work: LibraryWorkItem) : Intent
        data object WorkDismissed : Intent
    }
}

enum class LibraryView { SHELF, LIST }

data class LibrarySectionItem(
    val figureId: Long,
    val reporterName: String,
    val portraitUrl: String?,
    val era: FigureEra?,
    val works: List<LibraryWorkItem>,
)

data class LibraryWorkItem(
    val id: Long,
    val title: String,
    val year: Int?,
    /** Set for a book someone else wrote that preserves the reporter's words. */
    val recordedBy: String?,
    val coverUrl: String?,
    /** Printed on the default cover. */
    val reporterName: String,
    val reporterPortraitUrl: String? = null,
    /** The reporter's figure id; with [shelfIndex] it picks the default cover's color. */
    val reporterId: Long = 0,
    /** The reporter's era, which sets the default cover's period binding. */
    val era: FigureEra? = null,
    /** The book's place on its reporter's full shelf, before any search, so neighbors take different cover colors. */
    val shelfIndex: Int = 0,
)
