package com.mediasage.feature.onboarding

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class OnboardingStateProvider : PreviewParameterProvider<OnboardingContract.UiState> {
    override val values = OnboardingContract.Step.entries.indices.asSequence()
        .map { index -> OnboardingContract.UiState(currentIndex = index) }
}
