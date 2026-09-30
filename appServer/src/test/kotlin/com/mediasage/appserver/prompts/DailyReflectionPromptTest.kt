package com.mediasage.appserver.prompts

import com.mediasage.appserver.repository.WorkData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyReflectionPromptTest {

    private val weekOfScriptures = listOf("Psalm 46:10", "Isaiah 40:31", "John 15:5", "Romans 8:28", "Philippians 4:6")

    private fun params(
        works: List<WorkData> = listOf(WorkData(1, "The Pursuit of God", 1948), WorkData(2, "The Root of the Righteous", null)),
        headlines: List<String> = emptyList(),
        previousScriptures: List<String> = emptyList(),
        previousReflections: List<String> = emptyList(),
        theme: ReflectionTheme? = null,
        writingsOnly: Boolean = false,
    ) = DailyReflectionPrompt.Params(
        figureName = "A.W. Tozer",
        works = works,
        headlines = headlines,
        tone = "morning",
        dayOfWeek = "Thursday",
        previousScriptures = previousScriptures,
        previousReflections = previousReflections,
        theme = theme,
        writingsOnly = writingsOnly
    )

    private val fromWorksLine = "Draw the insight, implication and inspiration from the source works above."

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
    fun asksForNoEmDashesOrSemicolonsAndUsesNoDashesItself() {
        val fullWeek = params(
            headlines = listOf("Floods displace thousands"),
            previousScriptures = weekOfScriptures,
            previousReflections = listOf("Monday morning: God is near.")
        )
        val prompt = DailyReflectionPrompt.buildSystemPrompt("A.W. Tozer") + DailyReflectionPrompt.buildUserMessage(fullWeek)

        assertTrue(prompt.contains("Do not use em dashes or semicolons. Use periods and commas."))
        assertFalse(prompt.contains('\u2014'), "the prompt models the style it asks for: no em dashes")
        assertFalse(prompt.contains('\u2013'), "the prompt models the style it asks for: no en dashes")
    }

    @Test
    fun givesOneSentenceCountAndWordLimitForEverySection() {
        val message = DailyReflectionPrompt.buildUserMessage(params())

        assertTrue(message.contains("Each section must be exactly 2 short sentences, under 50 words in total."))
        assertEquals(3, Regex(""""<2 short sentences, under 50 words>"""").findAll(message).count())
        assertFalse(message.contains("1-3 sentences"), "no conflicting sentence count")
        assertFalse(message.contains("1-2 sentences"), "no conflicting sentence count")
    }

    @Test
    fun asksEachSectionToGrowOutOfTheOneBefore() {
        val message = DailyReflectionPrompt.buildUserMessage(params())

        assertTrue(message.contains("Let each section grow out of the one before."))
        assertTrue(message.contains("the inspiration should answer the implication"))
        assertTrue(message.contains("Carry one idea or image through all three."))
    }

    @Test
    fun explainsThatARecordedWorkHoldsTheFiguresWordsNotTheWritersCommentary() {
        val recorded = WorkData(53901, "Scenes in the Life of Harriet Tubman", 1869, recordedBy = "Sarah Bradford")

        val message = DailyReflectionPrompt.buildUserMessage(params(works = listOf(recorded)))

        assertTrue(message.contains("- words recorded by Sarah Bradford in Scenes in the Life of Harriet Tubman (1869)"))
        assertTrue(message.contains("not on the writer's narration or commentary"))
    }

    @Test
    fun saysNothingAboutRecordedWorksWhenEveryWorkIsTheFiguresOwn() {
        assertFalse(DailyReflectionPrompt.buildUserMessage(params()).contains("words recorded by"))
    }

    @Test
    fun keepsTodaysHeadlinesInTheHeadlinesLens() {
        val message = DailyReflectionPrompt.buildUserMessage(params(headlines = listOf("Floods displace thousands")))

        assertTrue(message.contains("## Today's Headlines"))
        assertTrue(message.contains("- Floods displace thousands"))
    }

    @Test
    fun writingsLensDrawsOnTheSourceWorksWithNoHeadlinesOrThemeFocus() {
        val message = DailyReflectionPrompt.buildUserMessage(params(writingsOnly = true))

        assertTrue(message.contains("## Source Works from A.W. Tozer"))
        assertTrue(message.contains(fromWorksLine))
        assertFalse(message.contains("Headlines"))
        assertFalse(message.contains("Focus the scripture selection"))
    }

    @Test
    fun writingsLensAsksForOneWorkAndOnlyThatSource() {
        val writings = DailyReflectionPrompt.buildUserMessage(params(writingsOnly = true))
        val headlines = DailyReflectionPrompt.buildUserMessage(params())

        assertTrue(writings.contains("just one of the source works above and list only that work"))
        assertTrue(writings.contains("Do not name the source work or refer to A.W. Tozer in the third person"))
        assertTrue(writings.contains("not the same idea with a different verse"))
        assertFalse(headlines.contains("just one of the source works above"))
        assertFalse(headlines.contains("in the third person"))
        assertFalse(headlines.contains("not the same idea with a different verse"))
        assertTrue(headlines.contains("List the source works you drew from"))
    }

    @Test
    fun writingsLineIsOmittedWhenTheFigureHasNoWorksToDrawOn() {
        val message = DailyReflectionPrompt.buildUserMessage(params(works = emptyList(), writingsOnly = true))

        assertFalse(message.contains(fromWorksLine))
    }

    @Test
    fun themeAndHeadlinesLensesDoNotAskToDrawOnlyFromTheWorks() {
        val themed = DailyReflectionPrompt.buildUserMessage(params(theme = ReflectionTheme.HOPE))
        val headlines = DailyReflectionPrompt.buildUserMessage(params(headlines = listOf("Floods displace thousands")))

        assertTrue(themed.contains("Focus the scripture selection and reflection on the theme of hope."))
        assertFalse(themed.contains(fromWorksLine))
        assertFalse(headlines.contains(fromWorksLine))
    }
}
