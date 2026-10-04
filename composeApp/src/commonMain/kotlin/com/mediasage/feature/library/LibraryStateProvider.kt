package com.mediasage.feature.library

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.domain.model.FigureEra

internal class LibraryStateProvider : PreviewParameterProvider<LibraryContract.UiState> {
    override val values = sequenceOf(
        LibraryShelfState,
        LibraryShelfState.copy(view = LibraryView.LIST),
        LibraryContract.UiState.Success(sections = emptyList()),
        LibraryContract.UiState.Success(sections = emptyList(), searchQuery = "Kempis"),
        LibraryShelfState.copy(selectedWork = LibraryShelfState.sections.last().works.first()),
        LibraryContract.UiState.Loading,
    )
}

private fun work(id: Long, title: String, year: Int?, reporter: String, recordedBy: String? = null) =
    LibraryWorkItem(id = id, title = title, year = year, recordedBy = recordedBy, coverUrl = null, reporterName = reporter)

/** Three reporters' shelves: undated works, dated works, and books that record a reporter's words. */
internal val LibraryShelfState = LibraryContract.UiState.Success(
    sections = listOf(
        LibrarySectionItem(
            figureId = 19,
            reporterName = "A.W. Tozer",
            portraitUrl = null,
            era = FigureEra.MODERN,
            works = listOf(
                work(19003, "The Pursuit of God", 1948, "A.W. Tozer"),
                work(19008, "The Knowledge of the Holy", 1961, "A.W. Tozer"),
            ),
        ),
        LibrarySectionItem(
            figureId = 36,
            reporterName = "Augustine of Hippo",
            portraitUrl = null,
            era = FigureEra.EARLY_CHURCH,
            works = listOf(
                work(36001, "Confessions", null, "Augustine of Hippo"),
                work(36002, "The City of God", null, "Augustine of Hippo"),
            ),
        ),
        LibrarySectionItem(
            figureId = 53,
            reporterName = "Harriet Tubman",
            portraitUrl = null,
            era = FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS,
            works = listOf(
                work(53901, "Scenes in the Life of Harriet Tubman", 1869, "Harriet Tubman", recordedBy = "Sarah Bradford"),
                work(53903, "Harriet, the Moses of Her People", 1901, "Harriet Tubman", recordedBy = "Sarah Bradford"),
            ),
        ),
    ),
)
