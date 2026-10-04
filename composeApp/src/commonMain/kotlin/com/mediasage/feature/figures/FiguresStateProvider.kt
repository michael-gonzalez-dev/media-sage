package com.mediasage.feature.figures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.data.ReporterView
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter

internal class FiguresStateProvider : PreviewParameterProvider<FiguresContract.UiState> {
    override val values = sequenceOf(
        FiguresContract.UiState.Loading,
        FiguresContract.UiState.Success(figures = emptyList()),
        FiguresContract.UiState.Success(figures = emptyList(), searchQuery = "zz", selectedEra = FigureEra.MIDDLE_AGES),
        FiguresContract.UiState.Success(
            view = ReporterView.GRID,
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
        AugustineFiguresState.copy(view = ReporterView.GRID),
        // A different reporter picked after today's briefing was written: they carry "Wednesday's pick".
        AugustineFiguresState.copy(scheduledPick = FiguresContract.ScheduledPick(19L, LensFilter.HOPE, "Wednesday")),
    )
}

private const val PORTRAITS = "https://sfocythzurfdpkegjlsl.supabase.co/storage/v1/object/public/portraits"

/**
 * The "All" deck opened on today's reporter (Augustine), used for the thecouragepost.app screenshots. The grid order
 * pins Augustine first and the rest follow alphabetically. The "known for" lines match the figures table.
 */
internal val AugustineFiguresState = FiguresContract.UiState.Success(
    figures = listOf(
        sampleFigure(
            36L, "Augustine of Hippo", "Bishop & Church Father", "354-430", FigureEra.EARLY_CHURCH,
            "Augustine wrote Confessions and The City of God, and his teaching on sin and grace shaped both Catholic and Protestant thought.",
            isPinned = true,
        ),
        sampleFigure(
            19L, "A.W. Tozer", "Pastor & Author", "1897-1963", FigureEra.MODERN,
            "Tozer wrote The Pursuit of God and The Knowledge of the Holy, which called evangelicals back to a deep " +
                "personal hunger for God.",
        ),
        sampleFigure(
            57L, "Abraham Lincoln", "President & Statesman", "1809-1865", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS,
            "Lincoln led the Union through the Civil War and pushed through the 13th Amendment, which ended slavery in the United States.",
        ),
        sampleFigure(
            88L, "Adoniram Judson", "Missionary to Burma", "1788-1850", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS,
            "Judson translated the whole Bible into Burmese, a translation still used by Myanmar's Christians today.",
        ),
        sampleFigure(
            31L, "Bernard of Clairvaux", "Abbot & Theologian", "1090-1153", FigureEra.MIDDLE_AGES,
            "Bernard preached sermons on the Song of Songs and the love of Christ that shaped medieval devotion and were " +
                "admired by Luther and Calvin.",
        ),
        sampleFigure(
            67L, "Blaise Pascal", "Mathematician & Philosopher", "1623-1662", FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS,
            "Pascal helped found probability theory, and his Pensées remain a classic defense of Christian faith.",
        ),
        sampleFigure(
            4L, "Charles Spurgeon", "Preacher & Pastor", "1834-1892", FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS,
            "Spurgeon preached at London's Metropolitan Tabernacle for decades, and his sermons were printed weekly and " +
                "read around the world.",
        ),
    ),
    todayPick = FiguresContract.ReporterPick(36L, LensFilter.NEWS),
)

private fun sampleFigure(
    id: Long,
    name: String,
    role: String,
    lifespan: String,
    era: FigureEra,
    knownFor: String,
    isPinned: Boolean = false,
) = VoiceFigureItem(
    id = id,
    name = name,
    era = era,
    role = role,
    lifespan = lifespan,
    imageUrl = "$PORTRAITS/$id.webp",
    isPinned = isPinned,
    knownFor = knownFor,
)
