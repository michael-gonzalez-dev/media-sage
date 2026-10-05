package com.mediasage.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mediasage.theme.AppTheme
import com.mediasage.theme.MediaSageTheme
import com.mediasage.ui.EraChipRow
import com.mediasage.ui.FigureAvatar
import com.mediasage.ui.MediaSageEmptyState
import com.mediasage.ui.ScreenHeader
import com.mediasage.ui.SearchField
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.library_empty_subtitle
import mediasage.composeapp.generated.resources.library_empty_title
import mediasage.composeapp.generated.resources.library_filtered_empty_subtitle
import mediasage.composeapp.generated.resources.library_filtered_empty_title
import mediasage.composeapp.generated.resources.library_recorded_by
import mediasage.composeapp.generated.resources.library_search_hint
import mediasage.composeapp.generated.resources.library_show_list
import mediasage.composeapp.generated.resources.library_show_shelf
import mediasage.composeapp.generated.resources.library_subtitle
import mediasage.composeapp.generated.resources.library_work_count
import mediasage.composeapp.generated.resources.title_library
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val ShelfBookWidth = 112.dp
private val SectionAvatarSize = 28.dp
private val ListThumbnailWidth = 22.dp

@Composable
fun LibraryScreen(
    state: LibraryContract.UiState,
    onIntent: (LibraryContract.Intent) -> Unit,
) {
    when (state) {
        is LibraryContract.UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is LibraryContract.UiState.Success -> {
            LibraryContent(state, onIntent)
            state.selectedWork?.let { work ->
                LibraryWorkSheet(work = work, onDismiss = { onIntent(LibraryContract.Intent.WorkDismissed) })
            }
        }
    }
}

@Composable
private fun LibraryContent(state: LibraryContract.UiState.Success, onIntent: (LibraryContract.Intent) -> Unit) {
    val shelfState = rememberLazyListState()
    val listState = rememberLazyListState()
    val activeState = if (state.view == LibraryView.SHELF) shelfState else listState
    val collapsed by remember(activeState) {
        derivedStateOf { activeState.firstVisibleItemIndex > 0 || activeState.firstVisibleItemScrollOffset > 0 }
    }
    val onWorkClick: (LibraryWorkItem) -> Unit = { onIntent(LibraryContract.Intent.WorkSelected(it)) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LibraryHeader(state, activeState, collapsed, onIntent)
            when {
                state.sections.isEmpty() && state.isFiltered -> MediaSageEmptyState(
                    title = stringResource(Res.string.library_filtered_empty_title),
                    subtitle = stringResource(Res.string.library_filtered_empty_subtitle),
                )
                state.sections.isEmpty() -> MediaSageEmptyState(
                    title = stringResource(Res.string.library_empty_title),
                    subtitle = stringResource(Res.string.library_empty_subtitle),
                )
                state.view == LibraryView.SHELF -> LibraryShelves(state.sections, shelfState, onWorkClick)
                else -> LibraryList(state.sections, listState, onWorkClick)
            }
        }
    }
}

/** The title with the Shelf / List button at its end, then the search field and era chips, pinned above the shelves. */
@Composable
private fun LibraryHeader(
    state: LibraryContract.UiState.Success,
    listState: LazyListState,
    collapsed: Boolean,
    onIntent: (LibraryContract.Intent) -> Unit,
) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp)) {
        ScreenHeader(
            title = stringResource(Res.string.title_library),
            listState = listState,
            isCollapsed = collapsed,
            expandedTitleSize = 24f,
            subtitle = {
                Text(
                    text = stringResource(Res.string.library_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            },
            stickyContent = {
                SearchField(
                    query = state.searchQuery,
                    label = stringResource(Res.string.library_search_hint),
                    onQueryChanged = { onIntent(LibraryContract.Intent.SearchQueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
        ViewToggle(state.view, onIntent, Modifier.align(Alignment.TopEnd))
    }
    // Outside the 16dp inset so the chips scroll edge-to-edge, as on the Reporters tab.
    EraChipRow(
        selectedEra = state.selectedEra,
        onEraSelected = { onIntent(LibraryContract.Intent.EraSelected(it)) },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp)
    )
}

/** Shows the view a tap switches to. */
@Composable
private fun ViewToggle(view: LibraryView, onIntent: (LibraryContract.Intent) -> Unit, modifier: Modifier) {
    val (icon, label, next) = when (view) {
        LibraryView.SHELF -> Triple(Icons.AutoMirrored.Outlined.ViewList, Res.string.library_show_list, LibraryView.LIST)
        LibraryView.LIST -> Triple(Icons.Outlined.ViewAgenda, Res.string.library_show_shelf, LibraryView.SHELF)
    }
    IconButton(onClick = { onIntent(LibraryContract.Intent.ViewSelected(next)) }, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = stringResource(label))
    }
}

/** One horizontal shelf per reporter, so the page scrolls by reporter rather than by row of books. */
@Composable
private fun LibraryShelves(sections: List<LibrarySectionItem>, listState: LazyListState, onWorkClick: (LibraryWorkItem) -> Unit) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(sections, key = { it.figureId }) { section ->
            Column {
                SectionHeader(section, Modifier.padding(horizontal = 16.dp))
                LazyRow(
                    // Room above and below so the covers' shadows aren't clipped by the row.
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(section.works, key = { it.id }) { work -> ShelfBook(work, onClick = { onWorkClick(work) }) }
                }
            }
        }
    }
}

/** The cover carries the title, so beneath it only the year and who recorded the words. */
@Composable
private fun ShelfBook(work: LibraryWorkItem, onClick: () -> Unit) {
    Column(modifier = Modifier.width(ShelfBookWidth).clickable(onClick = onClick)) {
        BookCover(work)
        WorkDetails(work, Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun LibraryList(sections: List<LibrarySectionItem>, listState: LazyListState, onWorkClick: (LibraryWorkItem) -> Unit) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        sections.forEach { section ->
            item(key = "reporter-${section.figureId}") { SectionHeader(section) }
            items(section.works, key = { it.id }) { work -> ListBook(work, onClick = { onWorkClick(work) }) }
        }
    }
}

@Composable
private fun ListBook(work: LibraryWorkItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookThumbnail(work, Modifier.width(ListThumbnailWidth))
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(text = work.title, style = MaterialTheme.typography.bodyLarge)
            WorkDetails(work)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** The reporter's portrait, bold name, and how many works their shelf holds in italic, over an accent hairline. */
@Composable
private fun SectionHeader(section: LibrarySectionItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FigureAvatar(
                name = section.reporterName,
                portraitUrl = section.portraitUrl,
                size = SectionAvatarSize,
                modifier = Modifier.padding(end = 10.dp),
            )
            // Name and count share a baseline, so the small count sits on the name's line rather than floating mid-height.
            Row(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.reporterName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false).alignByBaseline(),
                )
                Text(
                    text = "  " + pluralStringResource(Res.plurals.library_work_count, section.works.size, section.works.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 0.75.dp,
            color = MediaSageTheme.colors.accent.copy(alpha = 0.7f),
        )
    }
}

/** "1869 · Words recorded by Sarah Bradford", or whichever half is known; nothing when neither is. */
@Composable
internal fun WorkDetails(work: LibraryWorkItem, modifier: Modifier = Modifier) {
    val recorded = work.recordedBy?.let { stringResource(Res.string.library_recorded_by, it) }
    val details = listOfNotNull(work.year?.toString(), recorded).joinToString(" · ")
    if (details.isNotEmpty()) {
        Text(
            text = details,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }
}

// region Previews

@Preview
@Composable
private fun LibraryScreenPreview(
    @PreviewParameter(LibraryStateProvider::class) state: LibraryContract.UiState
) {
    MediaSageTheme { LibraryScreen(state = state, onIntent = {}) }
}

@Preview(name = "Shelf — Classic light")
@Composable
private fun LibraryShelfClassicLightPreview() {
    MediaSageTheme(theme = AppTheme.CLASSIC, darkTheme = false) { LibraryScreen(state = LibraryShelfState, onIntent = {}) }
}

@Preview(name = "Shelf — Warm dark")
@Composable
private fun LibraryShelfWarmDarkPreview() {
    MediaSageTheme(theme = AppTheme.WARM, darkTheme = true) { LibraryScreen(state = LibraryShelfState, onIntent = {}) }
}

@Preview(name = "Shelf — Modern dark")
@Composable
private fun LibraryShelfModernDarkPreview() {
    MediaSageTheme(theme = AppTheme.MODERN, darkTheme = true) { LibraryScreen(state = LibraryShelfState, onIntent = {}) }
}

// endregion
