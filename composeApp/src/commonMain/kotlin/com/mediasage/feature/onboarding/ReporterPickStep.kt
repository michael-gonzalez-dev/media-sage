package com.mediasage.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.mediasage.feature.figures.DeckCard
import com.mediasage.feature.figures.ReporterDeck
import com.mediasage.feature.figures.ReporterDeckLayout
import com.mediasage.feature.figures.ReporterPortraitStrip
import com.mediasage.theme.BrandAmber
import com.mediasage.ui.EraChipRow
import com.mediasage.ui.labelRes
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.onboarding_pick_body
import mediasage.composeapp.generated.resources.onboarding_pick_loading
import mediasage.composeapp.generated.resources.onboarding_pick_selection
import mediasage.composeapp.generated.resources.onboarding_pick_title
import mediasage.composeapp.generated.resources.reporter_card_today_pick
import org.jetbrains.compose.resources.stringResource

private val StepVerticalPadding = 4.dp

/**
 * Keeps the step's own sideways swipes (cards, era chips, portraits) from turning the onboarding step when they reach
 * the end of their row. A swipe on the title and body has no row under it, so it still turns the step.
 */
private val KeepSidewaysSwipes = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = Offset(available.x, 0f)
    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = Velocity(available.x, 0f)
}

/**
 * The step after the briefing: one big reporter card at a time, era chips to narrow the deck, and a portrait strip to
 * jump anywhere in it. Tapping a lens on a card's back picks that reporter and lens for today. The step scrolls as a
 * whole, so at the largest text size nothing is cut off. Swiping the title moves to the next or previous step.
 */
@Composable
internal fun ReporterPickStep(state: OnboardingContract.UiState, onIntent: (OnboardingContract.Intent) -> Unit) {
    val cards = deckCards(state.deck, state.selection)
    val cardsState = rememberUpdatedState(cards)
    val selectedId = state.selection?.reporterId
    val selectedIndex = cards.indexOfFirst { it.info.id == selectedId }.coerceAtLeast(0)
    // Opens straight on today's reporter, so the step's first frame builds one set of cards rather than two.
    val pagerState = rememberPagerState(initialPage = selectedIndex) { cardsState.value.size }
    // Lands on today's reporter again when the era or the selection changes (or on the first card if they're outside it).
    LaunchedEffect(state.selectedEra, selectedId, cards.isNotEmpty()) {
        if (pagerState.currentPage != selectedIndex) pagerState.scrollToPage(selectedIndex)
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableHeight = maxHeight - StepVerticalPadding * 2
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(KeepSidewaysSwipes)
                .verticalScroll(rememberScrollState())
                .padding(vertical = StepVerticalPadding),
        ) {
            ReporterDeckLayout(
                availableHeight = availableHeight,
                top = {
                    PickHeader()
                    EraChipRow(
                        selectedEra = state.selectedEra,
                        onEraSelected = { onIntent(OnboardingContract.Intent.SelectEra(it)) },
                        contentPadding = PaddingValues(horizontal = PageSidePadding, vertical = 8.dp),
                    )
                },
                deck = {
                    ReporterDeck(
                        cards = cardsState,
                        pagerState = pagerState,
                        onLensSelected = { id, lens -> onIntent(OnboardingContract.Intent.SelectReporter(id, lens)) },
                    )
                },
                bottom = {
                    ReporterPortraitStrip(cards, pagerState, contentPadding = PaddingValues(horizontal = PageSidePadding))
                    SelectionLine(state.selectedReporter, state.selection)
                },
            )
        }
    }
}

/** Today's pick carries the tag, the amber border and its lens; every other card is plain. */
@Composable
private fun deckCards(deck: List<OnboardingContract.PickReporter>, selection: OnboardingContract.PickSelection?): List<DeckCard> {
    val todayTag = stringResource(Res.string.reporter_card_today_pick)
    return deck.map { reporter ->
        val isSelected = reporter.card.id == selection?.reporterId
        DeckCard(
            info = reporter.card,
            tag = todayTag.takeIf { isSelected },
            isHighlighted = isSelected,
            selectedLens = selection?.lens?.takeIf { isSelected },
        )
    }
}

@Composable
private fun PickHeader() {
    Column(modifier = Modifier.padding(horizontal = PageSidePadding), horizontalAlignment = Alignment.CenterHorizontally) {
        StepTitle(stringResource(Res.string.onboarding_pick_title))
        Text(
            text = stringResource(Res.string.onboarding_pick_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** Always shows today's pick, even when that reporter is outside the current era or scrolled away. Read out when it changes. */
@Composable
private fun SelectionLine(reporter: OnboardingContract.PickReporter?, selection: OnboardingContract.PickSelection?) {
    val text = if (reporter != null && selection != null) {
        stringResource(Res.string.onboarding_pick_selection, reporter.card.name, stringResource(selection.lens.labelRes()))
    } else {
        stringResource(Res.string.onboarding_pick_loading)
    }
    Row(
        modifier = Modifier.padding(vertical = 6.dp).semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandAmber))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
    }
}
