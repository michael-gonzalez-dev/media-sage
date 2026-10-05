package com.mediasage.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.domain.model.Figure
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LibrarySection
import com.mediasage.domain.model.Work
import com.mediasage.domain.repository.WorkRepository
import com.mediasage.domain.usecase.GetLibraryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    getLibrary: GetLibraryUseCase,
    private val workRepository: WorkRepository,
    private val viewSelection: LibraryViewSelection,
) : ViewModel() {

    private val input = MutableStateFlow(LibraryInput())

    val state: StateFlow<LibraryContract.UiState> =
        combine(getLibrary(), viewSelection.view, input) { sections, view, filter ->
            LibraryContract.UiState.Success(
                sections = sections.map { it.toItem() }.mapNotNull { it.filteredBy(filter) },
                view = view,
                searchQuery = filter.query,
                selectedEra = filter.era,
                selectedWork = filter.selectedWork,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LibraryContract.UiState.Loading)

    init {
        // Non-fatal: offline, the Library shows the works stored by the last sync.
        viewModelScope.launch { runCatching { workRepository.syncWorks() } }
    }

    fun onIntent(intent: LibraryContract.Intent) {
        when (intent) {
            is LibraryContract.Intent.ViewSelected -> viewSelection.select(intent.view)
            is LibraryContract.Intent.SearchQueryChanged -> input.update { it.copy(query = intent.query) }
            is LibraryContract.Intent.EraSelected -> input.update { it.copy(era = intent.era) }
            is LibraryContract.Intent.WorkSelected -> input.update { it.copy(selectedWork = intent.work) }
            is LibraryContract.Intent.WorkDismissed -> input.update { it.copy(selectedWork = null) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** What the reader has done in the Library: their search and era (null means All), and the book they opened. */
private data class LibraryInput(
    val query: String = "",
    val era: FigureEra? = null,
    val selectedWork: LibraryWorkItem? = null,
)

private fun LibrarySection.toItem(): LibrarySectionItem {
    val era = FigureEra.fromCentury(figure.century)
    return LibrarySectionItem(
        figureId = figure.id,
        reporterName = figure.name,
        portraitUrl = figure.portraitUrl,
        era = era,
        works = works.mapIndexed { index, work -> work.toItem(figure, era, index) },
    )
}

private fun Work.toItem(figure: Figure, era: FigureEra?, shelfIndex: Int) = LibraryWorkItem(
    id = id,
    title = title,
    year = year,
    recordedBy = recordedBy,
    coverUrl = coverUrl,
    reporterName = figure.name,
    reporterPortraitUrl = figure.portraitUrl,
    reporterId = figure.id,
    era = era,
    shelfIndex = shelfIndex,
)

/**
 * The shelf as the filter leaves it, or null when nothing on it matches. A search for the reporter's name keeps their
 * whole shelf; otherwise only the works whose title or recorder matches stay.
 */
private fun LibrarySectionItem.filteredBy(filter: LibraryInput): LibrarySectionItem? {
    if (filter.era != null && era != filter.era) return null
    val query = filter.query.trim()
    if (query.isEmpty() || reporterName.contains(query, ignoreCase = true)) return this
    val matching = works.filter { work ->
        work.title.contains(query, ignoreCase = true) || work.recordedBy?.contains(query, ignoreCase = true) == true
    }
    return if (matching.isEmpty()) null else copy(works = matching)
}
