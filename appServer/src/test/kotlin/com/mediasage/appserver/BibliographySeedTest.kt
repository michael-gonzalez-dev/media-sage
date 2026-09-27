package com.mediasage.appserver

import com.mediasage.appserver.service.SourceWorks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the curated bibliography in `seed_works.sql` against the verified quotes in
 * `seed_quotes.sql`, so a new quote or a bibliography edit can't silently break coverage.
 */
class BibliographySeedTest {

    private data class SeedWork(val id: Long, val figureId: Long, val title: String, val year: Int?)

    private val figureIds = Regex("""VALUES \((\d+),'""").findAll(resource("seed_figures.sql"))
        .map { it.groupValues[1].toLong() }.toSet()

    // Rows are (figure_id, source, text, themes, verified); only verified quotes must be covered.
    private val quoteSources = Regex("""^\((\d+), '((?:[^']|'')*)', '(?:[^']|'')*', '(?:[^']|'')*', true\)""", RegexOption.MULTILINE)
        .findAll(resource("seed_quotes.sql"))
        .map { it.groupValues[1].toLong() to it.groupValues[2].replace("''", "'") }
        .toSet()

    private val works = Regex("""^\((\d+), (\d+), '((?:[^']|'')*)', (\d+|NULL)\)""", RegexOption.MULTILINE)
        .findAll(resource("seed_works.sql"))
        .map { match ->
            val groups = match.groupValues
            SeedWork(groups[1].toLong(), groups[2].toLong(), groups[3].replace("''", "'"), groups[4].toIntOrNull())
        }
        .toList()

    @Test
    fun everyWorkCitedByAVerifiedQuoteIsInThatFiguresBibliography() {
        val titlesByFigure = works.groupBy { it.figureId }.mapValues { (_, list) -> list.map { SourceWorks.normalize(it.title) }.toSet() }

        val uncovered = quoteSources
            .filterNot { (_, source) -> source.startsWith(SECONDARY_SOURCE_PREFIX) }
            .filter { (figureId, source) -> SourceWorks.candidateTitles(source).none { it in titlesByFigure[figureId].orEmpty() } }

        assertEquals(emptySet(), uncovered.toSet(), "quote sources missing from seed_works.sql")
    }

    @Test
    fun theSupabaseUpdateScriptUnverifiesExactlyTheQuotesTheSeedMarksUnverified() {
        // Supabase is patched by update_quote_sources.sql, not re-seeded, so the two must agree —
        // an un-verification present only in seed_quotes.sql would never reach production.
        val unverifiedInSeed = UNVERIFIED_SEED_ROW.findAll(resource("seed_quotes.sql"))
            .map { it.groupValues[1].toLong() to it.groupValues[2] }
            .toSet()
        val unverifiedByScript = UNVERIFY_STATEMENT.findAll(resource("update_quote_sources.sql"))
            .map { it.groupValues[1].toLong() to it.groupValues[2] }
            .toSet()

        assertEquals(unverifiedInSeed, unverifiedByScript)
    }

    @Test
    fun everyFigureHasABibliographyUnlessTheyAuthoredNoWorks() {
        val withWorks = works.map { it.figureId }.toSet()

        assertEquals(figureIds - FIGURES_WITHOUT_AUTHORED_WORKS, withWorks)
    }

    @Test
    fun noFigureListsTheSameWorkTwice() {
        val duplicates = works.groupBy { it.figureId to SourceWorks.normalize(it.title) }.filterValues { it.size > 1 }.keys

        assertEquals(emptySet(), duplicates)
    }

    @Test
    fun workIdsAreScopedToTheirFigureSoEditsNeverRenumberOtherFigures() {
        assertTrue(works.all { it.id / FIGURE_ID_BLOCK == it.figureId }, "ids must be figure_id * $FIGURE_ID_BLOCK + n")
        assertEquals(works.size, works.map { it.id }.toSet().size)
    }

    @Test
    fun titlesKeepTheYearInItsOwnColumn() {
        assertTrue(works.none { YEAR_IN_TITLE.containsMatchIn(it.title) })
    }

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResource(name)) { "$name not on the classpath" }.readText()

    private companion object {
        /** Quotes recorded in someone else's biography — the book is not the figure's own work. */
        const val SECONDARY_SOURCE_PREFIX = "Cited in "
        const val FIGURE_ID_BLOCK = 1000L
        val YEAR_IN_TITLE = Regex("""\(\s*c?\.?\s*\d{3,4}""")
        val UNVERIFIED_SEED_ROW = Regex(
            """^\((\d+), '(?:[^']|'')*', '((?:[^']|'')*)', '(?:[^']|'')*', false\)""",
            RegexOption.MULTILINE
        )
        val UNVERIFY_STATEMENT = Regex(
            """^UPDATE quotes SET verified = false WHERE figure_id = (\d+) AND text = '((?:[^']|'')*)';""",
            RegexOption.MULTILINE
        )
        /** Figures whose words survive only as recorded by others; reviewed and intentionally empty. */
        val FIGURES_WITHOUT_AUTHORED_WORKS = setOf(53L, 89L)
    }
}
