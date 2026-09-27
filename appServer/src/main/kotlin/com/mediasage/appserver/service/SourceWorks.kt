package com.mediasage.appserver.service

import com.mediasage.appserver.repository.WorkData
import java.text.Normalizer

/**
 * Picks which of a figure's bibliography works a briefing is pointed at, and checks the titles
 * the model returns against that bibliography so an unlisted or invented source is never shown.
 */
object SourceWorks {

    /**
     * A [size]-work window that slides forward one work per briefing (morning, evening, next morning…).
     * Consecutive briefings always see a different set whenever the bibliography is larger than the
     * window, yet overlap heavily, so a work stays available for several briefings in a row rather
     * than vanishing the next day — the weekly history, not the rotation, keeps the ideas fresh.
     */
    fun rotationWindow(works: List<WorkData>, epochDay: Long, isEvening: Boolean, size: Int): List<WorkData> {
        if (works.size <= size) return works
        val slot = epochDay * BRIEFINGS_PER_DAY + if (isEvening) 1 else 0
        val start = Math.floorMod(slot, works.size.toLong()).toInt()
        return List(size) { works[(start + it) % works.size] }
    }

    /** The bibliography entries named by [sources], formatted for display; unmatched titles are dropped. */
    fun matchSources(sources: List<String>, works: List<WorkData>): List<String> {
        val byTitle = works.associateBy { normalize(it.title) }
        return sources.mapNotNull { source -> candidateTitles(source).firstNotNullOfOrNull { byTitle[it] } }
            .distinct()
            .map { it.displayTitle }
    }

    /**
     * The normalized titles a source string could refer to: drops a trailing "(year)", then tries the
     * full text and every prefix that ends before a ", " or just after a "? "/"! " — which strips
     * locators like "Confessions, Book X, Chapter 27 (397 AD)" or "Who Is the Rich Man…? Chapter 26".
     */
    fun candidateTitles(source: String): Set<String> {
        val base = source.trim().replace(TRAILING_PARENTHETICAL, "")
        val cuts = COMMA_BOUNDARY.findAll(base).map { it.range.first } +
            QUESTION_BOUNDARY.findAll(base).map { it.range.first + 1 }
        return (cuts + base.length).map { normalize(base.substring(0, it)) }.toSet()
    }

    fun normalize(title: String): String =
        Normalizer.normalize(title, Normalizer.Form.NFKD)
            .replace(COMBINING_MARKS, "")
            .lowercase()
            .replace(NON_ALPHANUMERIC, " ")
            .trim()
            .removePrefix("the ")

    private const val BRIEFINGS_PER_DAY = 2
    private val TRAILING_PARENTHETICAL = Regex("""\s*\([^()]*\)\s*$""")
    private val COMMA_BOUNDARY = Regex(", ")
    private val QUESTION_BOUNDARY = Regex("[?!] ")
    private val COMBINING_MARKS = Regex("""\p{M}+""")
    private val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")
}
