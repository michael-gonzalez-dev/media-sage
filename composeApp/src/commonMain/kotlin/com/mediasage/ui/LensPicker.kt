package com.mediasage.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasage.domain.model.LensFilter
import com.mediasage.theme.LensFaith
import com.mediasage.theme.LensGrace
import com.mediasage.theme.LensGrief
import com.mediasage.theme.LensHope
import com.mediasage.theme.LensJustice
import com.mediasage.theme.LensLove
import com.mediasage.theme.LensPerseverance
import com.mediasage.theme.LensRepentance
import com.mediasage.theme.LensWritings
import com.mediasage.theme.MediaSageTheme
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.you_lens_faith
import mediasage.composeapp.generated.resources.you_lens_grace
import mediasage.composeapp.generated.resources.you_lens_grief
import mediasage.composeapp.generated.resources.you_lens_hope
import mediasage.composeapp.generated.resources.you_lens_justice
import mediasage.composeapp.generated.resources.you_lens_love
import mediasage.composeapp.generated.resources.you_lens_perseverance
import mediasage.composeapp.generated.resources.you_lens_repentance
import mediasage.composeapp.generated.resources.you_lens_today
import mediasage.composeapp.generated.resources.you_lens_writings
import mediasage.composeapp.generated.resources.you_picker_back_description
import mediasage.composeapp.generated.resources.you_picker_choose_theme
import org.jetbrains.compose.resources.stringResource

/**
 * Picks the lens a reporter briefs through. Headlines is reported as `null`, matching how a day
 * assignment stores the default lens. [onBack] adds a back row (for pickers nested under a figure
 * list); without it the header shows only the reporter.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LensPicker(
    figureName: String,
    portraitUrl: String?,
    onLensSelected: (LensFilter?) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        LensPickerHeader(figureName = figureName, portraitUrl = portraitUrl, onBack = onBack)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Text(
            text = stringResource(Res.string.you_picker_choose_theme),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LensFilter.entries.forEach { lens ->
                LensChip(lens = lens, onClick = { onLensSelected(if (lens == LensFilter.NEWS) null else lens) })
            }
        }
    }
}

@Composable
private fun LensPickerHeader(figureName: String, portraitUrl: String?, onBack: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onBack != null) Modifier.clickable(onClick = onBack) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.you_picker_back_description),
            )
        }
        if (portraitUrl != null) {
            AsyncImage(
                model = portraitUrl,
                contentDescription = figureName,
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                error = rememberVectorPainter(Icons.Filled.Person),
                fallback = rememberVectorPainter(Icons.Filled.Person),
            )
        } else {
            FigurePlaceholder(name = figureName, size = 32.dp)
        }
        Text(text = figureName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LensChip(lens: LensFilter, onClick: () -> Unit) {
    val lensColor = lens.color()
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = stringResource(lens.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(labelColor = lensColor),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = lensColor.copy(alpha = 0.5f),
            borderWidth = 1.5.dp,
        ),
    )
}

internal fun LensFilter.labelRes() = when (this) {
    LensFilter.NEWS -> Res.string.you_lens_today
    LensFilter.WRITINGS -> Res.string.you_lens_writings
    LensFilter.LOVE -> Res.string.you_lens_love
    LensFilter.GRACE -> Res.string.you_lens_grace
    LensFilter.FAITH -> Res.string.you_lens_faith
    LensFilter.GRIEF -> Res.string.you_lens_grief
    LensFilter.REPENTANCE -> Res.string.you_lens_repentance
    LensFilter.HOPE -> Res.string.you_lens_hope
    LensFilter.JUSTICE -> Res.string.you_lens_justice
    LensFilter.PERSEVERANCE -> Res.string.you_lens_perseverance
}

@Composable
internal fun LensFilter.color(): Color = when (this) {
    LensFilter.NEWS -> MaterialTheme.colorScheme.primary
    LensFilter.WRITINGS -> LensWritings
    LensFilter.LOVE -> LensLove
    LensFilter.GRACE -> LensGrace
    LensFilter.FAITH -> LensFaith
    LensFilter.GRIEF -> LensGrief
    LensFilter.REPENTANCE -> LensRepentance
    LensFilter.HOPE -> LensHope
    LensFilter.JUSTICE -> LensJustice
    LensFilter.PERSEVERANCE -> LensPerseverance
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun LensPickerPreview() {
    MediaSageTheme {
        Surface {
            LensPicker(figureName = "C.S. Lewis", portraitUrl = null, onLensSelected = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LensPickerWithBackPreview() {
    MediaSageTheme(darkTheme = true) {
        Surface {
            LensPicker(figureName = "Teresa of Ávila", portraitUrl = null, onLensSelected = {}, onBack = {})
        }
    }
}

// endregion
