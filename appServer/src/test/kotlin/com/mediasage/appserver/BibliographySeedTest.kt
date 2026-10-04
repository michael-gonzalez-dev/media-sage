package com.mediasage.appserver

import com.mediasage.appserver.service.SourceWorks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the curated bibliography in `seed_works.sql` against the verified quotes in
 * `seed_quotes.sql`, so a new quote or a bibliography edit can't silently break coverage.
 */
class BibliographySeedTest {

    private data class SeedWork(
        val id: Long,
        val figureId: Long,
        val title: String,
        val year: Int?,
        val recordedBy: String?,
        val forQuotesOnly: Boolean = false,
    )

    private val figureIds = Regex("""VALUES \((\d+),'""").findAll(resource("seed_figures.sql"))
        .map { it.groupValues[1].toLong() }.toSet()

    private data class SeedQuote(val figureId: Long, val source: String, val text: String, val verified: Boolean)

    private val seedQuotes = SEED_QUOTE_ROW.findAll(resource("seed_quotes.sql"))
        .map { match ->
            val groups = match.groupValues
            SeedQuote(groups[1].toLong(), groups[2].replace("''", "'"), groups[3].replace("''", "'"), groups[4] == "true")
        }
        .toList()

    // Only verified quotes must be covered by the bibliography.
    private val quoteSources = seedQuotes.filter { it.verified }.map { it.figureId to it.source }.toSet()

    // Recorded works were drawn from quote sources, and stay in the bibliography after a quote is un-verified.
    private val allQuoteSources = seedQuotes.map { it.figureId to it.source }.toSet()

    private val verifiedCitedIn = quoteSources.filter { (_, source) -> source.startsWith(SECONDARY_SOURCE_PREFIX) }

    // (figure, text before the change) for every quote add_verified_quotes.sql verifies again or corrects.
    private val restoredByAddScript = QUOTE_RESTORE.findAll(resource("add_verified_quotes.sql"))
        .map { it.groupValues[3].toLong() to it.groupValues[4].replace("''", "'") }
        .toList()

    private val verifiedQuotes = seedQuotes.filter { it.verified }.map { Triple(it.figureId, it.source, it.text) }.toSet()

    // Rows are (id, figure_id, title, year) for the figure's own works, plus recorded_by in the Recorded Words section,
    // plus for_quotes_only in the Recorded for Quotes section.
    private val works = Regex(
        """^\((\d+), (\d+), '((?:[^']|'')*)', (\d+|NULL)(?:, '((?:[^']|'')*)')?(?:, (true|false))?\)""",
        RegexOption.MULTILINE
    )
        .findAll(resource("seed_works.sql"))
        .map { match ->
            val groups = match.groupValues
            SeedWork(
                id = groups[1].toLong(),
                figureId = groups[2].toLong(),
                title = groups[3].replace("''", "'"),
                year = groups[4].toIntOrNull(),
                recordedBy = groups[5].ifEmpty { null }?.replace("''", "'"),
                forQuotesOnly = groups[6] == "true"
            )
        }
        .toList()

    private val recordedWorks = works.filter { it.recordedBy != null }

    // The recorded works a briefing may draw on; the rest are listed only so "Cited in" quotes can cite them.
    private val briefingRecordedWorks = recordedWorks.filterNot { it.forQuotesOnly }

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
        // Supabase is patched by update_quote_sources.sql and update_quote_verification.sql, not re-seeded, so the
        // scripts and the seed must agree — an un-verification present only in seed_quotes.sql would never reach production.
        val unverifiedInSeed = UNVERIFIED_SEED_ROW.findAll(resource("seed_quotes.sql"))
            .map { it.groupValues[1].toLong() to it.groupValues[2] }
            .toSet()
        val unverifiedByScript = listOf("update_quote_sources.sql", "update_quote_verification.sql")
            .flatMap { script -> UNVERIFY_STATEMENT.findAll(resource(script)).map { it.groupValues[1].toLong() to it.groupValues[2] } }
            .toSet()
        // add_verified_quotes.sql runs last and verifies some of those again.
        val reverified = restoredByAddScript.map { (figureId, oldText) -> figureId to oldText.replace("'", "''") }.toSet()

        assertEquals(unverifiedInSeed, unverifiedByScript - reverified)
    }

    @Test
    fun theVerificationPatchLeavesSupabaseMatchingTheSeed() {
        // Supabase is patched by update_quote_verification.sql, not re-seeded, so every correction must land on the seed's values.
        val patch = resource("update_quote_verification.sql")
        val corrections = QUOTE_CORRECTION.findAll(patch).toList()

        val statements = patch.lines().count { it.startsWith("UPDATE ") }
        assertEquals(statements, corrections.size + UNVERIFY_STATEMENT.findAll(patch).count(), "every UPDATE is checked")

        val unescape = { value: String -> value.replace("''", "'") }
        // add_verified_quotes.sql runs last and may correct a quote's source again.
        val correctedLater = restoredByAddScript.toSet()
        corrections.forEach { match ->
            val (source, text, figureId) = match.destructured
            if (figureId.toLong() to unescape(text) in correctedLater) return@forEach
            val corrected = Triple(figureId.toLong(), unescape(source), unescape(text))
            assertTrue(corrected in verifiedQuotes, "seed_quotes.sql lacks verified ($figureId, $source, $text)")
        }
    }

    @Test
    fun theAddedQuotesPatchLeavesSupabaseMatchingTheSeed() {
        // Supabase is patched by add_verified_quotes.sql, not re-seeded, so every quote it adds or restores must land on a
        // verified seed row.
        val patch = resource("add_verified_quotes.sql")
        val restores = QUOTE_RESTORE.findAll(patch).toList()
        val additions = QUOTE_ADDITION.findAll(patch).toList()

        val statements = patch.lines().count { it.startsWith("UPDATE ") || it.startsWith("INSERT ") }
        assertEquals(statements, restores.size + additions.size, "every statement is checked")

        val unescape = { value: String -> value.replace("''", "'") }
        val landed = restores.map { val (source, text, figureId) = it.destructured; Triple(figureId, source, text) } +
            additions.map { val (figureId, source, text) = it.destructured; Triple(figureId, source, text) }
        landed.forEach { (figureId, source, text) ->
            val quote = Triple(figureId.toLong(), unescape(source), unescape(text))
            assertTrue(quote in verifiedQuotes, "seed_quotes.sql lacks verified $quote")
        }
        additions.forEach { assertEquals(it.groupValues[3], it.groupValues[4], "an addition checks for its own text") }
    }

    @Test
    fun everyFigureHasABibliographyEvenIfTheyAuthoredNoWorks() {
        val withWorks = works.map { it.figureId }.toSet()

        assertEquals(figureIds, withWorks)
    }

    @Test
    fun everyVerifiedCitedInQuoteNamesOneOfItsFiguresRecordedWorksExactly() {
        val unrecorded = verifiedCitedIn.filter { (figureId, source) ->
            recordedWorks.none { it.figureId == figureId && citesRecordedWork(source, it) }
        }

        assertEquals(emptyList(), unrecorded, "\"Cited in\" quote sources must name a recorded work exactly as seed_works.sql does")
    }

    @Test
    fun everyRecordedWorkIsCitedByOneOfItsFiguresQuotes() {
        val uncited = recordedWorks.filter { work ->
            allQuoteSources.none { (figureId, source) -> figureId == work.figureId && citesRecordedWork(source, work) }
        }

        assertEquals(emptyList(), uncited.map { it.title })
    }

    @Test
    fun onlyRecordedWorksAreKeptForQuotesOnly() {
        assertEquals(emptyList(), works.filter { it.forQuotesOnly && it.recordedBy == null }.map { it.title })
    }

    @Test
    fun theRecordedSourcesPatchLeavesSupabaseMatchingTheSeed() {
        // Supabase is patched by update_recorded_sources.sql, not re-seeded, so every change must land on the seed's values.
        val patch = resource("update_recorded_sources.sql")

        val statements = patch.lines().count { it.startsWith("UPDATE ") }
        val checked = QUOTE_SOURCE_UPDATE.findAll(patch).count() + WORK_UPDATE.findAll(patch).count()
        assertEquals(statements, checked, "every UPDATE is checked")

        QUOTE_SOURCE_UPDATE.findAll(patch).forEach { match ->
            val (newSource, figureId, oldSource) = match.destructured
            assertTrue(figureId.toLong() to newSource.replace("''", "'") in allQuoteSources, "seed_quotes.sql lacks $newSource")
            assertFalse(figureId.toLong() to oldSource.replace("''", "'") in allQuoteSources, "seed_quotes.sql still has $oldSource")
        }
        WORK_UPDATE.findAll(patch).forEach { match ->
            val (assignments, id) = match.destructured
            val work = works.single { it.id == id.toLong() }
            YEAR_ASSIGNMENT.find(assignments)?.let { assertEquals(it.groupValues[1].toInt(), work.year, "year of $id") }
            RECORDED_BY_ASSIGNMENT.find(assignments)?.let { assertEquals(it.groupValues[1], work.recordedBy, "recorded_by of $id") }
        }
    }

    @Test
    fun onlyFiguresWithFewerThanAFullWindowOfTheirOwnWorksHaveARecordedWorkForBriefings() {
        // A briefing looks at five works; a figure with five of their own would never reach a recorded one.
        val ownWorkCounts = works.filter { it.recordedBy == null }.groupingBy { it.figureId }.eachCount()

        val unreachable = briefingRecordedWorks.filter { (ownWorkCounts[it.figureId] ?: 0) >= BRIEFING_WINDOW }

        assertEquals(emptyList(), unreachable.map { it.title })
    }

    @Test
    fun noFigureHasMoreThanOneRecordedWorkForBriefings() {
        val repeated = briefingRecordedWorks.groupingBy { it.figureId }.eachCount().filterValues { it > 1 }.keys

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
    fun everyLifeOfAnotherIsOneOfTheFiguresOwnWorks() {
        val flagged = LIFE_OF_ANOTHER_UPDATE.findAll(resource("seed_works.sql")).map { it.groupValues[1].toLong() }.toList()

        assertTrue(flagged.isNotEmpty())
        assertEquals(emptyList(), flagged.filterNot { id -> works.any { it.id == id && it.recordedBy == null } })
    }

    @Test
    fun titlesKeepTheYearInItsOwnColumn() {
        assertTrue(works.none { YEAR_IN_TITLE.containsMatchIn(it.title) })
    }

    // "Cited in <writer>, <title> (<year>)", optionally with a locator such as ", vol. 1" before the year.
    private fun citesRecordedWork(source: String, work: SeedWork): Boolean {
        val citation = "$SECONDARY_SOURCE_PREFIX${work.recordedBy}, ${work.title}"
        val year = work.year?.let { " ($it)" }.orEmpty()
        return source == citation + year || (source.startsWith("$citation, ") && source.endsWith(year))
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
        val QUOTE_SOURCE_UPDATE = Regex(
            """^UPDATE quotes SET source = '((?:[^']|'')*)' WHERE figure_id = (\d+) AND source = '((?:[^']|'')*)';""",
            RegexOption.MULTILINE
        )
        /** Rows are (figure_id, source, text, themes, verified). */
        val SEED_QUOTE_ROW = Regex(
            """^\((\d+), '((?:[^']|'')*)', '((?:[^']|'')*)', '(?:[^']|'')*', (true|false)\)""",
            RegexOption.MULTILINE
        )
        val QUOTE_CORRECTION = Regex(
            """^UPDATE quotes SET source = '((?:[^']|'')*)', text = '((?:[^']|'')*)' WHERE figure_id = (\d+) AND text = '(?:[^']|'')*';""",
            RegexOption.MULTILINE
        )
        val QUOTE_RESTORE = Regex(
            """^UPDATE quotes SET source = '((?:[^']|'')*)', text = '((?:[^']|'')*)', verified = true """ +
                """WHERE figure_id = (\d+) AND text = '((?:[^']|'')*)';""",
            RegexOption.MULTILINE
        )
        val QUOTE_ADDITION = Regex(
            """^INSERT INTO quotes \(figure_id, source, text, themes, verified\) """ +
                """SELECT (\d+), '((?:[^']|'')*)', '((?:[^']|'')*)', '(?:[^']|'')*', true """ +
                """WHERE NOT EXISTS \(SELECT 1 FROM quotes WHERE figure_id = \d+ AND text = '((?:[^']|'')*)'\);""",
            RegexOption.MULTILINE
        )
        val WORK_UPDATE = Regex("""^UPDATE works SET (.+) WHERE id = (\d+);""", RegexOption.MULTILINE)
        val YEAR_ASSIGNMENT = Regex("""year = (\d+)""")
        val RECORDED_BY_ASSIGNMENT = Regex("""recorded_by = '((?:[^']|'')*)'""")
        val LIFE_OF_ANOTHER_UPDATE = Regex("""^UPDATE works SET is_life_of_another = true WHERE id = (\d+);""", RegexOption.MULTILINE)
    }
}
