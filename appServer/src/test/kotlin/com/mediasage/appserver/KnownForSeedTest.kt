package com.mediasage.appserver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the reporter cards' "known for" sentences: every seeded figure has one, and the one-time
 * Supabase backfill in `update_figure_known_for.sql` writes exactly what the seed holds.
 */
class KnownForSeedTest {

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResource(name)) { "$name not found" }.readText()

    // Seed rows end (..., themes, known_for, portrait_url, is_enabled). Bios span several lines.
    private val seedRow = Regex(
        """^INSERT INTO figures .*? VALUES \((\d+),.*?,'(?:[^']|'')*','((?:[^']|'')*)',(?:'[^']*'|NULL),(?:true|false)\);$""",
        setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
    )

    private val backfillRow = Regex(
        """^UPDATE figures SET known_for = '((?:[^']|'')*)', .* WHERE id = (\d+) AND name = """,
        RegexOption.MULTILINE
    )

    private val seeded = seedRow
        .findAll(resource("seed_figures.sql"))
        .associate { it.groupValues[1].toLong() to it.groupValues[2] }

    private val backfilled = backfillRow
        .findAll(resource("update_figure_known_for.sql"))
        .associate { it.groupValues[2].toLong() to it.groupValues[1] }

    @Test
    fun everySeededFigureHasAKnownForSentence() {
        assertEquals(100, seeded.size)
        val missing = seeded.filterValues { it.isBlank() }.keys
        assertTrue(missing.isEmpty(), "Figures without a known-for sentence: $missing")
    }

    @Test
    fun backfillMatchesSeed() {
        assertEquals(seeded, backfilled)
    }
}
