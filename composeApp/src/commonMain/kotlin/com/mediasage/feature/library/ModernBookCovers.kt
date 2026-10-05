package com.mediasage.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The 20th century's printed covers. The sans-serif is the platform's own, which sets them apart from the app's serifs.

/** A modern book's printed cover, in the layout its place on the shelf gives it. */
@Composable
internal fun ModernPrintPanel(work: LibraryWorkItem, cloth: Color, modifier: Modifier) {
    when (work.modernLayout()) {
        ModernLayout.TRI_BAND -> TriBandPanel(work, cloth, modifier)
        ModernLayout.BLOCK -> BlockPanel(work, cloth, modifier)
        ModernLayout.JACKET -> JacketPanel(work, cloth, modifier)
        ModernLayout.STRIPES -> StripesPanel(work, cloth, modifier)
    }
}

/** The layout's signature at list-row size. */
@Composable
internal fun ModernThumbnailMark(layout: ModernLayout, cloth: Color) {
    when (layout) {
        ModernLayout.TRI_BAND -> Box(Modifier.fillMaxWidth().fillMaxHeight(0.42f).background(CoverCream))
        ModernLayout.JACKET -> Box(Modifier.fillMaxSize().background(CoverCream).padding(3.dp).border(1.dp, cloth))
        ModernLayout.BLOCK -> Box(Modifier.fillMaxSize()) {
            Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().fillMaxHeight(0.34f).background(lighter(cloth)))
        }
        ModernLayout.STRIPES -> Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(CoverCream))
            Spacer(Modifier.height(2.dp))
            Box(Modifier.fillMaxWidth().height(2.dp).background(lighter(cloth)))
        }
    }
}

private fun lighter(cloth: Color) = lerp(cloth, Color.White, 0.3f)

@Composable
private fun sansTitle(size: Int, line: Int) = MaterialTheme.typography.titleMedium.copy(
    fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = line.sp, fontWeight = FontWeight.Bold,
)

@Composable
private fun sansCaps(): TextStyle = MaterialTheme.typography.labelSmall.copy(
    fontFamily = FontFamily.SansSerif, fontSize = 7.sp, lineHeight = 9.sp, letterSpacing = 1.5.sp,
)

/** The 1930s–50s paperback: color bands above and below a cream band holding the title. */
@Composable
private fun TriBandPanel(work: LibraryWorkItem, cloth: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Box(Modifier.weight(0.3f).fillMaxWidth().padding(start = HingeWidth + 4.dp, end = 6.dp), contentAlignment = Alignment.Center) {
            ReporterCaps(work, CoverCream, TextAlign.Center)
        }
        Box(
            Modifier.weight(0.42f).fillMaxWidth().background(CoverCream).padding(start = HingeWidth + 4.dp, end = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = work.title,
                style = sansTitle(size = 12, line = 14),
                color = lerp(cloth, Color.Black, 0.35f),
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(Modifier.weight(0.28f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.padding(start = HingeWidth).width(16.dp).height(1.5.dp).background(CoverCream))
        }
    }
}

/**
 * The 1950s–60s Swiss typographic style, in shapes and type alone: a lighter block across the top holding the name,
 * the title large and flush left beneath it, and a thick rule at the foot.
 */
@Composable
private fun BlockPanel(work: LibraryWorkItem, cloth: Color, modifier: Modifier) {
    val inset = Modifier.padding(start = HingeWidth + 7.dp, end = 8.dp)
    Column(modifier = modifier) {
        Box(
            Modifier.weight(0.34f).fillMaxWidth().background(lighter(cloth)).then(inset).padding(bottom = 8.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            ReporterCaps(work, CoverCream, TextAlign.Start)
        }
        Column(Modifier.weight(0.66f).fillMaxWidth().then(inset).padding(top = 10.dp, bottom = 12.dp)) {
            Text(text = work.title, style = sansTitle(size = 15, line = 17), color = CoverCream, maxLines = 5, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Box(Modifier.width(28.dp).height(4.dp).background(CoverCream))
        }
    }
}

/** The 1940s–50s typographic dust jacket: cream paper, a double frame and type in the book's color. */
@Composable
private fun JacketPanel(work: LibraryWorkItem, cloth: Color, modifier: Modifier) {
    Box(modifier = modifier.background(CoverCream).padding(start = HingeWidth + 4.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().border(1.5.dp, cloth).padding(2.5.dp).border(0.5.dp, cloth)
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ReporterCaps(work, cloth, TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Text(
                text = work.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold),
                color = lerp(cloth, Color.Black, 0.2f),
                textAlign = TextAlign.Center,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(5.dp).clip(CircleShape).background(cloth))
        }
    }
}

/** The 1960s paperback: three bands straight across the middle, the title above them in bold sans-serif. */
@Composable
private fun StripesPanel(work: LibraryWorkItem, cloth: Color, modifier: Modifier) {
    val textPadding = Modifier.padding(start = HingeWidth + 7.dp, end = 8.dp)
    Column(modifier = modifier.padding(top = 14.dp, bottom = 10.dp)) {
        Text(
            text = work.title,
            style = sansTitle(size = 13, line = 15),
            color = CoverCream,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = textPadding,
        )
        Spacer(Modifier.weight(1f))
        listOf(CoverCream, lighter(cloth), lerp(cloth, Color.Black, 0.3f)).forEach { band ->
            Box(Modifier.fillMaxWidth().height(5.dp).background(band))
            Spacer(Modifier.height(3.dp))
        }
        Spacer(Modifier.weight(1f))
        Box(textPadding) { ReporterCaps(work, CoverCream, TextAlign.Start) }
    }
}

@Composable
private fun ReporterCaps(work: LibraryWorkItem, color: Color, align: TextAlign) {
    Text(
        text = work.reporterName.uppercase(),
        style = sansCaps(),
        color = color,
        textAlign = align,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
