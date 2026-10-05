package com.mediasage.feature.library

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Period binding details for the Library covers, drawn in code so no texture images are needed.

/** Blind tooling: a line pressed into leather, drawn as a dark groove with a faint lit edge below it. */
private fun DrawScope.blindLine(start: Offset, end: Offset) {
    drawLine(Color.Black.copy(alpha = 0.35f), start, end, 1.dp.toPx())
    val lit = Offset(0f, 0.75.dp.toPx())
    drawLine(Color.White.copy(alpha = 0.10f), start + lit, end + lit, 0.75.dp.toPx())
}

internal fun DrawScope.drawBlindFrame(inset: Float) {
    val w = size.width - inset
    val h = size.height - inset
    blindLine(Offset(inset, inset), Offset(w, inset))
    blindLine(Offset(inset, h), Offset(w, h))
    blindLine(Offset(inset, inset), Offset(inset, h))
    blindLine(Offset(w, inset), Offset(w, h))
}

/** CoverHalfBindingLeather-binding cornerpieces: a short L and a dot inside each corner. */
internal fun DrawScope.drawCornerPieces(color: Color, inset: Float = 3.dp.toPx()) {
    val arm = 8.dp.toPx()
    val stroke = 1.25.dp.toPx()
    listOf(
        Offset(inset, inset) to Offset(1f, 1f),
        Offset(size.width - inset, inset) to Offset(-1f, 1f),
        Offset(inset, size.height - inset) to Offset(1f, -1f),
        Offset(size.width - inset, size.height - inset) to Offset(-1f, -1f),
    ).forEach { (corner, dir) ->
        drawLine(color, corner, corner + Offset(arm * dir.x, 0f), stroke)
        drawLine(color, corner, corner + Offset(0f, arm * dir.y), stroke)
        drawCircle(color, radius = 1.2.dp.toPx(), center = corner + Offset(3.5.dp.toPx() * dir.x, 3.5.dp.toPx() * dir.y))
    }
}

/** Victorian cloth: paired gilt bands across the top and foot. */
internal fun DrawScope.drawBands() {
    val color = CoverGilt.copy(alpha = 0.7f)
    val gap = 3.dp.toPx()
    listOf(0f, gap, size.height - gap, size.height).forEachIndexed { index, y ->
        val stroke = if (index == 0 || index == 3) 1.25.dp.toPx() else 0.5.dp.toPx()
        drawLine(color, Offset(0f, y), Offset(size.width, y), stroke)
    }
}

/** Bookcloth weave: fine crossing threads, barely there, so the cloth reads as woven rather than painted. */
internal fun DrawScope.drawLinen() {
    val step = 1.5.dp.toPx()
    val thread = Color.White.copy(alpha = 0.035f)
    var x = 0f
    while (x < size.width) {
        drawLine(thread, Offset(x, 0f), Offset(x, size.height), 0.5f)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(Color.Black.copy(alpha = 0.05f), Offset(0f, y), Offset(size.width, y), 0.5f)
        y += step
    }
}

/** An early codex's leather wrap: a flap folded over the fore-edge, with a tie. */
internal fun DrawScope.drawWrapFlap(flapWidth: Float) {
    val left = size.width - flapWidth
    drawRect(Color.White.copy(alpha = 0.06f), topLeft = Offset(left, 0f), size = Size(flapWidth, size.height))
    drawLine(Color.Black.copy(alpha = 0.4f), Offset(left, 0f), Offset(left, size.height), 1.5.dp.toPx())
    val tieY = size.height / 2
    val tie = CoverTieLeather
    drawLine(tie, Offset(left - 9.dp.toPx(), tieY), Offset(size.width, tieY), 1.5.dp.toPx())
    drawCircle(tie, radius = 2.dp.toPx(), center = Offset(left - 9.dp.toPx(), tieY))
}

/** A medieval board's brass: a domed boss in each corner and two clasps over the fore-edge. */
internal fun DrawScope.drawMedievalHardware() {
    val radius = 3.dp.toPx()
    val inset = 6.dp.toPx()
    listOf(
        Offset(inset + 9.dp.toPx(), inset), Offset(size.width - inset, inset),
        Offset(inset + 9.dp.toPx(), size.height - inset), Offset(size.width - inset, size.height - inset),
    ).forEach { center ->
        drawCircle(
            Brush.radialGradient(listOf(CoverBrassHighlight, CoverBrass, CoverBrassShadow), center = center - Offset(1f, 1f), radius = radius),
            radius = radius,
            center = center,
        )
    }
    listOf(0.3f, 0.7f).forEach { fraction ->
        val top = size.height * fraction - 3.dp.toPx()
        val clasp = Size(12.dp.toPx(), 6.dp.toPx())
        drawRect(CoverBrass, topLeft = Offset(size.width - clasp.width, top), size = clasp)
        drawRect(CoverBrassShadow, topLeft = Offset(size.width - clasp.width, top), size = clasp, style = Stroke(0.75.dp.toPx()))
        drawCircle(CoverBrassShadow, radius = 1.dp.toPx(), center = Offset(size.width - clasp.width + 3.dp.toPx(), top + clasp.height / 2))
    }
}

/** A Georgian half binding: leather over the spine and fore-edge corners, cloth or paper sides, a gilt fillet at the join. */
internal fun DrawScope.drawHalfBinding(spineWidth: Float) {
    drawRect(CoverHalfBindingLeather, size = Size(spineWidth, size.height))
    val corner = 20.dp.toPx()
    listOf(
        Triple(Offset(size.width, 0f), Offset(size.width - corner, 0f), Offset(size.width, corner)),
        Triple(Offset(size.width, size.height), Offset(size.width - corner, size.height), Offset(size.width, size.height - corner)),
    ).forEach { (a, b, c) ->
        val path = Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            close()
        }
        drawPath(path, CoverHalfBindingLeather)
        drawLine(CoverGilt.copy(alpha = 0.6f), b, c, 0.75.dp.toPx())
    }
    drawLine(CoverGilt.copy(alpha = 0.6f), Offset(spineWidth, 0f), Offset(spineWidth, size.height), 0.75.dp.toPx())
}
