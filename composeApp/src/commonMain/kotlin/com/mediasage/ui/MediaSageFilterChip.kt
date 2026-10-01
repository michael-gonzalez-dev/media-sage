package com.mediasage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.theme.ComicGradientOrientation
import com.mediasage.theme.MediaSageTheme
import com.mediasage.theme.rememberComicSurfaceColors

/**
 * A single-select filter pill in the comic palette, for chip rows such as the Headlines categories
 * and the Reporters eras. The selected chip gets the comic gradient and a checkmark.
 */
@Composable
fun MediaSageFilterChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // FilterChip for its selected semantics and checkmark animation; the comic gradient can't be
    // expressed through SelectableChipColors (flat Colors only), so the selected container is
    // transparent and rememberComicSurfaceColors' background paints on a sibling Box behind it.
    // The chip's 32dp pill renders centered inside its 48dp minimum touch target, so the gradient
    // is inset to those visual bounds instead of filling the full (invisible) interactive layout.
    val comicColors = rememberComicSurfaceColors(ComicGradientOrientation.Horizontal)
    Box(modifier = modifier) {
        if (selected) {
            SelectedChipBackground(background = comicColors.background)
        }
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
            leadingIcon = if (selected) ({ SelectedCheckIcon() }) else null,
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(
                labelColor = comicColors.content.copy(alpha = 0.75f),
                selectedContainerColor = Color.Transparent,
                selectedLabelColor = comicColors.content,
                selectedLeadingIconColor = comicColors.content
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = comicColors.border.copy(alpha = 0.5f),
                selectedBorderColor = comicColors.border,
                selectedBorderWidth = 1.dp
            )
        )
    }
}

@Composable
private fun BoxScope.SelectedChipBackground(background: Modifier) {
    val touchTargetInset = (LocalMinimumInteractiveComponentSize.current - FilterChipDefaults.Height) / 2
    Box(
        modifier = Modifier
            .matchParentSize()
            .padding(vertical = touchTargetInset.coerceAtLeast(0.dp))
            .clip(CircleShape)
            .then(background)
    )
}

@Composable
private fun SelectedCheckIcon() {
    Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = null,
        modifier = Modifier.size(FilterChipDefaults.IconSize)
    )
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun MediaSageFilterChipPreview() {
    MediaSageTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MediaSageFilterChip(label = "All", selected = true, onClick = {})
            MediaSageFilterChip(label = "1500s & 1600s", selected = false, onClick = {})
        }
    }
}

// endregion
