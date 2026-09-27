package com.mediasage.appserver.service

import com.mediasage.appserver.repository.WorkData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SourceWorksTest {

    private fun bibliography(size: Int) = (1..size).map { WorkData(id = it.toLong(), title = "Work $it", year = null) }

    @Test
    fun rotationWindow_changesAcrossConsecutiveDaysForEveryBibliographyLargerThanTheWindow() {
        (6..30).forEach { size ->
            val works = bibliography(size)
            (20_000L..20_014L).forEach { day ->
                val today = SourceWorks.rotationWindow(works, day, isEvening = false, size = 5).toSet()
                val tomorrow = SourceWorks.rotationWindow(works, day + 1, isEvening = false, size = 5).toSet()
                assertNotEquals(today, tomorrow, "bibliography of $size, day $day")
            }
        }
    }

    @Test
    fun rotationWindow_givesTheEveningADifferentSetThanThatMorning() {
        (6..30).forEach { size ->
            val works = bibliography(size)
            val morning = SourceWorks.rotationWindow(works, 20_000L, isEvening = false, size = 5).toSet()
            val evening = SourceWorks.rotationWindow(works, 20_000L, isEvening = true, size = 5).toSet()
            assertNotEquals(morning, evening, "bibliography of $size")
        }
    }

    @Test
    fun rotationWindow_keepsMostOfYesterdaysWorksSoABookIsNotDroppedTheNextDay() {
        val works = bibliography(20)

        val today = SourceWorks.rotationWindow(works, 20_000L, isEvening = false, size = 5).toSet()
        val tomorrow = SourceWorks.rotationWindow(works, 20_001L, isEvening = false, size = 5).toSet()

        assertEquals(3, today.intersect(tomorrow).size)
    }

    @Test
    fun rotationWindow_eventuallyReachesEveryWorkInTheBibliography() {
        val works = bibliography(20)

        val seen = (20_000L until 20_010L).flatMap { day ->
            listOf(false, true).flatMap { SourceWorks.rotationWindow(works, day, it, size = 5) }
        }.toSet()

        assertEquals(works.toSet(), seen)
    }

    @Test
    fun rotationWindow_returnsTheWholeBibliographyWhenItFitsInTheWindow() {
        val works = bibliography(3)

        assertEquals(works, SourceWorks.rotationWindow(works, 20_000L, isEvening = false, size = 5))
        assertEquals(emptyList(), SourceWorks.rotationWindow(emptyList(), 20_000L, isEvening = false, size = 5))
    }

    @Test
    fun matchSources_returnsTheBibliographyEntryFormattedWithItsYear() {
        val works = listOf(
            WorkData(1, "The Pursuit of God", 1948),
            WorkData(2, "Confessions", null),
        )

        val matched = SourceWorks.matchSources(
            listOf("the pursuit of god", "Confessions, Book X, Chapter 27 (397 AD)", "Pursuit of God (1948)"),
            works
        )

        assertEquals(listOf("The Pursuit of God (1948)", "Confessions"), matched)
    }

    @Test
    fun matchSources_dropsTitlesThatAreNotInTheBibliography() {
        val works = listOf(WorkData(1, "The Pursuit of God", 1948))

        val matched = SourceWorks.matchSources(listOf("The Pursuit of Holiness", "An Invented Treatise"), works)

        assertTrue(matched.isEmpty())
    }

    @Test
    fun matchSources_returnsNothingForAFigureWithoutABibliography() {
        assertTrue(SourceWorks.matchSources(listOf("Anything"), emptyList()).isEmpty())
    }

    @Test
    fun candidateTitles_stripsLocatorsAfterACommaOrAQuestionMark() {
        assertTrue("a mighty fortress is our god" in SourceWorks.candidateTitles("A Mighty Fortress Is Our God, hymn (1529)"))
        assertTrue(
            "who is the rich man that shall be saved" in
                SourceWorks.candidateTitles("Who Is the Rich Man That Shall Be Saved? Chapter 26 (circa 200 AD)")
        )
        assertTrue("pensees" in SourceWorks.candidateTitles("Pensées, 277"))
    }
}
