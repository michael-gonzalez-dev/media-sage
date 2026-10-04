package com.mediasage.feature.you

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mediasage.ui.MediaSageBackRow
import com.mediasage.ui.MediaSageBriefingHeader
import com.mediasage.ui.MediaSageDateDivider
import com.mediasage.ui.MediaSageEmptyState
import com.mediasage.ui.MediaSageScriptureBlock
import kotlinx.datetime.LocalDate
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.reader_history_intro
import mediasage.composeapp.generated.resources.reader_history_list_empty_subtitle
import mediasage.composeapp.generated.resources.reader_history_list_empty_title
import mediasage.composeapp.generated.resources.reader_history_show_calendar
import mediasage.composeapp.generated.resources.reader_history_show_list
import mediasage.composeapp.generated.resources.you_nav_history
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReaderHistoryScreen(
    state: ReaderHistoryContract.UiState,
    onIntent: (ReaderHistoryContract.Intent) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToDayDetail: (epochDay: Long, figureName: String?, figureImageUrl: String?) -> Unit = { _, _, _ -> },
) {
    val ready = state as? ReaderHistoryContract.UiState.Ready

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MediaSageBackRow(onNavigateBack = onNavigateBack) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.you_nav_history),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (ready != null) {
                        ViewModeToggleButton(viewMode = ready.viewMode, onIntent = onIntent)
                    }
                }
            }
            if (ready != null) {
                Text(
                    text = stringResource(Res.string.reader_history_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                when (ready.viewMode) {
                    ReaderHistoryContract.ViewMode.CALENDAR -> HistoryCalendarList(
                        calendarMonths = ready.calendarMonths,
                        onNavigateToDayDetail = onNavigateToDayDetail,
                    )
                    ReaderHistoryContract.ViewMode.LIST -> HistoryListView(
                        listDays = ready.listDays,
                        onNavigateToDayDetail = onNavigateToDayDetail,
                    )
                }
            }
        }
    }
}

/** A trailing icon button in the top bar that switches between the calendar and the list, like the Reporters tab's grid toggle. */
@Composable
private fun ViewModeToggleButton(
    viewMode: ReaderHistoryContract.ViewMode,
    onIntent: (ReaderHistoryContract.Intent) -> Unit,
) {
    // Shows the view a tap switches to.
    val (icon, label, next) = when (viewMode) {
        ReaderHistoryContract.ViewMode.CALENDAR ->
            Triple(Icons.Outlined.ViewAgenda, Res.string.reader_history_show_list, ReaderHistoryContract.ViewMode.LIST)
        ReaderHistoryContract.ViewMode.LIST ->
            Triple(Icons.Outlined.CalendarMonth, Res.string.reader_history_show_calendar, ReaderHistoryContract.ViewMode.CALENDAR)
    }
    IconButton(onClick = { onIntent(ReaderHistoryContract.Intent.ViewModeChanged(next)) }) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(label),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryCalendarList(
    calendarMonths: List<List<ReaderHistoryContract.CalendarDay>>,
    onNavigateToDayDetail: (epochDay: Long, figureName: String?, figureImageUrl: String?) -> Unit,
) {
    if (calendarMonths.all { month -> month.none { it.hasData } }) {
        MediaSageEmptyState(
            title = stringResource(Res.string.reader_history_list_empty_title),
            subtitle = stringResource(Res.string.reader_history_list_empty_subtitle),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(calendarMonths, key = { it.first().epochDay }) { monthDays ->
            CalendarCard(
                days = monthDays,
                onDayTapped = { epochDay ->
                    val day = monthDays.find { it.epochDay == epochDay }
                    if (day?.isFuture != true) {
                        onNavigateToDayDetail(epochDay, day?.figureName, day?.figurePortraitUrl)
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun HistoryListView(
    listDays: List<ReaderHistoryContract.ListDay>,
    onNavigateToDayDetail: (epochDay: Long, figureName: String?, figureImageUrl: String?) -> Unit,
) {
    if (listDays.isEmpty()) {
        MediaSageEmptyState(
            title = stringResource(Res.string.reader_history_list_empty_title),
            subtitle = stringResource(Res.string.reader_history_list_empty_subtitle),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(listDays, key = { it.epochDay }) { day ->
            HistoryDayCard(
                day = day,
                onClick = { onNavigateToDayDetail(day.epochDay, day.figureName, day.figurePortraitUrl) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
        }
    }
}

@Composable
private fun HistoryDayCard(
    day: ReaderHistoryContract.ListDay,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        MediaSageDateDivider(dateLabel = formatListDate(day.epochDay))
        MediaSageBriefingHeader(
            figureName = day.figureName,
            figureImageUrl = day.figurePortraitUrl,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
        )
        if (day.scriptureReference != null && day.scriptureText != null) {
            MediaSageScriptureBlock(
                scriptureReference = day.scriptureReference,
                scriptureText = day.scriptureText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun formatListDate(epochDay: Long): String {
    val date = LocalDate.fromEpochDays(epochDay.toInt())
    val monthName = date.month.name.lowercase().replaceFirstChar { it.titlecase() }
    return "$monthName ${date.dayOfMonth}, ${date.year}"
}
