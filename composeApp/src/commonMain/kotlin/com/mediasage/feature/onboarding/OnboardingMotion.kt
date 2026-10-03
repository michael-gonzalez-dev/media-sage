package com.mediasage.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasage.theme.BrandAmber
import com.mediasage.theme.MediaSageTheme
import com.mediasage.ui.FigurePlaceholder
import com.mediasage.ui.SepiaColorFilter
import kotlinx.coroutines.delay

// Shared motion vocabulary for the onboarding step animations: springy pops and drops rather than
// plain fades, so the flow feels light and a little playful.

internal const val PHASE_MILLIS = 550L
private const val REVEAL_DELAY_MILLIS = 1_200L
private const val REVEAL_FADE_MILLIS = 400
private const val PLACEHOLDER_ALPHA_LOW = 0.08f
private const val PLACEHOLDER_ALPHA_HIGH = 0.22f
private const val PLACEHOLDER_PULSE_MILLIS = 600
private const val PLACEHOLDER_LAST_LINE_FRACTION = 0.6f
private const val REFLECTION_LINES = 3
private const val FADE_MILLIS = 200
private const val TAP_RIPPLE_MILLIS = 900
private const val POP_START_SCALE = 0.4f
private val DropDistance = 40.dp
private val RiseDistance = 20.dp

internal val BouncySpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

/**
 * Counts up from 0 to [lastPhase], one phase every [PHASE_MILLIS], once per appearance of the step.
 * IDE previews start on the last phase so they show the finished frame rather than an empty one.
 */
@Composable
internal fun rememberEntrancePhase(lastPhase: Int): Int {
    val isPreview = LocalInspectionMode.current
    var phase by remember { mutableIntStateOf(if (isPreview) lastPhase else 0) }
    LaunchedEffect(Unit) {
        while (phase < lastPhase) {
            delay(PHASE_MILLIS)
            phase++
        }
    }
    return phase
}

/** Cycles 0 until [count], advancing every [periodMillis] for as long as the step is on screen. */
@Composable
internal fun rememberLoopingIndex(count: Int, periodMillis: Long): Int {
    val isPreview = LocalInspectionMode.current
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) {
        while (!isPreview) {
            delay(periodMillis)
            index = (index + 1) % count
        }
    }
    return index
}

// The entrance wrappers below keep [content]'s space reserved while hidden, so nothing around it
// jumps when it appears. That's why they draw through graphicsLayer rather than AnimatedVisibility,
// which removes hidden content from the layout.

/** Grows [content] from small with a bouncy overshoot. */
@Composable
internal fun PopIn(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val scale by animateFloatAsState(if (visible) 1f else POP_START_SCALE, BouncySpring, label = "popScale")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(FADE_MILLIS), label = "popAlpha")
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        },
    ) { content() }
}

/** Drops [content] into place from above, bouncing as it lands. */
@Composable
internal fun DropIn(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val lift by animateFloatAsState(if (visible) 0f else 1f, BouncySpring, label = "dropLift")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(FADE_MILLIS), label = "dropAlpha")
    Box(
        modifier = modifier.graphicsLayer {
            translationY = -lift * DropDistance.toPx()
            this.alpha = alpha
        },
    ) { content() }
}

/** Raises [content] a little into place. */
@Composable
internal fun RiseIn(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val lift by animateFloatAsState(if (visible) 0f else 1f, BouncySpring, label = "riseLift")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(FADE_MILLIS), label = "riseAlpha")
    Box(
        modifier = modifier.graphicsLayer {
            translationY = lift * RiseDistance.toPx()
            this.alpha = alpha
        },
    ) { content() }
}

/** The reporter's real portrait once figures have synced; their initials until then. */
@Composable
internal fun SamplePortrait(
    figure: OnboardingContract.SampleFigure,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    sepia: Boolean = false,
) {
    if (figure.portraitUrl != null) {
        AsyncImage(
            model = figure.portraitUrl,
            contentDescription = null,
            modifier = modifier.size(size).clip(shape),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            colorFilter = if (sepia) SepiaColorFilter else null,
        )
    } else {
        FigurePlaceholder(name = figure.name, size = size, modifier = modifier)
    }
}

/**
 * A sample reflection that arrives whole: a brief placeholder, then the full text fades in. It never types itself out,
 * which would read as a machine writing live. It always takes the same height, so nothing around it moves. It plays
 * again whenever [text] changes. The placeholder holds until [started], so a reflection that rises into view later
 * still shows its loading bars rather than arriving already revealed.
 */
@Composable
internal fun RevealedReflection(text: String, modifier: Modifier = Modifier, started: Boolean = true) {
    val isPreview = LocalInspectionMode.current
    val reveal = remember { Animatable(if (isPreview) 1f else 0f) }
    LaunchedEffect(text, started) {
        if (isPreview) return@LaunchedEffect
        reveal.snapTo(0f)
        if (!started) return@LaunchedEffect
        delay(REVEAL_DELAY_MILLIS)
        reveal.animateTo(1f, tween(REVEAL_FADE_MILLIS))
    }
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = REFLECTION_LINES,
            maxLines = REFLECTION_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = reveal.value },
        )
        ReflectionPlaceholder(modifier = Modifier.matchParentSize().graphicsLayer { alpha = 1f - reveal.value })
    }
}

/** Soft bars in place of the reflection's lines, the last one shorter, gently pulsing like a paragraph still loading. */
@Composable
private fun ReflectionPlaceholder(modifier: Modifier) {
    val pulse by rememberInfiniteTransition(label = "placeholderPulse").animateFloat(
        initialValue = PLACEHOLDER_ALPHA_LOW,
        targetValue = PLACEHOLDER_ALPHA_HIGH,
        animationSpec = infiniteRepeatable(tween(PLACEHOLDER_PULSE_MILLIS), RepeatMode.Reverse),
        label = "placeholderAlpha",
    )
    val barColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier.graphicsLayer { alpha = pulse }, verticalArrangement = Arrangement.SpaceEvenly) {
        repeat(REFLECTION_LINES) { line ->
            val width = if (line == REFLECTION_LINES - 1) PLACEHOLDER_LAST_LINE_FRACTION else 1f
            Box(modifier = Modifier.fillMaxWidth(width).height(10.dp).clip(CircleShape).background(barColor))
        }
    }
}

/** An expanding, fading ring that marks a simulated tap, replayed each time [tapCount] changes. */
@Composable
internal fun TapRipple(tapCount: Int, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(TAP_RIPPLE_MILLIS))
        }
    }
    Canvas(modifier = modifier.size(72.dp)) {
        drawCircle(
            color = BrandAmber.copy(alpha = (1f - progress.value) * 0.6f),
            radius = size.minDimension / 2 * (0.3f + 0.7f * progress.value),
        )
    }
}
