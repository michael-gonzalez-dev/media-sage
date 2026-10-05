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

private data class SampleWork(val id: Long, val title: String, val year: Int?, val recordedBy: String? = null)

private fun shelf(figureId: Long, reporter: String, era: FigureEra, vararg works: SampleWork) = LibrarySectionItem(
    figureId = figureId,
    reporterName = reporter,
    portraitUrl = null,
    era = era,
    works = works.mapIndexed { index, work ->
        LibraryWorkItem(
            id = work.id,
            title = work.title,
            year = work.year,
            recordedBy = work.recordedBy,
            coverUrl = null,
            reporterName = reporter,
            reporterId = figureId,
            era = era,
            shelfIndex = index,
        )
    },
)

/** One reporter from every era, so each period binding shows (Tozer's five, every modern layout); Tubman's books record her words. */
internal val LibraryShelfState = LibraryContract.UiState.Success(
    sections = listOf(
        shelf(
            19, "A.W. Tozer", FigureEra.MODERN,
            SampleWork(19001, "Wingspread", 1943),
            SampleWork(19002, "Let My People Go", 1947),
            SampleWork(19003, "The Pursuit of God", 1948),
            SampleWork(19004, "The Divine Conquest", 1950),
            SampleWork(19008, "The Knowledge of the Holy", 1961),
        ),
        shelf(
            36, "Augustine of Hippo", FigureEra.EARLY_CHURCH,
            SampleWork(36001, "Confessions", null),
            SampleWork(36002, "The City of God", null),
        ),
        shelf(
            8, "Jonathan Edwards", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS,
            SampleWork(8001, "A Treatise Concerning Religious Affections", 1746),
            SampleWork(8002, "Freedom of the Will", 1754),
        ),
        shelf(
            1, "Martin Luther", FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS,
            SampleWork(1001, "On the Freedom of a Christian", 1520),
            SampleWork(1002, "The Bondage of the Will", 1525),
        ),
        shelf(
            40, "Thomas à Kempis", FigureEra.MIDDLE_AGES,
            SampleWork(40001, "The Imitation of Christ", null),
        ),
        shelf(
            53, "Harriet Tubman", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS,
            SampleWork(53901, "Scenes in the Life of Harriet Tubman", 1869, recordedBy = "Sarah Bradford"),
            SampleWork(53903, "Harriet, the Moses of Her People", 1901, recordedBy = "Sarah Bradford"),
        ),
    ),
)
