package com.mediasage.feature.figures

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasage.data.ReporterView
import com.mediasage.theme.MediaSageTheme
import com.mediasage.theme.ReaderAmber
import com.mediasage.ui.EraChipRow
import com.mediasage.ui.FigurePlaceholder
import com.mediasage.ui.MediaSageEmptyState
import com.mediasage.ui.ScreenHeader
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.search_voices_hint
import mediasage.composeapp.generated.resources.title_voices
import mediasage.composeapp.generated.resources.voices_empty_state
import mediasage.composeapp.generated.resources.voices_filtered_empty_subtitle
import mediasage.composeapp.generated.resources.voices_filtered_empty_title
import mediasage.composeapp.generated.resources.voices_show_deck
import mediasage.composeapp.generated.resources.voices_show_grid
import mediasage.composeapp.generated.resources.voices_subtitle
import org.jetbrains.compose.resources.stringResource

private const val MAX_THEME_CHIPS = 2

@Composable
fun FiguresScreen(
    state: FiguresContract.UiState,
    onIntent: (FiguresContract.Intent) -> Unit,
    onNavigateToFigureDetail: (figureId: Long) -> Unit = {}
) {
    when (state) {
        is FiguresContract.UiState.Loading -> LoadingState()
        is FiguresContract.UiState.Success -> {
            ReportersContent(
                state = state,
                onIntent = onIntent,
                onFigureClick = { id ->
                    onIntent(FiguresContract.Intent.FigureClicked(id))
                    onNavigateToFigureDetail(id)
                }
            )
            state.pendingReassignment?.let { pending ->
                PendingReassignmentDialog(
                    pending = pending,
                    onConfirm = { onIntent(FiguresContract.Intent.ConfirmReassignment) },
                    onDismiss = { onIntent(FiguresContract.Intent.CancelReassignment) },
                )
            }
        }
    }
}

/**
 * The field keeps its own text and passes each change on, so typing never waits for the ViewModel's state to come
 * back. In the deck the field sits inside a layout that composes a frame late, and a field fed from that state lost
 * keystrokes typed quickly.
 */
@Composable
private fun SearchBar(query: String, onQueryChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    val textState = rememberTextFieldState(initialText = query)
    val latestOnQueryChanged by rememberUpdatedState(onQueryChanged)
    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }.collect { latestOnQueryChanged(it) }
    }
    OutlinedTextField(
        state = textState,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        label = { Text(stringResource(Res.string.search_voices_hint)) },
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = MaterialTheme.shapes.medium,
        trailingIcon = {
            if (textState.text.isNotBlank()) {
                IconButton(onClick = { textState.clearText() }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                }
            }
        }
    )
}

/** The search field, with the button that switches between the card deck and the grid at its end. */
@Composable
private fun SearchRow(state: FiguresContract.UiState.Success, onIntent: (FiguresContract.Intent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SearchBar(
            query = state.searchQuery,
            onQueryChanged = { onIntent(FiguresContract.Intent.SearchQueryChanged(it)) },
            modifier = Modifier.weight(1f),
        )
        // Shows the view a tap switches to.
        val (icon, label, next) = when (state.view) {
            ReporterView.DECK -> Triple(Icons.Outlined.GridView, Res.string.voices_show_grid, ReporterView.GRID)
            ReporterView.GRID -> Triple(Icons.Outlined.ViewCarousel, Res.string.voices_show_deck, ReporterView.DECK)
        }
        IconButton(onClick = { onIntent(FiguresContract.Intent.ViewSelected(next)) }, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
            Icon(imageVector = icon, contentDescription = stringResource(label))
        }
    }
}

@Composable
private fun ReportersContent(
    state: FiguresContract.UiState.Success,
    onIntent: (FiguresContract.Intent) -> Unit,
    onFigureClick: (Long) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    // The grid scrolls the header shut and keeps it pinned above; the deck carries the header in its own scroll.
    val collapsed by remember(state.view) {
        derivedStateOf {
            state.view == ReporterView.GRID && (gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.view == ReporterView.GRID) ReportersHeader(state, onIntent, listState, collapsed)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .pullToRefresh(
                        isRefreshing = state.isRefreshing,
                        state = pullToRefreshState,
                        onRefresh = { onIntent(FiguresContract.Intent.Refresh) }
                    )
            ) {
                when (state.view) {
                    ReporterView.GRID -> VoicesGrid(state, gridState, onFigureClick)
                    ReporterView.DECK -> VoicesDeck(
                        state = state,
                        onIntent = onIntent,
                        onReadMore = onFigureClick,
                        header = { ReportersHeader(state, onIntent, listState, collapsed = false) },
                        emptyState = { if (state.isFiltered) FilteredEmptyState() else EmptyState() },
                    )
                }

                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun ReportersHeader(
    state: FiguresContract.UiState.Success,
    onIntent: (FiguresContract.Intent) -> Unit,
    listState: LazyListState,
    collapsed: Boolean,
) {
    // Above the grid it stays pinned and collapses as the grid scrolls; in the deck it scrolls with the cards.
    ScreenHeader(
        title = stringResource(Res.string.title_voices),
        listState = listState,
        isCollapsed = collapsed,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        expandedTitleSize = 24f,
        subtitle = {
            Text(
                text = stringResource(Res.string.voices_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        },
        stickyContent = { SearchRow(state, onIntent) }
    )
    // Outside ScreenHeader's 16dp inset so the chips scroll edge-to-edge; the row's own
    // contentPadding lines resting chips up with the search field above.
    EraChipRow(
        selectedEra = state.selectedEra,
        onEraSelected = { onIntent(FiguresContract.Intent.EraSelected(it)) },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun VoicesGrid(state: FiguresContract.UiState.Success, gridState: LazyGridState, onFigureClick: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.figures.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                if (state.isFiltered) FilteredEmptyState() else EmptyState()
            }
        } else {
            items(state.figures, key = { it.id }) { figure ->
                PortraitCard(figure = figure, onClick = { onFigureClick(figure.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PortraitCard(figure: VoiceFigureItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = if (figure.isPinned) 2.dp else 1.dp,
                color = if (figure.isPinned) ReaderAmber else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            Box {
                if (figure.imageUrl != null) {
                    AsyncImage(
                        model = figure.imageUrl,
                        contentDescription = figure.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .then(
                                // Gold ring inset on the portrait when pinned
                                if (figure.isPinned) Modifier.border(
                                    width = 3.dp,
                                    color = ReaderAmber,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                                ) else Modifier
                            ),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .then(
                                if (figure.isPinned) Modifier.border(
                                    width = 3.dp,
                                    color = ReaderAmber,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        FigurePlaceholder(name = figure.name, size = 72.dp)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(
                    text = figure.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
                if (figure.role.isNotBlank()) {
                    Text(
                        text = figure.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                    )
                }
                if (figure.lifespan.isNotBlank()) {
                    Text(
                        text = figure.lifespan,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
                if (figure.themes.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        figure.themes.take(MAX_THEME_CHIPS).forEach { theme ->
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = theme,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.voices_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilteredEmptyState() {
    MediaSageEmptyState(
        title = stringResource(Res.string.voices_filtered_empty_title),
        subtitle = stringResource(Res.string.voices_filtered_empty_subtitle),
        modifier = Modifier.padding(vertical = 48.dp)
    )
}

private val FiguresContract.UiState.Success.isFiltered: Boolean
    get() = selectedEra != null || searchQuery.isNotBlank()

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun FiguresScreenPreview(
    @PreviewParameter(FiguresStateProvider::class) state: FiguresContract.UiState
) {
    MediaSageTheme {
        FiguresScreen(state = state, onIntent = {})
    }
}

// endregion
