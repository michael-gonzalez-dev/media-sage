package com.mediasage.feature.library

import androidx.compose.ui.graphics.Color
import com.mediasage.domain.model.FigureEra
import com.mediasage.theme.BrandAmber

// Every color the Library's default covers use, in one place. They're the books' own materials, not theme tokens:
// a red morocco binding stays red on any page, so none of them change with the app theme or dark mode. If another
// feature starts using them (era chips colored by era, say), promote them to theme/Color.kt.

/** Gilt stamping: the brand amber, the one cover color the app shares. */
internal val CoverGilt = BrandAmber

/** Cream paper: the modern covers' jackets and bands, and their type on color. */
internal val CoverCream = Color(0xFFF1EADB)

/** The red leather lettering-piece of an 18th-century half binding. */
internal val CoverRedLabel = Color(0xFF5E1F22)

/** The dark leather over a half binding's spine and corners. */
internal val CoverHalfBindingLeather = Color(0xFF3B2618)

/** The undyed thong that ties an early codex's wrap. */
internal val CoverTieLeather = Color(0xFFB89770)

/** Medieval brass fittings: the metal, its lit dome and its shadowed edge. */
internal val CoverBrass = Color(0xFFB08D57)
internal val CoverBrassHighlight = Color(0xFFE6CFA0)
internal val CoverBrassShadow = Color(0xFF6E5530)

/** How a book's period bound it, drawn on the default cover so a reader can tell the eras apart at a glance. */
enum class PeriodBinding { GENERAL, EARLY_CODEX, MEDIEVAL_BOARDS, REFORMATION_BLIND, GEORGIAN_HALF, VICTORIAN_CLOTH, MODERN_PRINT }

/** The 20th century's printed covers, mixed along a modern shelf. Shapes and type only, no pictures or symbols, so any reporter suits any layout. */
enum class ModernLayout { TRI_BAND, BLOCK, JACKET, STRIPES }

data class CoverStyle(val cloths: List<Color>, val binding: PeriodBinding)

/** The year the 1700s and 1800s era turns from Georgian half bindings to Victorian publisher's cloth. */
private const val VICTORIAN_FROM_YEAR = 1800

private val GeneralStyle = CoverStyle(
    listOf(
        Color(0xFF6B2626), // oxblood
        Color(0xFF2E4A3B), // forest
        Color(0xFF1F2D4C), // navy
        Color(0xFF7A5A22), // ochre
        Color(0xFF4A2D46), // plum
        Color(0xFF2A4750), // slate teal
        Color(0xFF5A3B28), // umber
    ),
    PeriodBinding.GENERAL,
)

// Tanned leather, porphyry purple, faded indigo and bronze-olive over a wrapped codex.
private val EarlyChurchStyle = CoverStyle(
    listOf(Color(0xFF6A4A30), Color(0xFF4E2340), Color(0xFF2F3A55), Color(0xFF4F4A25)),
    PeriodBinding.EARLY_CODEX,
)

// Leather over wooden boards, dyed in manuscript lapis, vermilion, green and violet.
private val MiddleAgesStyle = CoverStyle(
    listOf(Color(0xFF1E3363), Color(0xFF7A2A1E), Color(0xFF2E4A3B), Color(0xFF3E2C5C)),
    PeriodBinding.MEDIEVAL_BOARDS,
)

// Blind-tooled tan and dark calf, and goatskin morocco dyed red, green and blue-black.
private val ReformationStyle = CoverStyle(
    listOf(Color(0xFF7A5232), Color(0xFF6E2226), Color(0xFF3E2A1C), Color(0xFF3F4A2A), Color(0xFF22334D)),
    PeriodBinding.REFORMATION_BLIND,
)

// Coloured papers for the sides of a half binding.
private val GeorgianStyle = CoverStyle(
    listOf(Color(0xFF5D6B73), Color(0xFF6B6A4A), Color(0xFF7A5E3A), Color(0xFF4D3A4A)),
    PeriodBinding.GEORGIAN_HALF,
)

// Publisher's bookcloth.
private val VictorianStyle = CoverStyle(
    listOf(Color(0xFF24453A), Color(0xFF1F2D4C), Color(0xFF4A2D46), Color(0xFF5E2433)),
    PeriodBinding.VICTORIAN_CLOTH,
)

// Mid-century print colors: teal, burnt sienna, slate, mustard and plum.
private val ModernStyle = CoverStyle(
    listOf(Color(0xFF2F5D62), Color(0xFFA0522D), Color(0xFF3A4752), Color(0xFF7A6421), Color(0xFF5B3A5E)),
    PeriodBinding.MODERN_PRINT,
)

/** The 1700s and 1800s split by the book's year; an undated book of that era takes the 1800s cloth. */
fun coverStyle(era: FigureEra?, year: Int?): CoverStyle = when (era) {
    FigureEra.EARLY_CHURCH -> EarlyChurchStyle
    FigureEra.MIDDLE_AGES -> MiddleAgesStyle
    FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS -> ReformationStyle
    FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS -> if (year != null && year < VICTORIAN_FROM_YEAR) GeorgianStyle else VictorianStyle
    FigureEra.MODERN -> ModernStyle
    null -> GeneralStyle
}

fun LibraryWorkItem.coverStyle(): CoverStyle = coverStyle(era, year)

/**
 * The book's cloth: its era's colors taken in turn along the shelf, so neighbors never share one. Each reporter starts
 * on their own color and half of them walk the colors backwards, so shelves of one era don't all repeat one order.
 */
fun LibraryWorkItem.coverCloth(): Color {
    val cloths = coverStyle().cloths
    val count = cloths.size
    val start = (reporterId % count).toInt()
    // Stepping by 1 or by count - 1 visits every color before repeating, so neighbors still differ.
    val step = if (reporterId % 2 == 0L) 1 else count - 1
    return cloths[(start + shelfIndex * step) % count]
}

/** Neighbors on a modern shelf take different layouts, and each reporter starts on their own. */
fun LibraryWorkItem.modernLayout(): ModernLayout {
    val layouts = ModernLayout.entries
    return layouts[((reporterId + shelfIndex) % layouts.size).toInt()]
}
