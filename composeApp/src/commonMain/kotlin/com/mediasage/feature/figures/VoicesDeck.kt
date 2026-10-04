package com.mediasage.feature.figures

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.reporter_card_today_pick
import mediasage.composeapp.generated.resources.reporter_card_weekday_pick
import org.jetbrains.compose.resources.stringResource

/**
 * The Reporters tab as a deck of reporter cards, opening on today's reporter, with the portrait strip under it. Tapping
 * a lens on a card's back picks that reporter and lens for today, and "Read more" opens the reporter's page. [header]
 * (title, search and era chips) scrolls with the deck, so at the largest text size the card gets the whole screen;
 * at other sizes everything fits and nothing scrolls. When the search or era leaves no reporters, [emptyState] takes
 * the card's place under the same header, so the search field stays put while the reader types.
 */
@Composable
internal fun VoicesDeck(
    state: FiguresContract.UiState.Success,
    onIntent: (FiguresContract.Intent) -> Unit,
    onReadMore: (reporterId: Long) -> Unit,
    header: @Composable () -> Unit,
    emptyState: @Composable () -> Unit,
) {
    val cards = voicesDeckCards(state)
    val cardsState = rememberUpdatedState(cards)
    val todayIndex = cards.indexOfFirst { it.info.id == state.todayPick?.figureId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = todayIndex) { cardsState.value.size }
    // Lands on today's reporter (or the first card when the search or era leaves them out) whenever the search or era
    // changes. The last landing is saved with the pager, so coming back from a reporter's page keeps the card the reader
    // left rather than jumping back to today's.
    val landingKey = "${state.searchQuery}|${state.selectedEra}|${cards.isNotEmpty()}"
    var landedFor by rememberSaveable { mutableStateOf(landingKey) }
    LaunchedEffect(landingKey) {
        if (landedFor != landingKey) {
            landedFor = landingKey
            pagerState.scrollToPage(todayIndex)
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableHeight = maxHeight
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ReporterDeckLayout(
                availableHeight = availableHeight,
                top = { Column(modifier = Modifier.fillMaxWidth()) { header() } },
                deck = {
                    if (cards.isEmpty()) {
                        emptyState()
                    } else {
                        ReporterDeck(
                            cards = cardsState,
                            pagerState = pagerState,
                            onLensSelected = { id, lens -> onIntent(FiguresContract.Intent.LensSelected(id, lens)) },
                            onReadMore = onReadMore,
                        )
                    }
                },
                bottom = {
                    if (cards.isNotEmpty()) {
                        ReporterPortraitStrip(cards, pagerState, contentPadding = PaddingValues(horizontal = 16.dp))
                    }
                },
            )
        }
    }
}

/**
 * Today's reporter carries "Today's pick", the amber border and today's lens. A reporter picked after today's briefing
 * was written carries "{Weekday}'s pick" and their lens, since they start next week.
 */
@Composable
private fun voicesDeckCards(state: FiguresContract.UiState.Success): List<DeckCard> {
    val todayTag = stringResource(Res.string.reporter_card_today_pick)
    val scheduled = state.scheduledPick
    val scheduledTag = scheduled?.let { stringResource(Res.string.reporter_card_weekday_pick, it.weekdayLabel) }
    return state.deck.map { figure ->
        val isToday = figure.id == state.todayPick?.figureId
        val isScheduled = figure.id == scheduled?.figureId
        DeckCard(
            info = figure.toCardInfo(),
            tag = if (isToday) todayTag else scheduledTag?.takeIf { isScheduled },
            isHighlighted = isToday,
            selectedLens = if (isToday) state.todayPick?.lens else scheduled?.lens?.takeIf { isScheduled },
        )
    }
}

private fun VoiceFigureItem.toCardInfo() = ReporterCardInfo(
    id = id,
    name = name,
    role = role,
    lifespan = lifespan,
    knownFor = knownFor,
    portraitUrl = imageUrl,
)
