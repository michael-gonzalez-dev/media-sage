package com.mediasage.appserver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards John Perkins's death year: the seed and the one-time Supabase patch in
 * `update_john_perkins_death_year.sql` agree on 2026, in his lifespan and in his biography.
 */
class PerkinsDeathYearSeedTest {

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResource(name)) { "$name not found" }.readText()

    // Captures (lifespan, bio) of seed row 65.
    private val perkinsSeedRow = Regex(
        """^INSERT INTO figures .*? VALUES \(65,'John Perkins','(?:[^']|'')*','(?:[^']|'')*','(?:[^']|'')*','([^']*)','((?:[^']|'')*)',""",
        RegexOption.MULTILINE
    )

    private val patch = Regex(
        """^UPDATE figures SET lifespan = '([^']*)', bio = REPLACE\(bio, '([^']*)', '([^']*)'\), """ +
            """.* WHERE id = 65 AND name = 'John Perkins';$""",
        RegexOption.MULTILINE
    )

    private val seeded = checkNotNull(perkinsSeedRow.find(resource("seed_figures.sql"))) { "Perkins seed row not found" }
    private val patched = checkNotNull(patch.find(resource("update_john_perkins_death_year.sql"))) { "Perkins patch not found" }

    @Test
    fun seedShowsPerkinsDyingIn2026() {
        val (lifespan, bio) = seeded.destructured
        assertEquals("1930-2026", lifespan)
        assertTrue("He died in 2026," in bio)
        assertFalse("2023" in bio)
    }

    @Test
    fun patchWritesWhatTheSeedHolds() {
        val (lifespan, from, to) = patched.destructured
        assertEquals(seeded.groupValues[1], lifespan)
        assertFalse(from in seeded.groupValues[2])
        assertTrue(to in seeded.groupValues[2])
    }
}
