package com.mediasage.feature.you

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.you_reporters_this_week
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReporterScheduleSection(
    weekSlots: List<ReaderContract.DaySlot>,
    onIntent: (ReaderContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayIndex = remember(weekSlots) { weekSlots.indexOfFirst { it.isToday } }
    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, todayIndex))
    val density = LocalDensity.current
    val slotWidth = daySlotWidth()
    CenterTodayEffect(rowState, todayIndex, slotWidth, density)

    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.you_reporters_this_week),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(weekSlots.size) { index ->
                val slot = weekSlots[index]
                DaySlotItem(
                    slot = slot,
                    width = slotWidth,
                    onClick = { onIntent(ReaderContract.Intent.DaySlotTapped(index)) },
                )
            }
        }
    }
}

@Composable
private fun CenterTodayEffect(rowState: LazyListState, todayIndex: Int, slotWidth: Dp, density: Density) {
    LaunchedEffect(todayIndex, slotWidth) {
        if (todayIndex < 0) return@LaunchedEffect
        snapshotFlow { rowState.layoutInfo.viewportSize.width }
            .filter { it > 0 }
            .first()
            .let { viewportWidth ->
                val itemWidthPx = with(density) { slotWidth.roundToPx() }
                val offset = -(viewportWidth / 2 - itemWidthPx / 2)
                rowState.scrollToItem(index = todayIndex, scrollOffset = offset)
            }
    }
}

/**
 * A day's slot is 80dp at the default text size and widens in step with the name text, so the longest
 * single word in a reporter's name ("Melanchthon", about 74dp at the default size) fits on one line
 * instead of breaking mid-word. The portrait stays 72dp.
 */
@Composable
private fun daySlotWidth(): Dp {
    val nameSize = with(LocalDensity.current) { MaterialTheme.typography.labelSmall.fontSize.toDp() }
    return DaySlotBaseWidth * (nameSize / DaySlotBaseNameSize).coerceAtLeast(1f)
}

private val DaySlotBaseWidth = 80.dp

// labelSmall's unscaled size in Type.kt.
private val DaySlotBaseNameSize = 11.dp
