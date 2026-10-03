package com.mediasage.feature.you

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.domain.model.LensFilter
import kotlinx.datetime.DayOfWeek

internal class ReaderStateProvider : PreviewParameterProvider<ReaderContract.UiState> {
    override val values = sequenceOf<ReaderContract.UiState>(AugustineReaderState)
}

private const val PORTRAITS = "https://sfocythzurfdpkegjlsl.supabase.co/storage/v1/object/public/portraits"

/** Saturday, October 3, 2026 — the date shown in the other website screenshots. */
private const val TODAY_EPOCH_DAY = 20729L

/**
 * Reader tab used for the thecouragepost.app screenshots. The week's roster mixes eras with
 * Augustine today, the memory quote is Confessions I.1 in Pusey's public-domain
 * translation, and the past-briefing lines are written for this sample, not quoted.
 */
internal val AugustineReaderState = ReaderContract.UiState.Ready(
    weekSlots = listOf(
        rosterSlot(DayOfWeek.MONDAY, 28L, "Julian of Norwich", LensFilter.LOVE),
        rosterSlot(DayOfWeek.TUESDAY, 68L, "C.S. Lewis", LensFilter.WRITINGS),
        rosterSlot(DayOfWeek.WEDNESDAY, 56L, "Frederick Douglass", LensFilter.JUSTICE),
        rosterSlot(DayOfWeek.THURSDAY, 30L, "Teresa of Ávila", LensFilter.GRACE),
        rosterSlot(DayOfWeek.FRIDAY, 53L, "Harriet Tubman", LensFilter.PERSEVERANCE),
        rosterSlot(DayOfWeek.SATURDAY, 36L, "Augustine of Hippo", LensFilter.HOPE),
        rosterSlot(DayOfWeek.SUNDAY, 3L, "Dietrich Bonhoeffer", LensFilter.NEWS),
    ),
    quoteCard = ReaderContract.QuoteCard(
        quoteText = "Thou madest us for Thyself, and our heart is restless, until it repose in Thee.",
        figureName = "Augustine of Hippo",
        figureRole = "Bishop & Church Father",
        figureImageUrl = "$PORTRAITS/36.webp",
    ),
    pastBriefings = listOf(
        pastBriefing(
            1, 53L, "Harriet Tubman",
            "Freedom was worth every mile of the road. Keep walking toward God's call, even in the dark.",
        ),
        pastBriefing(
            2, 30L, "Teresa of Ávila",
            "Prayer is a friendship, not a chore. Spend a few quiet minutes with God today and simply talk.",
        ),
        pastBriefing(
            3, 56L, "Frederick Douglass",
            "Speak up for someone who cannot speak for themselves. Courage grows each time you use it.",
        ),
    ),
    hasMorePastBriefings = true,
)

private fun rosterSlot(day: DayOfWeek, figureId: Long, name: String, lens: LensFilter) = ReaderContract.DaySlot(
    dayOfWeek = day,
    epochDay = TODAY_EPOCH_DAY - DayOfWeek.SATURDAY.ordinal + day.ordinal,
    isToday = day == DayOfWeek.SATURDAY,
    assignedFigureName = name,
    assignedFigureImageUrl = "$PORTRAITS/$figureId.webp",
    assignedLens = lens,
)

private fun pastBriefing(daysAgo: Int, figureId: Long, name: String, inspiration: String) = ReaderContract.PastBriefingCard(
    epochDay = TODAY_EPOCH_DAY - daysAgo,
    figureName = name,
    figureImageUrl = "$PORTRAITS/$figureId.webp",
    inspiration = inspiration,
    dayLabel = when (daysAgo) {
        1 -> ReaderContract.DayLabel.Yesterday
        2 -> ReaderContract.DayLabel.Text("Thursday")
        else -> ReaderContract.DayLabel.Text("Wednesday")
    },
)
