package com.mediasage.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasage.theme.BrandAmber

/** A book's 2:3 portrait shape. */
private const val COVER_ASPECT_RATIO = 2f / 3f

/** Square at the spine, softly rounded at the fore-edge, like a hardback. */
private val CoverShape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 5.dp, bottomEnd = 5.dp)

enum class CoverKind { ART, DEFAULT }

/** Which cover a work shows: its art while there's a URL that hasn't failed to load, otherwise the clothbound default. */
fun coverKind(coverUrl: String?, loadFailed: Boolean): CoverKind =
    if (!coverUrl.isNullOrBlank() && !loadFailed) CoverKind.ART else CoverKind.DEFAULT

@Composable
fun BookCover(work: LibraryWorkItem, modifier: Modifier = Modifier) {
    var loadFailed by remember(work.coverUrl) { mutableStateOf(false) }
    val coverModifier = modifier
        .fillMaxWidth()
        .aspectRatio(COVER_ASPECT_RATIO)
        .shadow(elevation = 6.dp, shape = CoverShape)
        .clip(CoverShape)
    when (coverKind(work.coverUrl, loadFailed)) {
        CoverKind.ART -> AsyncImage(
            model = work.coverUrl,
            contentDescription = work.title,
            modifier = coverModifier,
            contentScale = ContentScale.Crop,
            onError = { loadFailed = true },
        )
        CoverKind.DEFAULT -> ClothBookCover(work, coverModifier)
    }
}

private val ThumbnailShape = RoundedCornerShape(topStart = 1.dp, bottomStart = 1.dp, topEnd = 2.dp, bottomEnd = 2.dp)

/** A book's cover at list-row size: its art, or its cloth with a shaded hinge and a gilt edge. The row names the book. */
@Composable
fun BookThumbnail(work: LibraryWorkItem, modifier: Modifier = Modifier) {
    var loadFailed by remember(work.coverUrl) { mutableStateOf(false) }
    val thumbnailModifier = modifier
        .aspectRatio(COVER_ASPECT_RATIO)
        .shadow(elevation = 2.dp, shape = ThumbnailShape)
        .clip(ThumbnailShape)
    when (coverKind(work.coverUrl, loadFailed)) {
        CoverKind.ART -> AsyncImage(
            model = work.coverUrl,
            contentDescription = null,
            modifier = thumbnailModifier,
            contentScale = ContentScale.Crop,
            onError = { loadFailed = true },
        )
        CoverKind.DEFAULT -> Box(
            thumbnailModifier
                .background(clothColorFor(work.id))
                .background(Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.3f), 0.25f to Color.Transparent))
                .padding(start = 5.dp, end = 3.dp, top = 3.dp, bottom = 3.dp)
                .border(0.5.dp, Gilt.copy(alpha = 0.6f)),
        )
    }
}

/** Deep bookcloths, each dark enough for gilt type to read on it and distinct on a light or dark page. */
private val ClothColors = listOf(
    Color(0xFF6B2626), // oxblood
    Color(0xFF2E4A3B), // forest
    Color(0xFF1F2D4C), // navy
    Color(0xFF7A5A22), // ochre
    Color(0xFF4A2D46), // plum
    Color(0xFF2A4750), // slate teal
    Color(0xFF5A3B28), // umber
)

/**
 * The same book always gets the same cloth. A reporter's works have consecutive ids, so neighbors on a shelf never share
 * one; seven cloths also leave no shared neighbor across the gap between a reporter's own and recorded works.
 */
internal fun clothColorFor(workId: Long): Color {
    val count = ClothColors.size
    return ClothColors[(((workId % count) + count) % count).toInt()]
}

/** Gilt stamping is part of the book, not the theme, so it stays gold on every page. */
private val Gilt = BrandAmber
private val HingeWidth = 9.dp

/** A clothbound cover: the book's cloth, a shaded hinge, and a gilt panel holding the title and the reporter's name. */
@Composable
private fun ClothBookCover(work: LibraryWorkItem, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(clothColorFor(work.id))
            // Light falls from above, so the cloth reads as a surface rather than a flat fill.
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.Black.copy(alpha = 0.18f)))),
    ) {
        Hinge(Modifier.align(Alignment.CenterStart))
        GiltPanel(
            work,
            Modifier.fillMaxSize().padding(start = HingeWidth + 6.dp, end = 7.dp, top = 9.dp, bottom = 9.dp),
        )
    }
}

@Composable
private fun Hinge(modifier: Modifier) {
    Row(modifier.fillMaxHeight()) {
        Box(
            Modifier.fillMaxHeight().width(HingeWidth).background(
                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.08f))),
            ),
        )
        Box(Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.12f)))
    }
}

@Composable
private fun GiltPanel(work: LibraryWorkItem, modifier: Modifier) {
    Column(
        modifier = modifier
            .border(0.75.dp, Gilt.copy(alpha = 0.55f), RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(5.dp).rotate(45f).background(Gilt.copy(alpha = 0.8f)))
        Spacer(Modifier.weight(1f))
        Text(
            text = work.title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
            color = Gilt,
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Box(Modifier.width(18.dp).height(0.75.dp).background(Gilt.copy(alpha = 0.7f)))
        Spacer(Modifier.weight(1f))
        Text(
            text = work.reporterName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, lineHeight = 9.sp, letterSpacing = 1.2.sp),
            color = Gilt.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
