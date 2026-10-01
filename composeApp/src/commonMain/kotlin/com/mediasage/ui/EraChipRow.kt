package com.mediasage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
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
 * its resting chips line up with the content above.
 */
@Composable
fun EraChipRow(
    selectedEra: FigureEra?,
    onEraSelected: (FigureEra?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    LazyRow(
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
