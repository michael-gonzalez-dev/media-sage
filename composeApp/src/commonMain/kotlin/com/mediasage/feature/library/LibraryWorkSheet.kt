package com.mediasage.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.theme.MediaSageTheme
import com.mediasage.ui.FigureAvatar
import com.mediasage.ui.MediaSageBottomSheet

private val SheetCoverWidth = 140.dp
private val SheetAvatarSize = 32.dp

/** A tapped book's details: its cover, title, year and recorder, and the reporter whose words it holds. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryWorkSheet(work: LibraryWorkItem, onDismiss: () -> Unit) {
    MediaSageBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        LibraryWorkDetails(work, Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp))
    }
}

@Composable
private fun LibraryWorkDetails(work: LibraryWorkItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BookCover(work, Modifier.width(SheetCoverWidth))
        Text(
            text = work.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        WorkDetails(work)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FigureAvatar(
                name = work.reporterName,
                portraitUrl = work.reporterPortraitUrl,
                size = SheetAvatarSize,
                modifier = Modifier.padding(end = 10.dp),
            )
            Text(text = work.reporterName, style = MaterialTheme.typography.titleMedium)
        }
    }
}

// region Previews

@Preview
@Composable
private fun LibraryWorkDetailsPreview() {
    MediaSageTheme {
        LibraryWorkDetails(LibraryShelfState.sections.last().works.first(), Modifier.padding(24.dp))
    }
}

// endregion
