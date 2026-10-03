package com.mediasage.feature.briefing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.ui.ErrorType

internal class BriefingUiStateProvider : PreviewParameterProvider<BriefingContract.UiState> {
    override val values = sequenceOf(
        BriefingContract.UiState.Loading(todayLabel = "Friday, June 5, 2026"),
        BriefingContract.UiState.Success(
            todayLabel = "Friday, June 5, 2026",
            card = BriefingContract.CardState.LoadingWithFigure(
                figureId = 1L,
                figureName = "C.S. Lewis",
                figureImageUrl = null,
                theme = "Faith"
            )
        ),
        BriefingContract.UiState.Success(
            todayLabel = "Friday, June 5, 2026",
            card = BriefingContract.CardState.Ready(
                figureId = 1L,
                figureName = "C.S. Lewis",
                figureImageUrl = null,
                scriptureReference = "Romans 8:28",
                scriptureText = "And we know that in all things God works for the good of those who love him.",
                insight = "Even setbacks are woven into a larger, purposeful story.",
                implication = "Trust that today's difficulty is not the whole story.",
                inspiration = "Hardships often prepare ordinary people for an extraordinary destiny.",
                sources = listOf("Schools Nationwide Integrate Compassion Into Core Curriculum"),
                tone = "Encouraging",
                theme = "Faith"
            )
        ),
        AugustineBriefingState,
        BriefingContract.UiState.Error(errorType = ErrorType.NETWORK)
    )
}

/**
 * Augustine briefing used for the thecouragepost.app screenshots. The scripture is KJV and the only
 * work cited is Confessions, so everything quoted here is public domain.
 */
internal val AugustineBriefingState = BriefingContract.UiState.Success(
    todayLabel = "Saturday, October 3, 2026",
    card = BriefingContract.CardState.Ready(
        figureId = 36L,
        figureName = "Augustine of Hippo",
        figureImageUrl = "https://sfocythzurfdpkegjlsl.supabase.co/storage/v1/object/public/portraits/36.webp",
        scriptureReference = "Matthew 11:28",
        scriptureText = "Come unto me, all ye that labour and are heavy laden, and I will give you rest.",
        insight = "I searched for rest in pleasure, in praise, and in clever arguments. " +
            "None of them could hold the weight of my heart. It was made for God, and it would not settle for less.",
        implication = "Your restlessness today is not a failure. It is a signpost. Let it turn you toward the One who made you.",
        inspiration = "Before the day fills up, bring Him the burden you are carrying. He has promised rest, not more striving.",
        sources = listOf("Confessions (397)"),
        tone = "Encouraging",
        theme = "Hope",
        challenge = "Name one thing you keep reaching for to quiet your heart. Offer it to God today.",
    )
)
