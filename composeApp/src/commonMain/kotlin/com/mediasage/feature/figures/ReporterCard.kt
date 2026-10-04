package com.mediasage.feature.figures

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasage.domain.model.LensFilter
import com.mediasage.theme.BrandAmber
import com.mediasage.theme.MediaSageTheme
import com.mediasage.ui.FigurePlaceholder
import com.mediasage.ui.color
import com.mediasage.ui.labelRes
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.reporter_card_lenses
import mediasage.composeapp.generated.resources.reporter_card_read_more
import mediasage.composeapp.generated.resources.reporter_card_role_lifespan
import mediasage.composeapp.generated.resources.reporter_card_show_back
import mediasage.composeapp.generated.resources.reporter_card_show_front
import org.jetbrains.compose.resources.stringResource

/** What a [ReporterCard] shows. [lenses] are the lenses the reporter can brief through, one badge each. */
data class ReporterCardInfo(
    val id: Long,
    val name: String,
    val role: String,
    val lifespan: String,
    val knownFor: String,
    val portraitUrl: String?,
    val lenses: List<LensFilter> = LensFilter.entries,
)

private const val FLIP_DURATION_MS = 450
private const val CAMERA_DISTANCE = 12f
private const val LIGHT_FILL_LUMINANCE = 0.4f
private val CornerInset = 12.dp
private val FlipIconSize = 20.dp
private val FlipHintPadding = 6.dp
private val FlipHintSize = FlipIconSize + FlipHintPadding * 2

// The portrait sits under both faces, so the scrims stay dark in every theme to keep the white text readable.
private val FrontScrim = Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.88f))
private val BackScrim = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.62f), Color.Black.copy(alpha = 0.92f)))

/**
 * A reporter's trading card. The front is the full portrait with the name, role and lifespan; the back is
 * drawn over the darkened portrait with the "known for" sentence and lens badges. The card fills the size
 * its caller gives it, so both faces always match, and the back scrolls if its text runs longer. Only the
 * face showing is composed, so a screen reader reads just that face. Tapping anywhere on the card flips it.
 * Badges are display-only unless [onLensSelected] is given. The selected lens sits beside the badges' heading, where it
 * reads without scrolling the back, and the badges below it are the other lenses to switch to. When [isOnScreen] turns
 * false (a deck moved the card out of view), the card returns to its front at once, without the flip animation, so the
 * reader never sees it turn. [border] replaces the default outline and shows on both faces; [tag] shows at the top start
 * of the front only, beside a flip hint. Both are drawn on the card, so they turn with it. [onReadMore] adds a "Read more" button under the
 * back's "known for" sentence; it takes its own tap, so it doesn't flip the card.
 */
@Composable
fun ReporterCard(
    info: ReporterCardInfo,
    modifier: Modifier = Modifier,
    selectedLens: LensFilter? = null,
    onLensSelected: ((LensFilter) -> Unit)? = null,
    isOnScreen: Boolean = true,
    border: BorderStroke? = null,
    tag: String? = null,
    onReadMore: (() -> Unit)? = null,
) {
    var flipped by rememberSaveable(info.id) { mutableStateOf(false) }
    LaunchedEffect(isOnScreen) { if (!isOnScreen) flipped = false }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = if (isOnScreen) tween(FLIP_DURATION_MS) else snap(),
        label = "reporterCardFlip",
    )
    val flipLabel = stringResource(if (flipped) Res.string.reporter_card_show_front else Res.string.reporter_card_show_back)
    val shape = MaterialTheme.shapes.large
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = CAMERA_DISTANCE * density
                }
                .clip(shape)
                // The whole card flips. A lens badge on the back takes its own tap, so picking a lens doesn't flip it.
                .clickable(onClickLabel = flipLabel, role = Role.Button) { flipped = !flipped }
                .border(border ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape),
        ) {
            if (rotation <= 90f) {
                ReporterCardFront(info)
                FrontCorners(tag)
            } else {
                // Pre-flipped so the back reads the right way round once the card has turned. The back has no corners: like
                // a sticker on a real card, the tag stays on the front, and a reader looking at the back already knows a tap
                // flips it, so its text can start at the top.
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                    ReporterCardBack(info, selectedLens, onLensSelected, onReadMore)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.Portrait(info: ReporterCardInfo, scrim: Brush) {
    if (info.portraitUrl != null) {
        AsyncImage(
            model = info.portraitUrl,
            // The name is read from the text on the card.
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize(),
        )
    } else {
        Box(modifier = Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            FigurePlaceholder(name = info.name, size = 96.dp)
        }
    }
    Box(modifier = Modifier.matchParentSize().background(scrim))
}

@Composable
private fun BoxScope.ReporterCardFront(info: ReporterCardInfo) {
    Portrait(info, FrontScrim)
    Column(modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)) {
        Text(info.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(info.role, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = Color.White.copy(alpha = 0.88f))
        Text(info.lifespan, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.72f))
    }
}

@Composable
private fun BoxScope.ReporterCardBack(
    info: ReporterCardInfo,
    selectedLens: LensFilter?,
    onLensSelected: ((LensFilter) -> Unit)?,
    onReadMore: (() -> Unit)? = null,
) {
    Portrait(info, BackScrim)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // At least the card's height, so the badges sit at the bottom; taller content scrolls instead of being cut off.
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(info.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = stringResource(Res.string.reporter_card_role_lifespan, info.role, info.lifespan),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.85f),
            )
            if (info.knownFor.isNotBlank()) {
                Text(info.knownFor, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.padding(top = 4.dp))
            }
            onReadMore?.let { ReadMoreButton(it) }
            Spacer(modifier = Modifier.weight(1f))
            LensBadges(info.lenses, selectedLens, onLensSelected)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LensBadges(lenses: List<LensFilter>, selectedLens: LensFilter?, onLensSelected: ((LensFilter) -> Unit)?) {
    if (lenses.isEmpty()) return
    Row(
        modifier = Modifier.padding(top = 8.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(Res.string.reporter_card_lenses),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        selectedLens?.let { LensBadge(it, selected = true, onClick = null) }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lenses.filter { it != selectedLens }.forEach { lens -> LensBadge(lens, selected = false, onClick = onLensSelected?.let { { it(lens) } }) }
    }
}

@Composable
private fun LensBadge(lens: LensFilter, selected: Boolean, onClick: (() -> Unit)?) {
    val lensColor = lens.cardColor()
    Text(
        text = stringResource(lens.labelRes()),
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) readableOn(lensColor) else Color.White,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) lensColor else Color.Transparent)
            .border(1.dp, lensColor.copy(alpha = if (selected) 1f else 0.85f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics { this.selected = selected }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun ReadMoreButton(onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
        Text(
            text = stringResource(Res.string.reporter_card_read_more),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandAmber,
        )
    }
}

// The card's back is dark in every theme, but Headlines takes the theme's primary colour, which is navy in light themes
// and would vanish there. On the card it uses the amber of the tag and border instead; the other lenses keep their own.
@Composable
private fun LensFilter.cardColor(): Color = if (this == LensFilter.NEWS) BrandAmber else color()

// Some lens fills are light (amber, gold) and some dark, so the text follows the fill.
private fun readableOn(fill: Color): Color = if (fill.luminance() > LIGHT_FILL_LUMINANCE) Color.Black else Color.White

// Drawn on the front face, so the tag and the flip hint turn away with it. The tag stops short of the hint, so at large
// text sizes it wraps rather than running under it.
@Composable
private fun BoxScope.FrontCorners(tag: String?) {
    tag?.let {
        CardTag(it, Modifier.align(Alignment.TopStart).padding(start = CornerInset, top = CornerInset, end = CornerInset * 2 + FlipHintSize))
    }
    FlipHint(modifier = Modifier.align(Alignment.TopEnd).padding(CornerInset))
}

@Composable
private fun CardTag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = modifier.clip(CircleShape).background(BrandAmber).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Shows the card can be flipped. Not a button of its own: taps fall through to the card, which is the one flip control. */
@Composable
private fun FlipHint(modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.45f), modifier = modifier) {
        Icon(Icons.Outlined.Autorenew, contentDescription = null, tint = Color.White, modifier = Modifier.padding(FlipHintPadding).size(FlipIconSize))
    }
}

// region Previews

private val previewInfo = ReporterCardInfo(
    id = 68L,
    name = "C.S. Lewis",
    role = "Author & Apologist",
    lifespan = "1898-1963",
    knownFor = "Lewis wrote Mere Christianity and The Chronicles of Narnia, which have brought Christian faith " +
        "to millions of readers.",
    portraitUrl = null,
)

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
private fun ReporterCardPreview() {
    MediaSageTheme {
        ReporterCard(info = previewInfo, modifier = Modifier.fillMaxSize().padding(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
private fun ReporterCardBackPreview() {
    MediaSageTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp).clip(MaterialTheme.shapes.large)) {
            ReporterCardBack(previewInfo, selectedLens = LensFilter.HOPE, onLensSelected = {}, onReadMore = {})
        }
    }
}

// endregion
