package com.mediasage.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mediasage.theme.BrandAmber
import com.mediasage.theme.rememberComicSurfaceColors
import com.mediasage.ui.MediaSageComicChip
import com.mediasage.theme.MediaSageTheme
import kotlinx.coroutines.launch
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.onboarding_back
import mediasage.composeapp.generated.resources.onboarding_briefing_ai
import mediasage.composeapp.generated.resources.onboarding_briefing_intro
import mediasage.composeapp.generated.resources.onboarding_briefing_mission
import mediasage.composeapp.generated.resources.onboarding_briefing_title
import mediasage.composeapp.generated.resources.onboarding_continue
import mediasage.composeapp.generated.resources.onboarding_finish
import mediasage.composeapp.generated.resources.onboarding_headlines_body
import mediasage.composeapp.generated.resources.onboarding_headlines_title
import mediasage.composeapp.generated.resources.onboarding_progress
import mediasage.composeapp.generated.resources.onboarding_reader_body
import mediasage.composeapp.generated.resources.onboarding_reader_title
import mediasage.composeapp.generated.resources.onboarding_skip
import com.mediasage.ui.paperSurface
import mediasage.composeapp.generated.resources.login_background_comic
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Matches the login screen's overlay, which keeps the illustration from competing with the page.
private const val BACKDROP_SCRIM_ALPHA = 0.3f

@Composable
fun OnboardingScreen(
    state: OnboardingContract.UiState,
    onIntent: (OnboardingContract.Intent) -> Unit,
) {
    // Continues the login screen's look, the comic illustration under a dark overlay with the page on top, so sign-in
    // and first run read as one opening.
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_background_comic),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = BACKDROP_SCRIM_ALPHA)))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .onboardingPage()
                // The paper image has a thin see-through border (about 10 to 15dp on a full page), so this padding is
                // measured from the image's bounds, not the paper's visible edge. The extra space keeps the button
                // clear of the paper's bottom edge.
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 28.dp),
        ) {
            OnboardingTopBar(state = state, onIntent = onIntent)
            OnboardingStepPager(state = state, onIntent = onIntent, modifier = Modifier.weight(1f).fillMaxWidth())
            MediaSageComicChip(
                icon = null,
                label = stringResource(if (state.isLastStep) Res.string.onboarding_finish else Res.string.onboarding_continue),
                onClick = { onIntent(OnboardingContract.Intent.Continue) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                centered = true,
            )
        }
    }
}

/**
 * The page the steps sit on. Light mode uses the login screen's white newspaper paper. Dark mode uses the theme's own
 * dark surface instead, since the paper is always light and the steps' colours follow dark mode.
 */
@Composable
private fun Modifier.onboardingPage(): Modifier = if (MediaSageTheme.isDark) {
    shadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
} else {
    paperSurface()
}

/**
 * The steps as swipeable pages. The view model owns the current step: Continue, Back and Skip move the pager, and a
 * reader's swipe that settles on a page tells the view model. Swiping stops on the pick step, whose cards swipe sideways
 * themselves; Back is the way out of it.
 */
@Composable
private fun OnboardingStepPager(
    state: OnboardingContract.UiState,
    onIntent: (OnboardingContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = state.currentIndex) { state.steps.size }
    val currentOnIntent by rememberUpdatedState(onIntent)
    LaunchedEffect(state.currentIndex) {
        if (pagerState.currentPage != state.currentIndex) pagerState.animateScrollToPage(state.currentIndex)
    }
    // Only a reader's own drag is reported. Continue, Back and Skip already set the step, and an animation cut short
    // by a quick second tap could otherwise settle half-way and be reported as a step back.
    LaunchedEffect(pagerState) {
        var dragged = false
        launch { pagerState.interactionSource.interactions.collect { if (it is DragInteraction.Start) dragged = true } }
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (dragged) {
                dragged = false
                currentOnIntent(OnboardingContract.Intent.GoToStep(page))
            }
        }
    }
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = state.currentStep != OnboardingContract.Step.PICK,
        pageSpacing = 24.dp,
        modifier = modifier,
    ) { index ->
        OnboardingStepContent(step = state.steps[index], state = state, onIntent = onIntent)
    }
}

@Composable
private fun OnboardingTopBar(
    state: OnboardingContract.UiState,
    onIntent: (OnboardingContract.Intent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (!state.isFirstStep) {
                TextButton(onClick = { onIntent(OnboardingContract.Intent.Back) }, colors = inkTextButtonColors()) {
                    Text(stringResource(Res.string.onboarding_back))
                }
            }
        }
        StepProgressIndicator(currentIndex = state.currentIndex, stepCount = state.steps.size)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (state.currentStep.skippable) {
                TextButton(onClick = { onIntent(OnboardingContract.Intent.Skip) }, colors = inkTextButtonColors()) {
                    Text(stringResource(Res.string.onboarding_skip))
                }
            }
        }
    }
}

@Composable
private fun inkTextButtonColors() = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)

/** One segment per step; the current one stretches wider. Screen readers hear "Step N of 4", re-announced as the step changes. */
@Composable
private fun StepProgressIndicator(currentIndex: Int, stepCount: Int) {
    val description = stringResource(Res.string.onboarding_progress, currentIndex + 1, stepCount)
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = description
            liveRegion = LiveRegionMode.Polite
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(stepCount) { index ->
            val color by animateColorAsState(
                targetValue = if (index <= currentIndex) BrandAmber else MediaSageTheme.colors.ruleLine,
                label = "progressSegmentColor",
            )
            val width by animateDpAsState(
                targetValue = if (index == currentIndex) 36.dp else 16.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "progressSegmentWidth",
            )
            Box(modifier = Modifier.size(width = width, height = 6.dp).clip(CircleShape).background(color))
        }
    }
}

// Scrolls rather than clipping, so every line stays readable at the largest text size.
@Composable
private fun OnboardingStepContent(
    step: OnboardingContract.Step,
    state: OnboardingContract.UiState,
    onIntent: (OnboardingContract.Intent) -> Unit,
) {
    // The pick step lays out its own scrolling column around a fixed-height card deck.
    if (step == OnboardingContract.Step.PICK) {
        ReporterPickStep(state = state, onIntent = onIntent)
        return
    }
    // Centred in the space between the top bar and the button, so a short step doesn't leave a gap at the bottom.
    // Content taller than that space starts at the top and scrolls.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            when (step) {
                OnboardingContract.Step.BRIEFING -> BriefingStep(featured = state.briefingFigure)
                OnboardingContract.Step.HEADLINES -> HeadlinesStep(featured = state.headlineFigure)
                OnboardingContract.Step.READER -> ReaderStep(figures = state.rosterFigures, quoteFigure = state.headlineFigure)
                OnboardingContract.Step.PICK -> Unit
            }
        }
    }
}

@Composable
private fun BriefingStep(featured: OnboardingContract.SampleFigure) {
    // The welcome and introduction come first, so the animation shows what the reader has just been told.
    StepTitle(stringResource(Res.string.onboarding_briefing_title))
    StepBody(stringResource(Res.string.onboarding_briefing_intro))
    BriefingStepAnimation(featured = featured)
    StepBody(stringResource(Res.string.onboarding_briefing_mission))
    val comicColors = rememberComicSurfaceColors()
    Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).then(comicColors.background)) {
        Text(
            text = stringResource(Res.string.onboarding_briefing_ai),
            style = MaterialTheme.typography.bodyMedium,
            color = comicColors.content,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun HeadlinesStep(featured: OnboardingContract.SampleFigure) {
    // Title first on every step, so the animation shows what the reader has just been told.
    StepTitle(stringResource(Res.string.onboarding_headlines_title))
    HeadlinesStepAnimation(featured = featured)
    StepBody(stringResource(Res.string.onboarding_headlines_body))
}

@Composable
private fun ReaderStep(figures: List<OnboardingContract.SampleFigure>, quoteFigure: OnboardingContract.SampleFigure) {
    StepTitle(stringResource(Res.string.onboarding_reader_title))
    ReaderStepAnimation(figures = figures, quoteFigure = quoteFigure)
    StepBody(stringResource(Res.string.onboarding_reader_body))
}

@Composable
private fun StepTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun StepBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview(
    @PreviewParameter(OnboardingStateProvider::class) state: OnboardingContract.UiState,
) {
    MediaSageTheme {
        OnboardingScreen(state = state, onIntent = {})
    }
}

// endregion
