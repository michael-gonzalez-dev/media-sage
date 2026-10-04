package com.mediasage.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasage.feature.figures.ReporterCard
import com.mediasage.theme.BrandAmber
import com.mediasage.ui.EraChipRow
import com.mediasage.ui.FigurePlaceholder
import com.mediasage.ui.labelRes
import kotlinx.coroutines.launch
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.onboarding_pick_body
import mediasage.composeapp.generated.resources.onboarding_pick_jump
import mediasage.composeapp.generated.resources.onboarding_pick_loading
import mediasage.composeapp.generated.resources.onboarding_pick_selection
import mediasage.composeapp.generated.resources.onboarding_pick_tag
import mediasage.composeapp.generated.resources.onboarding_pick_title
import org.jetbrains.compose.resources.stringResource

private const val STRIP_LEAD_ITEMS = 2
private const val CARD_ASPECT_RATIO = 5f / 7f // a 2.5 x 3.5 inch trading card
private val MinCardHeight = 300.dp
private val MinCardPeek = 32.dp
private val StepVerticalPadding = 4.dp
private val StripPortraitSize = 44.dp

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
    val deck = state.deck
    // The pager's page count, keys and pages all read this one state, so they always agree on the deck. Read
    // separately, an era chip could shrink the deck between the count and the key lookup and index past its end.
    val deckState = rememberUpdatedState(deck)
    val selectedId = state.selection?.reporterId
    val selectedIndex = deck.indexOfFirst { it.card.id == selectedId }.coerceAtLeast(0)
    // Opens straight on today's reporter, so the step's first frame builds one set of cards rather than two.
    val pagerState = rememberPagerState(initialPage = selectedIndex) { deckState.value.size }
    // Lands on today's reporter again when the era or the selection changes (or on the first card if they're outside it).
    LaunchedEffect(state.selectedEra, selectedId, deck.isNotEmpty()) {
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
            FillDeckLayout(
                availableHeight = availableHeight,
                top = {
                    PickHeader()
                    EraChipRow(
                        selectedEra = state.selectedEra,
                        onEraSelected = { onIntent(OnboardingContract.Intent.SelectEra(it)) },
                        contentPadding = PaddingValues(horizontal = PageSidePadding, vertical = 8.dp),
                    )
                },
                deck = { ReporterDeck(deckState, state.selection, pagerState, onIntent) },
                bottom = {
                    PortraitStrip(deck, pagerState, selectedId)
                    SelectionLine(state.selectedReporter, state.selection)
                },
            )
        }
    }
}

/**
 * Stacks [top], [deck] and [bottom], measuring the top and bottom first and giving the deck whatever height is left,
 * so the strip and selection line always sit on screen under the card. The deck never goes below [MinCardHeight]; when
 * it would (large text), the stack grows past [availableHeight] and the step scrolls instead.
 */
@Composable
private fun FillDeckLayout(
    availableHeight: Dp,
    top: @Composable () -> Unit,
    deck: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
) {
    val centered: (@Composable () -> Unit) -> @Composable () -> Unit = { slot ->
        { Column(horizontalAlignment = Alignment.CenterHorizontally) { slot() } }
    }
    Layout(contents = listOf(centered(top), deck, centered(bottom))) { (topSlot, deckSlot, bottomSlot), constraints ->
        val wrap = Constraints(maxWidth = constraints.maxWidth)
        val topPlaced = topSlot.map { it.measure(wrap) }
        val bottomPlaced = bottomSlot.map { it.measure(wrap) }
        val used = topPlaced.sumOf { it.height } + bottomPlaced.sumOf { it.height }
        val deckHeight = (availableHeight.roundToPx() - used).coerceAtLeast(MinCardHeight.roundToPx())
        val deckPlaced = deckSlot.map { it.measure(Constraints.fixed(constraints.maxWidth, deckHeight)) }
        layout(constraints.maxWidth, used + deckHeight) {
            var y = 0
            (topPlaced + deckPlaced + bottomPlaced).forEach { placeable ->
                placeable.placeRelative((constraints.maxWidth - placeable.width) / 2, y)
                y += placeable.height
            }
        }
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

@Composable
private fun ReporterDeck(
    deck: State<List<OnboardingContract.PickReporter>>,
    selection: OnboardingContract.PickSelection?,
    pagerState: PagerState,
    onIntent: (OnboardingContract.Intent) -> Unit,
) = BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
    // Shaped like a real trading card: as wide as its height allows, never wider than the screen with a peek either side.
    val cardWidth = minOf(maxHeight * CARD_ASPECT_RATIO, maxWidth - MinCardPeek * 2)
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = (maxWidth - cardWidth) / 2),
        pageSpacing = 8.dp,
        key = { page -> deck.value[page].card.id },
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val card = deck.value[page].card
        val isSelected = card.id == selection?.reporterId
        ReporterCard(
            info = card,
            modifier = Modifier.fillMaxSize(),
            selectedLens = selection?.lens?.takeIf { isSelected },
            onLensSelected = { lens -> onIntent(OnboardingContract.Intent.SelectReporter(card.id, lens)) },
            // The settled page, not the current one: currentPage changes halfway through a swipe, while the old card
            // is still half on screen, so the reset would show. Once the swipe settles it is only a sliver at the edge.
            isCurrent = page == pagerState.settledPage,
            border = BorderStroke(3.dp, BrandAmber).takeIf { isSelected },
            tag = stringResource(Res.string.onboarding_pick_tag).takeIf { isSelected },
        )
    }
}

/** Small portraits of the deck. The card showing gets a dark ring and today's pick an amber one; tapping jumps to that card. */
@Composable
private fun PortraitStrip(deck: List<OnboardingContract.PickReporter>, pagerState: PagerState, selectedId: Long?) {
    val scope = rememberCoroutineScope()
    val stripState = rememberLazyListState(initialFirstVisibleItemIndex = stripLeadIndex(pagerState.currentPage))
    // Keeps the card showing in view as the reader swipes.
    LaunchedEffect(pagerState.currentPage) {
        stripState.animateScrollToItem(stripLeadIndex(pagerState.currentPage))
    }
    LazyRow(
        state = stripState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = PageSidePadding),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        itemsIndexed(deck, key = { _, reporter -> reporter.card.id }) { index, reporter ->
            val ring = when {
                index == pagerState.currentPage -> BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                reporter.card.id == selectedId -> BorderStroke(2.dp, BrandAmber)
                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            }
            val jumpLabel = stringResource(Res.string.onboarding_pick_jump, reporter.card.name)
            StripPortrait(
                name = reporter.card.name,
                portraitUrl = reporter.card.portraitUrl,
                modifier = Modifier
                    .size(StripPortraitSize)
                    .clip(CircleShape)
                    .border(ring, CircleShape)
                    .clickable(onClickLabel = jumpLabel, role = Role.Button) { scope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }
}

// A couple of portraits before the card showing stay in view, so the reader can see where they are in the deck.
private fun stripLeadIndex(currentPage: Int): Int = (currentPage - STRIP_LEAD_ITEMS).coerceAtLeast(0)

@Composable
private fun StripPortrait(name: String, portraitUrl: String?, modifier: Modifier) {
    if (portraitUrl != null) {
        AsyncImage(
            model = portraitUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            FigurePlaceholder(name = name, size = StripPortraitSize)
        }
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
