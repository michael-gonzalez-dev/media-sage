package com.mediasage.feature.figures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.domain.model.FigureEra

internal class FiguresStateProvider : PreviewParameterProvider<FiguresContract.UiState> {
    override val values = sequenceOf(
        FiguresContract.UiState.Loading,
        FiguresContract.UiState.Success(figures = emptyList()),
        FiguresContract.UiState.Success(figures = emptyList(), searchQuery = "zz", selectedEra = FigureEra.MIDDLE_AGES),
        FiguresContract.UiState.Success(
            figures = listOf(
                VoiceFigureItem(
                    id = 1L,
                    name = "C.S. Lewis",
                    era = FigureEra.MODERN,
                    role = "Author & Apologist",
                    lifespan = "1898–1963",
                    themes = listOf("Faith", "Reason"),
                    imageUrl = null,
                    isPinned = true
                ),
                VoiceFigureItem(
                    id = 2L,
                    name = "Dietrich Bonhoeffer",
                    era = FigureEra.MODERN,
                    role = "Theologian & Martyr",
                    lifespan = "1898–1963",
                    themes = listOf("Justice", "Discipleship", "Grace"),
                    imageUrl = null
                ),
                VoiceFigureItem(
                    id = 3L,
                    name = "Martin Luther King Jr.",
                    era = FigureEra.MODERN,
                    role = "Pastor & Civil Rights Leader",
                    lifespan = "1898–1963",
                    themes = listOf("Justice", "Hope"),
                    imageUrl = null
                ),
                VoiceFigureItem(
                    id = 4L,
                    name = "Julian of Norwich",
                    era = FigureEra.MIDDLE_AGES,
                    role = "Mystic & Theologian",
                    lifespan = "1347–1380",
                    themes = listOf("Love", "Contemplation"),
                    imageUrl = null
                ),
            )
        ),
        AugustineFiguresState,
    )
}

private const val PORTRAITS = "https://sfocythzurfdpkegjlsl.supabase.co/storage/v1/object/public/portraits"

/**
 * The "All" list as the app orders it, used for the thecouragepost.app screenshots. Today's
 * reporter (Augustine) is pinned first and the rest follow alphabetically.
 */
internal val AugustineFiguresState = FiguresContract.UiState.Success(
    figures = listOf(
        sampleFigure(36L, "Augustine of Hippo", "Bishop & Church Father", "354-430", FigureEra.EARLY_CHURCH, isPinned = true),
        sampleFigure(19L, "A.W. Tozer", "Pastor & Author", "1897-1963", FigureEra.MODERN),
        sampleFigure(57L, "Abraham Lincoln", "President & Statesman", "1809-1865", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS),
        sampleFigure(88L, "Adoniram Judson", "Missionary to Burma", "1788-1850", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS),
    ),
)

private fun sampleFigure(
    id: Long,
    name: String,
    role: String,
    lifespan: String,
    era: FigureEra,
    isPinned: Boolean = false,
) = VoiceFigureItem(
    id = id,
    name = name,
    era = era,
    role = role,
    lifespan = lifespan,
    imageUrl = "$PORTRAITS/$id.webp",
    isPinned = isPinned,
)
