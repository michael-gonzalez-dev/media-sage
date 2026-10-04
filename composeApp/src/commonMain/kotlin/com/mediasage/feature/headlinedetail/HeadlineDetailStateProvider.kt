package com.mediasage.feature.headlinedetail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.ui.ErrorType

internal class HeadlineDetailStateProvider : PreviewParameterProvider<HeadlineDetailContract.UiState> {
    override val values = sequenceOf(
        HeadlineDetailContract.UiState.Loading,
        TozerHeadlineState.copy(encouragement = HeadlineDetailContract.EncouragementState.Loading),
        TozerHeadlineState,
        HeadlineDetailContract.UiState.Error(errorType = ErrorType.NETWORK),
    )
}

/**
 * Headline paired with Tozer, used for the thecouragepost.app screenshots. No headline photo,
 * the quote is from The Pursuit of God (1948, copyright not renewed), and the scripture is the
 * public-domain Berean Standard Bible.
 */
internal val TozerHeadlineState = HeadlineDetailContract.UiState.Success(
    headlineTitle = "Why So Many Americans Have Trouble Spending Their Retirement Savings",
    headlineSource = "Barron's · Oct 3, 2026",
    headlineCategory = "Business",
    headlineImageUrl = null,
    encouragement = HeadlineDetailContract.EncouragementState.Loaded(
        summary = "Many Americans spend decades building a nest egg. Once they retire, " +
            "many find it hard to spend any of it.",
        quoteText = "The man who has God for his treasure has all things in One.",
        figureName = "A.W. Tozer",
        figureRole = "Pastor & Author",
        figureImageUrl = "https://sfocythzurfdpkegjlsl.supabase.co/storage/v1/object/public/portraits/19.webp",
        scriptureReference = "Matthew 6:34",
        scriptureText = "Therefore do not worry about tomorrow, for tomorrow will worry about itself. " +
            "Today has enough trouble of its own.",
        matchExplanation = "Saving for the future is wise. Yet even a full account cannot quiet the fear of running " +
            "short. Tozer saw that things make a poor treasure. When God is your treasure, you already have enough.",
        matchTheme = "Faith",
        tone = "Encouraging",
    ),
)
