package com.mediasage.feature.figures

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasage.domain.model.LensFilter
import com.mediasage.theme.BrandAmber
import com.mediasage.ui.FigurePlaceholder
import kotlinx.coroutines.launch
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.reporter_deck_jump
import org.jetbrains.compose.resources.stringResource

private const val STRIP_LEAD_ITEMS = 2
private const val CARD_ASPECT_RATIO = 5f / 7f // a 2.5 x 3.5 inch trading card
private val MinCardHeight = 300.dp
private val MinCardPeek = 32.dp
private val StripPortraitSize = 44.dp

/**
 * One card in a [ReporterDeck]. [tag] shows on the card's corner (e.g. "Today's pick"), [isHighlighted] gives the card
 * an amber border and its portrait in the strip an amber ring, and [selectedLens] fills that lens badge on the back.
 */
data class DeckCard(
    val info: ReporterCardInfo,
    val tag: String? = null,
    val isHighlighted: Boolean = false,
    val selectedLens: LensFilter? = null,
)

/**
 * Stacks [top], [deck] and [bottom], measuring the top and bottom first and giving the deck whatever height is left,
 * so the portrait strip always sits on screen under the card. When that would leave the deck below [MinCardHeight]
 * (large text), the stack grows past [availableHeight] and the caller's scroll takes over: the deck is then sized so
 * that, with [top] scrolled away, the deck and [bottom] fill the screen.
 */
@Composable
fun ReporterDeckLayout(
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
        val available = availableHeight.roundToPx()
        val bottomHeight = bottomPlaced.sumOf { it.height }
        val used = topPlaced.sumOf { it.height } + bottomHeight
        // Everything fits: the deck takes what's left. Otherwise the stack scrolls anyway, so once the top has scrolled
        // away the deck and the strip under it fill the screen.
        val deckHeight = (available - used).takeIf { it >= MinCardHeight.roundToPx() }
            ?: maxOf(available - bottomHeight, MinCardHeight.roundToPx())
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

/**
 * Reporter cards one at a time, swiped sideways, with the next and previous cards peeking in at the edges. [cards] is a
 * [State] so the pager's page count, keys and pages all read the same list: read separately, a filter could shrink the
 * deck between the count and the key lookup and index past its end. [onReadMore] adds a "Read more" button to each back.
 */
@Composable
fun ReporterDeck(
    cards: State<List<DeckCard>>,
    pagerState: PagerState,
    onLensSelected: (reporterId: Long, lens: LensFilter) -> Unit,
    onReadMore: ((reporterId: Long) -> Unit)? = null,
) = BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
    // Shaped like a real trading card: as wide as its height allows, never wider than the screen with a peek either side.
    val cardWidth = minOf(maxHeight * CARD_ASPECT_RATIO, maxWidth - MinCardPeek * 2)
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = (maxWidth - cardWidth) / 2),
        pageSpacing = 8.dp,
        key = { page -> cards.value[page].info.id },
        // One card past each edge stays composed, so a card that leaves the screen is still around to turn back to its
        // front out of sight. Without it the card is gone the moment it leaves, and would come back still flipped.
        beyondViewportPageCount = 1,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val card = cards.value[page]
        val id = card.info.id
        // Before the pager's first layout (e.g. coming back to the deck) no page is listed as visible yet; that is not
        // "off screen", or a card left flipped would turn back the moment the reader returns to it.
        val isOnScreen by remember(pagerState, page) {
            derivedStateOf {
                val visible = pagerState.layoutInfo.visiblePagesInfo
                visible.isEmpty() || visible.any { it.index == page }
            }
        }
        ReporterCard(
            info = card.info,
            modifier = Modifier.fillMaxSize(),
            selectedLens = card.selectedLens,
            onLensSelected = { lens -> onLensSelected(id, lens) },
            // Not just "the current page": the cards either side peek in, so a card left flipped keeps its back until
            // it is fully out of view, and only then turns back to its front.
            isOnScreen = isOnScreen,
            border = BorderStroke(3.dp, BrandAmber).takeIf { card.isHighlighted },
            tag = card.tag,
            onReadMore = onReadMore?.let { { it(id) } },
        )
    }
}

/** Small portraits of the deck. The card showing gets a dark ring and a highlighted one an amber ring; tapping jumps to that card. */
@Composable
fun ReporterPortraitStrip(cards: List<DeckCard>, pagerState: PagerState, contentPadding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val stripState = rememberLazyListState(initialFirstVisibleItemIndex = stripLeadIndex(pagerState.currentPage))
    // Keeps the card showing in view as the reader swipes.
    LaunchedEffect(pagerState.currentPage) {
        stripState.animateScrollToItem(stripLeadIndex(pagerState.currentPage))
    }
    LazyRow(
        state = stripState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        itemsIndexed(cards, key = { _, card -> card.info.id }) { index, card ->
            val ring = when {
                index == pagerState.currentPage -> BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                card.isHighlighted -> BorderStroke(2.dp, BrandAmber)
                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            }
            val jumpLabel = stringResource(Res.string.reporter_deck_jump, card.info.name)
            StripPortrait(
                name = card.info.name,
                portraitUrl = card.info.portraitUrl,
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
