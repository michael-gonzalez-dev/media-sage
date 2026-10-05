package com.mediasage.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/** A book's 2:3 portrait shape. */
private const val COVER_ASPECT_RATIO = 2f / 3f

/** Square at the spine, softly rounded at the fore-edge, like a hardback. */
private val CoverShape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 5.dp, bottomEnd = 5.dp)
private val ThumbnailShape = RoundedCornerShape(topStart = 1.dp, bottomStart = 1.dp, topEnd = 2.dp, bottomEnd = 2.dp)
internal val HingeWidth = 9.dp
private val HalfBindingSpine = 18.dp
private val WrapFlapWidth = 14.dp

enum class CoverKind { ART, DEFAULT }

/** Which cover a work shows: its art while there's a URL that hasn't failed to load, otherwise its period binding. */
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
        CoverKind.DEFAULT -> PeriodCover(work, coverModifier)
    }
}

/** A book's cover at list-row size: its art, or its cloth with its period's trim. The row names the book. */
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
                .background(work.coverCloth())
                .background(Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.3f), 0.25f to Color.Transparent)),
            contentAlignment = Alignment.Center,
        ) {
            if (work.coverStyle().binding == PeriodBinding.MODERN_PRINT) {
                ModernThumbnailMark(work.modernLayout(), work.coverCloth())
            } else {
                Box(Modifier.fillMaxSize().padding(start = 5.dp, end = 3.dp, top = 3.dp, bottom = 3.dp).border(0.5.dp, CoverGilt.copy(alpha = 0.6f)))
            }
        }
    }
}

/** The cover a book without art shows: its cloth and its period's binding, hinge and stamping. */
@Composable
private fun PeriodCover(work: LibraryWorkItem, modifier: Modifier) {
    val cloth = work.coverCloth()
    val binding = work.coverStyle().binding
    Box(
        modifier = modifier
            .background(cloth)
            // Light falls from above, so the cloth reads as a surface rather than a flat fill.
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.Black.copy(alpha = 0.18f))))
            .drawBehind {
                when (binding) {
                    PeriodBinding.EARLY_CODEX -> drawWrapFlap(WrapFlapWidth.toPx())
                    PeriodBinding.GEORGIAN_HALF -> drawHalfBinding(HalfBindingSpine.toPx())
                    PeriodBinding.VICTORIAN_CLOTH -> drawLinen()
                    else -> Unit
                }
            }
            .drawWithContent {
                drawContent()
                if (binding == PeriodBinding.MEDIEVAL_BOARDS) drawMedievalHardware()
            },
    ) {
        if (binding == PeriodBinding.MODERN_PRINT) {
            ModernPrintPanel(work, cloth, Modifier.fillMaxSize())
        } else {
            StampedPanel(work, binding, cloth, Modifier.fillMaxSize().padding(panelInsets(binding)))
        }
        // Drawn last so the hinge's shade falls across every binding, the modern cream papers included.
        Hinge(Modifier.align(Alignment.CenterStart))
    }
}

private fun panelInsets(binding: PeriodBinding): PaddingValues {
    val start = if (binding == PeriodBinding.GEORGIAN_HALF) HalfBindingSpine + 6.dp else HingeWidth + 6.dp
    val end = when (binding) {
        PeriodBinding.EARLY_CODEX -> WrapFlapWidth + 4.dp
        PeriodBinding.MEDIEVAL_BOARDS -> 14.dp
        else -> 7.dp
    }
    return PaddingValues(start = start, end = end, top = 9.dp, bottom = 9.dp)
}

@Composable
private fun Hinge(modifier: Modifier) {
    Row(modifier.fillMaxHeight()) {
        Box(
            Modifier.fillMaxHeight().width(HingeWidth).background(
                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.04f))),
            ),
        )
        Box(Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.12f)))
    }
}

// region Stamped bindings (before the paperback)

/** The period's ornament, the title (in gilt, or on a leather label), a short rule, and the reporter's name. */
@Composable
private fun StampedPanel(work: LibraryWorkItem, binding: PeriodBinding, cloth: Color, modifier: Modifier) {
    Column(
        modifier = modifier.periodFrame(binding).padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PeriodOrnament(binding)
        Spacer(Modifier.weight(1f))
        when (binding) {
            PeriodBinding.REFORMATION_BLIND -> TitleLabel(work.title, lerp(cloth, Color.Black, 0.45f))
            PeriodBinding.GEORGIAN_HALF -> TitleLabel(work.title, CoverRedLabel)
            else -> GiltTitle(work.title)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.width(18.dp).height(0.75.dp).background(CoverGilt.copy(alpha = 0.7f)))
        Spacer(Modifier.weight(1f))
        Text(
            text = work.reporterName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, lineHeight = 9.sp, letterSpacing = 1.2.sp),
            color = CoverGilt.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.periodFrame(binding: PeriodBinding): Modifier = when (binding) {
    PeriodBinding.GENERAL -> border(0.75.dp, CoverGilt.copy(alpha = 0.55f), RoundedCornerShape(2.dp))
    PeriodBinding.EARLY_CODEX -> drawBehind { drawBlindFrame(0f) }
    PeriodBinding.MEDIEVAL_BOARDS -> border(0.75.dp, CoverGilt.copy(alpha = 0.55f)).padding(2.5.dp).border(0.5.dp, CoverGilt.copy(alpha = 0.45f))
    PeriodBinding.REFORMATION_BLIND -> drawBehind {
        drawBlindFrame(0f)
        drawBlindFrame(3.dp.toPx())
        drawCornerPieces(Color.Black.copy(alpha = 0.35f), inset = 6.dp.toPx())
    }
    PeriodBinding.VICTORIAN_CLOTH -> drawBehind {
        drawBands()
        drawBlindFrame(6.dp.toPx())
    }
    else -> this
}

@Composable
private fun GiltTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
        color = CoverGilt,
        textAlign = TextAlign.Center,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
    )
}

/** A lettering-piece: a separate leather label stamped in gilt, as the 1500s–1700s set their titles. */
@Composable
private fun TitleLabel(title: String, labelColor: Color) {
    Box(
        Modifier.fillMaxWidth().background(labelColor).border(0.5.dp, CoverGilt.copy(alpha = 0.7f)).padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold),
            color = CoverGilt,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PeriodOrnament(binding: PeriodBinding) {
    val color = CoverGilt.copy(alpha = 0.85f)
    when (binding) {
        PeriodBinding.EARLY_CODEX -> Box(Modifier.size(10.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.width(10.dp).height(1.75.dp).background(color))
            Box(Modifier.width(1.75.dp).height(10.dp).background(color))
        }
        PeriodBinding.MEDIEVAL_BOARDS -> Box(Modifier.size(10.dp)) { Quatrefoil(color) }
        PeriodBinding.REFORMATION_BLIND -> Box(Modifier.size(6.dp).rotate(45f).background(Color.Black.copy(alpha = 0.35f)))
        PeriodBinding.VICTORIAN_CLOTH -> Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { Box(Modifier.size(2.5.dp).clip(CircleShape).background(color)) }
        }
        else -> Box(Modifier.size(5.dp).rotate(45f).background(color))
    }
}

@Composable
private fun BoxScope.Quatrefoil(color: Color) {
    listOf(Alignment.TopCenter, Alignment.BottomCenter, Alignment.CenterStart, Alignment.CenterEnd).forEach { position ->
        Box(Modifier.align(position).size(4.dp).clip(CircleShape).background(color))
    }
}

// endregion
