package com.mediasage.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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

/** Grows from small with a bouncy overshoot. */
@Composable
internal fun Modifier.popIn(visible: Boolean): Modifier {
    val scale by animateFloatAsState(if (visible) 1f else POP_START_SCALE, BouncySpring, label = "popScale")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(FADE_MILLIS), label = "popAlpha")
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/** Falls into place from above and bounces as it lands. */
@Composable
internal fun Modifier.dropIn(visible: Boolean): Modifier {
    val lift by animateFloatAsState(if (visible) 0f else 1f, BouncySpring, label = "dropLift")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(FADE_MILLIS), label = "dropAlpha")
    return graphicsLayer {
        translationY = -lift * DropDistance.toPx()
        this.alpha = alpha
    }
}

/** Rises a little into place, keeping its space reserved so nothing below jumps. */
@Composable
internal fun Modifier.riseIn(visible: Boolean): Modifier {
    val lift by animateFloatAsState(if (visible) 0f else 1f, BouncySpring, label = "riseLift")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(FADE_MILLIS), label = "riseAlpha")
    return graphicsLayer {
        translationY = lift * RiseDistance.toPx()
        this.alpha = alpha
    }
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

/** Placeholder reflection text that types itself out again each time [key] changes. */
@Composable
internal fun ReflectionLines(key: Any, modifier: Modifier = Modifier) {
    val isPreview = LocalInspectionMode.current
    val lineColor = MediaSageTheme.colors.ruleLine
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1f, 0.9f, 0.6f).forEachIndexed { index, fraction ->
            val progress = remember(key) { Animatable(if (isPreview) 1f else 0f) }
            LaunchedEffect(key) {
                delay(index * PHASE_MILLIS / 3)
                progress.animateTo(1f, tween(durationMillis = 500))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction * progress.value)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(lineColor),
            )
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
