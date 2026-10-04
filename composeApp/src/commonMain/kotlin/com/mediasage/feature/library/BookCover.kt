package com.mediasage.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/** A book's 2:3 portrait shape. */
private const val COVER_ASPECT_RATIO = 2f / 3f

enum class CoverKind { ART, DEFAULT }

/** Which cover a work shows: its art while there's a URL that hasn't failed to load, otherwise the typeset default. */
fun coverKind(coverUrl: String?, loadFailed: Boolean): CoverKind =
    if (!coverUrl.isNullOrBlank() && !loadFailed) CoverKind.ART else CoverKind.DEFAULT

@Composable
fun BookCover(work: LibraryWorkItem, modifier: Modifier = Modifier) {
    var loadFailed by remember(work.coverUrl) { mutableStateOf(false) }
    val coverModifier = modifier.fillMaxWidth().aspectRatio(COVER_ASPECT_RATIO)
    when (coverKind(work.coverUrl, loadFailed)) {
        CoverKind.ART -> AsyncImage(
            model = work.coverUrl,
            contentDescription = work.title,
            modifier = coverModifier,
            contentScale = ContentScale.Crop,
            onError = { loadFailed = true },
        )
        CoverKind.DEFAULT -> DefaultBookCover(work, coverModifier)
    }
}

/** How far the cover's paper and spine are tinted toward the ink, so the book stands off the page in every theme. */
private const val PAPER_TINT = 0.08f
private const val SPINE_TINT = 0.2f
private val SpineWidth = 5.dp

/**
 * A typeset cover in the paper's style: the reporter's name between rules, the title, and the year at the foot. Its
 * paper and spine are tinted from the theme's own surface and ink, so it lifts off a dark page and stays crisp on a light one.
 */
@Composable
private fun DefaultBookCover(work: LibraryWorkItem, modifier: Modifier) {
    val surface = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .background(lerp(surface, ink, PAPER_TINT))
            // The spine's tone, not the theme outline: dark themes draw their outline in nearly the page color, which
            // left the top and bottom edges invisible, so the cover looked cut off.
            .border(1.dp, lerp(surface, ink, SPINE_TINT))
    ) {
        Box(Modifier.fillMaxHeight().width(SpineWidth).background(lerp(surface, ink, SPINE_TINT)))
        CoverType(work, ink, Modifier.weight(1f).fillMaxHeight().padding(horizontal = 8.dp, vertical = 14.dp))
    }
}

@Composable
private fun CoverType(work: LibraryWorkItem, ink: Color, modifier: Modifier) {
    val rule = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HorizontalDivider(color = rule, thickness = 2.dp)
        Text(
            text = work.reporterName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
            color = ink,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDivider(color = rule, thickness = 0.5.dp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = work.title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
            color = ink,
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        work.year?.let {
            Text(text = it.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = rule, thickness = 2.dp)
    }
}
