package com.mediasage.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.domain.model.LensFilter
import com.mediasage.feature.you.LensBadge
import com.mediasage.theme.MediaSageTheme
import com.mediasage.ui.MediaSageHeadlineCard
import com.mediasage.ui.MediaSageScriptureBlock
import com.mediasage.ui.QuoteCard
import com.mediasage.ui.ThemeChip
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.onboarding_sample_headline
import mediasage.composeapp.generated.resources.onboarding_sample_headline_category
import mediasage.composeapp.generated.resources.onboarding_sample_quote
import mediasage.composeapp.generated.resources.onboarding_sample_scripture_hope_reference
import mediasage.composeapp.generated.resources.onboarding_sample_scripture_hope_text
import mediasage.composeapp.generated.resources.onboarding_sample_scripture_news_reference
import mediasage.composeapp.generated.resources.onboarding_sample_scripture_news_text
import mediasage.composeapp.generated.resources.onboarding_sample_scripture_writings_reference
import mediasage.composeapp.generated.resources.onboarding_sample_scripture_writings_text
import org.jetbrains.compose.resources.stringResource

// Each step animates its feature with the app's own UI pieces. The animations are decorative —
// the step's title and body carry the meaning — so they are hidden from screen readers rather
// than read out as sample headlines and quotes.

private const val LENS_CYCLE_MILLIS = 2_200L
private const val TAP_DELAY_MILLIS = 1_500L
private const val PRESS_MILLIS = 800L
private const val DETAIL_HOLD_MILLIS = 5_500L
private const val CARD_HOLD_MILLIS = 2_500L
private const val PRESSED_SCALE = 0.95f
private const val SWAP_FADE_MILLIS = 600
private val BriefingLenses = listOf(LensFilter.NEWS, LensFilter.WRITINGS, LensFilter.HOPE)
private val RosterLenses = listOf(LensFilter.NEWS, LensFilter.GRACE, LensFilter.HOPE)
private val RosterDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)

// Bundled so the card renders instantly and offline. Source: "Sailors rebuild a home damaged by
// Hurricane Katrina", MC2 John P. Curtis, U.S. Navy, 2011 — a U.S. federal government work in the
// public domain (Wikimedia Commons: File:US_Navy_110412-N-SD120-002_Sailors_rebuild_a_home_damaged_by_Hurricane_Katrina.jpg).
private const val SAMPLE_HEADLINE_IMAGE = "drawable/onboarding_headline_rebuild.jpg"

/** The portrait pops in, then the lens chip cycles and the scripture and reflection change with it. */
@Composable
internal fun BriefingStepAnimation(featured: OnboardingContract.SampleFigure) {
    val phase = rememberEntrancePhase(lastPhase = 2)
    val lens = BriefingLenses[rememberLoopingIndex(count = BriefingLenses.size, periodMillis = LENS_CYCLE_MILLIS)]
    Column(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PopIn(visible = phase >= 1) {
            SamplePortrait(figure = featured, size = 140.dp, shape = MaterialTheme.shapes.small, sepia = true)
        }
        RiseIn(visible = phase >= 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = featured.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(
                    targetState = lens,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "lensChip",
                ) { ThemeChip(theme = it.name) }
            }
        }
        RiseIn(visible = phase >= 2) {
            AnimatedContent(
                targetState = lens,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "lensScripture",
            ) { shown ->
                val (reference, text) = shown.sampleScripture()
                MediaSageScriptureBlock(scriptureReference = reference, scriptureText = text)
            }
        }
        RiseIn(visible = phase >= 2) { ReflectionLines(key = lens) }
    }
}

@Composable
private fun LensFilter.sampleScripture(): Pair<String, String> = when (this) {
    LensFilter.NEWS -> stringResource(Res.string.onboarding_sample_scripture_news_reference) to
        stringResource(Res.string.onboarding_sample_scripture_news_text)
    LensFilter.WRITINGS -> stringResource(Res.string.onboarding_sample_scripture_writings_reference) to
        stringResource(Res.string.onboarding_sample_scripture_writings_text)
    else -> stringResource(Res.string.onboarding_sample_scripture_hope_reference) to
        stringResource(Res.string.onboarding_sample_scripture_hope_text)
}

private enum class HeadlinePhase { CARD, TAPPED, DETAIL }

/** Loops: a headline card is tapped, grows into the story's detail, holds, then returns to the card. */
@Composable
private fun rememberHeadlinePhase(): Pair<HeadlinePhase, Int> {
    val isPreview = LocalInspectionMode.current
    var phase by remember { mutableStateOf(if (isPreview) HeadlinePhase.DETAIL else HeadlinePhase.CARD) }
    var taps by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        delay(TAP_DELAY_MILLIS)
        while (true) {
            phase = HeadlinePhase.TAPPED
            taps++
            delay(PRESS_MILLIS)
            phase = HeadlinePhase.DETAIL
            delay(DETAIL_HOLD_MILLIS)
            phase = HeadlinePhase.CARD
            delay(CARD_HOLD_MILLIS)
        }
    }
    return phase to taps
}

@Composable
internal fun HeadlinesStepAnimation(featured: OnboardingContract.SampleFigure) {
    val appeared = rememberEntrancePhase(lastPhase = 1) >= 1
    val (phase, taps) = rememberHeadlinePhase()
    val pressScale by animateFloatAsState(
        targetValue = if (phase == HeadlinePhase.TAPPED) PRESSED_SCALE else 1f,
        animationSpec = BouncySpring,
        label = "headlinePress",
    )
    PopIn(visible = appeared, modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                },
            ) {
                HeadlineToDetail(showDetail = phase == HeadlinePhase.DETAIL, featured = featured)
            }
            TapRipple(tapCount = taps)
        }
    }
}

@Composable
private fun HeadlineToDetail(showDetail: Boolean, featured: OnboardingContract.SampleFigure) {
    AnimatedContent(
        targetState = showDetail,
        transitionSpec = {
            val fade = fadeIn(tween(SWAP_FADE_MILLIS))
            val enter = if (targetState) scaleIn(initialScale = 0.85f, animationSpec = BouncySpring) + fade else fade
            enter.togetherWith(scaleOut(targetScale = 0.9f) + fadeOut(tween(SWAP_FADE_MILLIS)))
                .using(SizeTransform(clip = false) { _, _ -> spring(stiffness = Spring.StiffnessVeryLow) })
        },
        label = "headlineToDetail",
    ) { detail ->
        if (detail) {
            HeadlineDetailMock(featured = featured)
        } else {
            MediaSageHeadlineCard(
                imageUrl = Res.getUri(SAMPLE_HEADLINE_IMAGE),
                headlineTitle = stringResource(Res.string.onboarding_sample_headline),
                grayscaleImage = false,
                onClick = {},
                category = stringResource(Res.string.onboarding_sample_headline_category),
            )
        }
    }
}

// A miniature of the headline detail screen: quote, reporter, scripture, then how it connects.
@Composable
private fun HeadlineDetailMock(featured: OnboardingContract.SampleFigure) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "“${stringResource(Res.string.onboarding_sample_quote)}”",
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            SamplePortrait(figure = featured, size = 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = featured.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        MediaSageScriptureBlock(
            scriptureReference = stringResource(Res.string.onboarding_sample_scripture_news_reference),
            scriptureText = stringResource(Res.string.onboarding_sample_scripture_news_text),
        )
        ReflectionLines(key = Unit)
    }
}

/** The week's roster drops in one reporter at a time, each with a lens, then a quote gets pinned. */
@Composable
internal fun ReaderStepAnimation(
    figures: List<OnboardingContract.SampleFigure>,
    quoteFigure: OnboardingContract.SampleFigure,
) {
    val roster = figures.take(RosterDays.size)
    val phase = rememberEntrancePhase(lastPhase = roster.size + 1)
    Column(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            roster.forEachIndexed { index, figure ->
                RosterDay(
                    day = RosterDays[index],
                    figure = figure,
                    lens = RosterLenses[index],
                    visible = phase >= index + 1,
                )
            }
        }
        // Room for the pin badge, which straddles the card's top-right corner.
        RiseIn(visible = phase >= 1, modifier = Modifier.padding(top = 18.dp, end = 18.dp)) {
            QuoteCard(
                quoteText = stringResource(Res.string.onboarding_sample_quote),
                isPinned = phase > roster.size,
                onPinQuote = {},
                footerText = quoteFigure.name,
            )
        }
    }
}

@Composable
private fun RosterDay(day: DayOfWeek, figure: OnboardingContract.SampleFigure, lens: LensFilter, visible: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = day.name.take(3),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DropIn(visible = visible) {
            Box(contentAlignment = Alignment.BottomEnd) {
                SamplePortrait(figure = figure, size = 64.dp)
                PopIn(visible = visible) {
                    LensBadge(
                        lens = lens,
                        modifier = Modifier.size(18.dp).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    )
                }
            }
        }
    }
}

// region Previews

private val PreviewState = OnboardingContract.UiState()

@Preview(showBackground = true)
@Composable
private fun BriefingStepAnimationPreview() {
    MediaSageTheme { BriefingStepAnimation(featured = PreviewState.briefingFigure) }
}

@Preview(showBackground = true)
@Composable
private fun HeadlinesStepAnimationPreview() {
    MediaSageTheme { HeadlinesStepAnimation(featured = PreviewState.headlineFigure) }
}

@Preview(showBackground = true)
@Composable
private fun ReaderStepAnimationPreview() {
    MediaSageTheme { ReaderStepAnimation(figures = PreviewState.rosterFigures, quoteFigure = PreviewState.headlineFigure) }
}

// endregion
