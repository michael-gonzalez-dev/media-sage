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

    private data class SeedWork(val id: Long, val figureId: Long, val title: String, val year: Int?, val recordedBy: String?)

    private val figureIds = Regex("""VALUES \((\d+),'""").findAll(resource("seed_figures.sql"))
        .map { it.groupValues[1].toLong() }.toSet()

    // Rows are (figure_id, source, text, themes, verified); only verified quotes must be covered.
    private val quoteSources = Regex("""^\((\d+), '((?:[^']|'')*)', '(?:[^']|'')*', '(?:[^']|'')*', true\)""", RegexOption.MULTILINE)
        .findAll(resource("seed_quotes.sql"))
        .map { it.groupValues[1].toLong() to it.groupValues[2].replace("''", "'") }
        .toSet()

    // Rows are (id, figure_id, title, year) for the figure's own works, plus recorded_by in the Recorded Words section.
    private val works = Regex("""^\((\d+), (\d+), '((?:[^']|'')*)', (\d+|NULL)(?:, '((?:[^']|'')*)')?\)""", RegexOption.MULTILINE)
        .findAll(resource("seed_works.sql"))
        .map { match ->
            val groups = match.groupValues
            SeedWork(
                id = groups[1].toLong(),
                figureId = groups[2].toLong(),
                title = groups[3].replace("''", "'"),
                year = groups[4].toIntOrNull(),
                recordedBy = groups[5].ifEmpty { null }?.replace("''", "'")
            )
        }
        .toList()

    private val recordedWorks = works.filter { it.recordedBy != null }

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
    fun everyFigureHasABibliographyEvenIfTheyAuthoredNoWorks() {
        val withWorks = works.map { it.figureId }.toSet()

        assertEquals(figureIds, withWorks)
    }

    @Test
    fun everyRecordedWorkIsDrawnFromThatFiguresCitedInQuoteSources() {
        val citedIn = quoteSources
            .filter { (_, source) -> source.startsWith(SECONDARY_SOURCE_PREFIX) }
            .groupBy({ it.first }, { SourceWorks.normalize(it.second) })

        val undrawn = recordedWorks.filterNot { work ->
            citedIn[work.figureId].orEmpty().any { source ->
                SourceWorks.normalize(work.title) in source && SourceWorks.normalize(work.recordedBy!!) in source
            }
        }

        assertEquals(emptyList(), undrawn.map { it.title }, "recorded works with no matching \"Cited in\" quote source")
    }

    @Test
    fun onlyFiguresWithFewerThanAFullWindowOfTheirOwnWorksHaveARecordedWork() {
        // A briefing looks at five works; a figure with five of their own would never reach a recorded one.
        val ownWorkCounts = works.filter { it.recordedBy == null }.groupingBy { it.figureId }.eachCount()

        val unreachable = recordedWorks.filter { (ownWorkCounts[it.figureId] ?: 0) >= BRIEFING_WINDOW }

        assertEquals(emptyList(), unreachable.map { it.title })
    }

    @Test
    fun noFigureHasMoreThanOneRecordedWork() {
        val repeated = recordedWorks.groupingBy { it.figureId }.eachCount().filterValues { it > 1 }.keys

        assertEquals(emptySet(), repeated)
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
        const val BRIEFING_WINDOW = 5
    }
}
