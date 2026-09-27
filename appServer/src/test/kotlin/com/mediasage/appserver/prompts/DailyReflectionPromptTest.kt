package com.mediasage.appserver.prompts

import com.mediasage.appserver.repository.WorkData
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyReflectionPromptTest {

    private val weekOfScriptures = listOf("Psalm 46:10", "Isaiah 40:31", "John 15:5", "Romans 8:28", "Philippians 4:6")

    private fun params(
        works: List<WorkData> = listOf(WorkData(1, "The Pursuit of God", 1948), WorkData(2, "The Root of the Righteous", null)),
        headlines: List<String> = emptyList(),
        previousScriptures: List<String> = emptyList(),
        previousReflections: List<String> = emptyList(),
    ) = DailyReflectionPrompt.Params(
        figureName = "A.W. Tozer",
        works = works,
        headlines = headlines,
        tone = "morning",
        dayOfWeek = "Thursday",
        previousScriptures = previousScriptures,
        previousReflections = previousReflections,
        theme = null
    )

    @Test
    fun listsEveryScriptureFromThePastWeekEvenWithNoEarlierBriefingToday() {
        // Regression: scriptures used to be zipped with today's reflections, so a first briefing
        // of the day (no reflections yet) got the "do not reuse" heading with an empty list.
        val message = DailyReflectionPrompt.buildUserMessage(params(previousScriptures = weekOfScriptures))

        assertTrue(message.contains("## Scriptures Already Used"))
        weekOfScriptures.forEach { assertTrue(message.contains("- $it"), "missing $it") }
    }

    @Test
    fun includesEachBriefingFromThePastWeekAndAsksForADifferentAngle() {
        val history = listOf("Monday morning: God is near.", "Tuesday evening: Rest in Him.")

        val message = DailyReflectionPrompt.buildUserMessage(params(previousReflections = history))

        assertTrue(message.contains("## What A.W. Tozer's Briefings Said This Past Week"))
        assertTrue(message.contains("do not restate any of these arguments"))
        history.forEach { assertTrue(message.contains("- $it")) }
    }

    @Test
    fun omitsHistoryBlocksOnAFiguresFirstDay() {
        val message = DailyReflectionPrompt.buildUserMessage(params())

        assertFalse(message.contains("Scriptures Already Used"))
        assertFalse(message.contains("Briefings Said"))
    }

    @Test
    fun listsTheSourceWorksWithTitleAndYearWhenKnown() {
        val message = DailyReflectionPrompt.buildUserMessage(params())

        assertTrue(message.contains("- The Pursuit of God (1948)"))
        assertTrue(message.contains("- The Root of the Righteous\n"))
        assertTrue(message.contains("copied exactly as written in the Source Works list"))
    }

    @Test
    fun asksForNoSourcesWhenTheFigureHasNoBibliography() {
        val message = DailyReflectionPrompt.buildUserMessage(params(works = emptyList()))

        assertFalse(message.contains("## Source Works"))
        assertTrue(message.contains("Return an empty sources list"))
    }

    @Test
    fun keepsTodaysHeadlinesInTheHeadlinesLens() {
        val message = DailyReflectionPrompt.buildUserMessage(params(headlines = listOf("Floods displace thousands")))

        assertTrue(message.contains("## Today's Headlines"))
        assertTrue(message.contains("- Floods displace thousands"))
    }
}
