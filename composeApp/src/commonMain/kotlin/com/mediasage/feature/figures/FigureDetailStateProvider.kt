package com.mediasage.feature.figures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class FigureDetailStateProvider : PreviewParameterProvider<FigureDetailContract.UiState> {
    override val values = sequenceOf(
        FigureDetailContract.UiState.Loading,
        FigureDetailContract.UiState.Success(
            figureName = "C.S. Lewis",
            figureRole = "Author & Apologist",
            figureImageUrl = null,
            bio = "Clive Staples Lewis (1898–1963) was a British writer, literary scholar, and lay theologian. " +
                "He held academic positions at both Oxford and Cambridge and is best known for his works of " +
                "fiction, including The Chronicles of Narnia and The Screwtape Letters, as well as his " +
                "Christian apologetics such as Mere Christianity and The Problem of Pain.",
            quotes = listOf(
                FigureQuoteItem(
                    "Hardships often prepare ordinary people for an extraordinary destiny.",
                    "The Weight of Glory (1941)"
                ),
                FigureQuoteItem(
                    "You are never too old to set another goal or to dream a new dream.",
                    "Mere Christianity (1952)"
                ),
                FigureQuoteItem(
                    "We are what we believe we are.",
                    "The Problem of Pain (1940)"
                ),
            )
        ),
        FigureDetailContract.UiState.Success(
            figureName = "Dietrich Bonhoeffer",
            figureRole = "Theologian & Martyr",
            figureImageUrl = null,
            bio = null,
            quotes = listOf(
                FigureQuoteItem(
                    "Silence in the face of evil is itself evil. Not to speak is to speak.",
                    "The Cost of Discipleship (1937)"
                )
            )
        ),
        FigureDetailContract.UiState.Success(
            figureName = "Julian of Norwich",
            figureRole = "Mystic & Anchoress",
            figureImageUrl = null,
            bio = "Julian of Norwich (c. 1343 – c. 1416) was an English anchoress and Christian mystic, " +
                "best known for Revelations of Divine Love, the earliest surviving book in English written " +
                "by a woman.",
            quotes = emptyList()
        ),
        FigureDetailContract.UiState.Error("Something went wrong. Please try again.")
    )
}
