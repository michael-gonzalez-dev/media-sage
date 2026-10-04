package com.mediasage.feature.onboarding

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.mediasage.domain.model.FigureEra
import com.mediasage.domain.model.LensFilter
import com.mediasage.feature.figures.ReporterCardInfo

private val sampleReporters = listOf(
    OnboardingContract.PickReporter(
        card = ReporterCardInfo(
            id = 68L,
            name = "C.S. Lewis",
            role = "Author & Apologist",
            lifespan = "1898-1963",
            knownFor = "Lewis wrote Mere Christianity and The Chronicles of Narnia, which have brought Christian faith " +
                "to millions of readers.",
            portraitUrl = null,
        ),
        era = FigureEra.MODERN,
    ),
    OnboardingContract.PickReporter(
        card = ReporterCardInfo(
            id = 58L,
            name = "Corrie ten Boom",
            role = "Holocaust Survivor & Evangelist",
            lifespan = "1892-1983",
            knownFor = "Corrie ten Boom survived a Nazi camp after hiding Jewish people and spent the rest of her life " +
                "preaching forgiveness around the world.",
            portraitUrl = null,
        ),
        era = FigureEra.MODERN,
    ),
)

internal class OnboardingStateProvider : PreviewParameterProvider<OnboardingContract.UiState> {
    override val values = OnboardingContract.Step.entries.indices.asSequence()
        .map { index ->
            OnboardingContract.UiState(
                currentIndex = index,
                reporters = sampleReporters,
                selection = OnboardingContract.PickSelection(reporterId = 68L, lens = LensFilter.HOPE),
            )
        }
}
