package com.mediasage.ui

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.domain.model.FigureEra
import com.mediasage.theme.MediaSageTheme
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.era_1500s_1600s
import mediasage.composeapp.generated.resources.era_1700s_1800s
import mediasage.composeapp.generated.resources.era_all
import mediasage.composeapp.generated.resources.era_early_church
import mediasage.composeapp.generated.resources.era_middle_ages
import mediasage.composeapp.generated.resources.era_modern
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A sideways-scrolling row of single-select era chips, led by All. A null [selectedEra] means All.
 * The row has no outer padding of its own beyond [contentPadding], so it can run edge-to-edge while
 * its resting chips line up with the content above. The selected chip slides to the middle of the row.
 */
@Composable
fun EraChipRow(
    selectedEra: FigureEra?,
    onEraSelected: (FigureEra?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val listState = rememberLazyListState()
    // All is item 0, so each era sits one place after its position in FigureEra.entries.
    val selectedIndex = FigureEra.entries.indexOf(selectedEra) + 1
    LaunchedEffect(selectedIndex) { listState.animateScrollToCentre(selectedIndex) }
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth().selectableGroup(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            MediaSageFilterChip(
                label = stringResource(Res.string.era_all),
                selected = selectedEra == null,
                onClick = { onEraSelected(null) }
            )
        }
        items(FigureEra.entries) { era ->
            MediaSageFilterChip(
                label = stringResource(era.labelRes()),
                selected = era == selectedEra,
                onClick = { onEraSelected(era) }
            )
        }
    }
}

/**
 * Scrolls until the item at [index] sits in the middle of the row. The row stops at its ends, so the first and last
 * chips can only get as close to the middle as the scroll allows.
 */
private suspend fun LazyListState.animateScrollToCentre(index: Int) {
    if (layoutInfo.visibleItemsInfo.none { it.index == index }) scrollToItem(index)
    // Waits for a layout that includes the item, which on first appearance is the row's first frame.
    val info = snapshotFlow { layoutInfo }.first { layout -> layout.visibleItemsInfo.any { it.index == index } }
    val item = info.visibleItemsInfo.first { it.index == index }
    val rowCentre = (info.viewportStartOffset + info.viewportEndOffset) / 2
    animateScrollBy((item.offset + item.size / 2 - rowCentre).toFloat())
}

private fun FigureEra.labelRes(): StringResource = when (this) {
    FigureEra.EARLY_CHURCH -> Res.string.era_early_church
    FigureEra.MIDDLE_AGES -> Res.string.era_middle_ages
    FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS -> Res.string.era_1500s_1600s
    FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS -> Res.string.era_1700s_1800s
    FigureEra.MODERN -> Res.string.era_modern
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun EraChipRowPreview() {
    MediaSageTheme {
        EraChipRow(selectedEra = FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS, onEraSelected = {})
    }
}

// endregion
