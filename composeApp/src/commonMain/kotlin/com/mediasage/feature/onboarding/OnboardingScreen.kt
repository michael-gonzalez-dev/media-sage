package com.mediasage.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mediasage.theme.BrandAmber
import com.mediasage.theme.rememberComicSurfaceColors
import com.mediasage.ui.MediaSageComicChip
import com.mediasage.theme.MediaSageTheme
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.onboarding_back
import mediasage.composeapp.generated.resources.onboarding_briefing_ai
import mediasage.composeapp.generated.resources.onboarding_briefing_disclosure_pointer
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
import org.jetbrains.compose.resources.stringResource

private const val STEP_SCALE = 0.94f

@Composable
fun OnboardingScreen(
    state: OnboardingContract.UiState,
    onIntent: (OnboardingContract.Intent) -> Unit,
) {
    // Same page colour the main tabs sit on (MediaSageScaffold uses surface, not background).
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            OnboardingTopBar(state = state, onIntent = onIntent)
            AnimatedContent(
                targetState = state.currentIndex,
                transitionSpec = { stepTransition() },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "onboardingStep",
            ) { index ->
                OnboardingStepContent(step = state.steps[index], state = state)
            }
            MediaSageComicChip(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                label = stringResource(if (state.isLastStep) Res.string.onboarding_finish else Res.string.onboarding_continue),
                onClick = { onIntent(OnboardingContract.Intent.Continue) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                centered = true,
            )
        }
    }
}

// Forward slides the next step in from the end; Back slides the previous one in from the start.
private fun AnimatedContentTransitionScope<Int>.stepTransition(): ContentTransform {
    val direction = if (targetState > initialState) 1 else -1
    val slide = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )
    return (slideInHorizontally(slide) { width -> direction * width / 2 } + scaleIn(initialScale = STEP_SCALE) + fadeIn())
        .togetherWith(slideOutHorizontally(slide) { width -> -direction * width / 2 } + scaleOut(targetScale = STEP_SCALE) + fadeOut())
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

/** One segment per step; the current one stretches wider. Screen readers hear "Step N of 3", re-announced as the step changes. */
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
private fun OnboardingStepContent(step: OnboardingContract.Step, state: OnboardingContract.UiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (step) {
            OnboardingContract.Step.BRIEFING -> BriefingStep(featured = state.briefingFigure)
            OnboardingContract.Step.HEADLINES -> HeadlinesStep(featured = state.headlineFigure)
            OnboardingContract.Step.READER -> ReaderStep(figures = state.rosterFigures, quoteFigure = state.headlineFigure)
        }
    }
}

@Composable
private fun BriefingStep(featured: OnboardingContract.SampleFigure) {
    BriefingStepAnimation(featured = featured)
    StepTitle(stringResource(Res.string.onboarding_briefing_title))
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
    Text(
        text = stringResource(Res.string.onboarding_briefing_disclosure_pointer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun HeadlinesStep(featured: OnboardingContract.SampleFigure) {
    HeadlinesStepAnimation(featured = featured)
    StepTitle(stringResource(Res.string.onboarding_headlines_title))
    StepBody(stringResource(Res.string.onboarding_headlines_body))
}

@Composable
private fun ReaderStep(figures: List<OnboardingContract.SampleFigure>, quoteFigure: OnboardingContract.SampleFigure) {
    ReaderStepAnimation(figures = figures, quoteFigure = quoteFigure)
    StepTitle(stringResource(Res.string.onboarding_reader_title))
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
