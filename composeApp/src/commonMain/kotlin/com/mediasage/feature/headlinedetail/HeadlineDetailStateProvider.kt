package com.mediasage.feature.headlinedetail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.ui.ErrorType

internal class HeadlineDetailStateProvider : PreviewParameterProvider<HeadlineDetailContract.UiState> {
    override val values = sequenceOf(
        HeadlineDetailContract.UiState.Loading,
        AugustineHeadlineState.copy(encouragement = HeadlineDetailContract.EncouragementState.Loading),
        AugustineHeadlineState,
        HeadlineDetailContract.UiState.Error(errorType = ErrorType.NETWORK),
    )
}

/**
 * Headline paired with Augustine, used for the thecouragepost.app screenshots. No headline photo,
 * the quote is Confessions I.1 in Pusey's public-domain translation, and the scripture is KJV.
 */
internal val AugustineHeadlineState = HeadlineDetailContract.UiState.Success(
    headlineTitle = "Why So Many Americans Have Trouble Spending Their Retirement Savings",
    headlineSource = "Barron's · Oct 3, 2026",
    headlineCategory = "Business",
    headlineImageUrl = null,
    encouragement = HeadlineDetailContract.EncouragementState.Loaded(
        summary = "Many Americans spend decades building a nest egg. Once they retire, " +
            "many find it hard to spend any of it.",
        quoteText = "Thou madest us for Thyself, and our heart is restless, until it repose in Thee.",
        figureName = "Augustine of Hippo",
        figureRole = "Bishop & Church Father",
        figureImageUrl = "https://sfocythzurfdpkegjlsl.supabase.co/storage/v1/object/public/portraits/36.webp",
        scriptureReference = "Matthew 6:34",
        scriptureText = "Take therefore no thought for the morrow: for the morrow shall take thought for the things " +
            "of itself. Sufficient unto the day is the evil thereof.",
        matchExplanation = "Saving for the future is wise. Yet even a full account cannot quiet the fear of not " +
            "having enough. Augustine knew that restlessness well. He found that only God could give the rest he sought.",
        matchTheme = "Faith",
        tone = "Encouraging",
    ),
)
