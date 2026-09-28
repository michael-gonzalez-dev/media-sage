package com.mediasage.appserver.prompts

import com.mediasage.appserver.repository.WorkData

object DailyReflectionPrompt {

    data class Params(
        val figureName: String,
        val works: List<WorkData>,
        val headlines: List<String>,
        val tone: String,
        val dayOfWeek: String,
        val previousScriptures: List<String>,
        val previousReflections: List<String>,
        val theme: ReflectionTheme?,
        val writingsOnly: Boolean = false
    )

    fun buildSystemPrompt(figureName: String) = """
        You are generating a devotional reflection in the voice of $figureName.
        Draw from your knowledge of $figureName's published works and thought. Let the theological register, vocabulary, and convictions of those works shape the reflection.
        Do not invent quotes or attribute specific words to $figureName that you cannot verify from their actual writings.
        Do not use em dashes or semicolons. Use periods and commas.
        Respond ONLY with valid JSON, with no markdown and no explanation outside the JSON.
    """.trimIndent()

    fun buildUserMessage(params: Params): String = buildString {
        append(buildWorksBlock(params.figureName, params.works))
        if (params.headlines.isNotEmpty()) {
            appendLine("## Today's Headlines (for thematic context only)")
            params.headlines.forEach { appendLine("- $it") }
            appendLine()
        }
        append(buildContextBlock(params.tone, params.dayOfWeek, params.theme, params.writingsOnly && params.works.isNotEmpty()))
        append(buildHistoryBlock(params.figureName, params.previousScriptures, params.previousReflections))
        appendLine("## Instructions")
        appendLine("Write a ${params.tone} devotional reflection in the voice of ${params.figureName} structured in three sections:")
        appendLine("- Insight: what this truth reveals about God, the world, or ourselves")
        appendLine("- Implication: what it asks of us")
        appendLine("- Inspiration: a word of hope or encouragement in ${params.figureName}'s voice")
        appendLine("Maintain ${params.figureName}'s voice throughout.")
        if (params.writingsOnly && params.works.isNotEmpty()) appendLine(writingsVoiceInstruction(params.figureName))
        appendLine("Each section must be exactly 2 sentences.")
        appendLine("- Include a scripture reference and the full verse text")
        appendLine(sourcesInstruction(params))
        appendLine(buildChallengeInstruction(params.tone))
        appendLine()
        appendLine(RESPONSE_FORMAT)
    }

    private fun sourcesInstruction(params: Params) = when {
        params.works.isEmpty() -> NO_SOURCES_INSTRUCTION
        params.writingsOnly -> WRITINGS_SOURCES_INSTRUCTION
        else -> SOURCES_INSTRUCTION
    }

    // Pinned to one work, the model tends to describe the book rather than speak as its author.
    private fun writingsVoiceInstruction(figureName: String) =
        "Write as $figureName. Do not name the source work or refer to $figureName in the third person " +
            "in the insight, implication or inspiration."

    private fun buildWorksBlock(figureName: String, works: List<WorkData>) = buildString {
        if (works.isEmpty()) return@buildString
        appendLine("## Source Works from $figureName")
        appendLine("Draw from your knowledge of these works to shape the theological voice and direction of the reflection.")
        appendLine()
        works.forEach { appendLine("- ${it.displayTitle}") }
        if (works.any { it.isRecorded }) {
            appendLine()
            appendLine(
                "A work listed as \"words recorded by\" someone was written by that person and preserves $figureName's own words. " +
                    "Draw on $figureName's recorded words in it, not on the writer's narration or commentary."
            )
        }
        appendLine()
    }

    private fun buildContextBlock(tone: String, dayOfWeek: String, theme: ReflectionTheme?, fromWorks: Boolean) = buildString {
        val dayContext = if (dayOfWeek.isNotBlank()) "$dayOfWeek, " else ""
        appendLine("## Context")
        appendLine("Today is $dayContext$tone.")
        if (theme != null) {
            appendLine("Focus the scripture selection and reflection on the theme of ${theme.displayName}.")
        }
        if (fromWorks) {
            appendLine("Draw the insight, implication and inspiration from the source works above.")
        }
        appendLine()
    }

    private fun buildHistoryBlock(
        figureName: String,
        previousScriptures: List<String>,
        previousReflections: List<String>
    ) = buildString {
        if (previousScriptures.isNotEmpty()) {
            appendLine("## Scriptures Already Used")
            appendLine(PREVIOUS_SCRIPTURES_INSTRUCTION)
            previousScriptures.forEach { appendLine("- $it") }
            appendLine()
        }
        if (previousReflections.isNotEmpty()) {
            appendLine("## What $figureName's Briefings Said This Past Week")
            appendLine(PREVIOUS_REFLECTIONS_INSTRUCTION)
            previousReflections.forEach { appendLine("- $it") }
            appendLine()
        }
    }

    private const val PREVIOUS_SCRIPTURES_INSTRUCTION =
        "These verses were used in recent briefings. Do NOT reuse any of them. Choose a different passage:"

    private const val PREVIOUS_REFLECTIONS_INSTRUCTION =
        "You may revisit a theme if the headlines call for it, but bring a fresh angle, " +
        "a different application, or a deeper dimension, and do not restate any of these arguments. " +
        "If you draw on a work an earlier briefing used, take a different part or idea from it:"

    private const val SOURCES_INSTRUCTION =
        "- List the source works you drew from, copied exactly as written in the Source Works list above"

    private const val WRITINGS_SOURCES_INSTRUCTION =
        "- Base the reflection on just one of the source works above and list only that work, " +
            "copied exactly as written in the Source Works list above"

    private const val NO_SOURCES_INSTRUCTION = "- Return an empty sources list"

    private fun buildChallengeInstruction(tone: String): String {
        val framing = if (tone.equals("evening", ignoreCase = true)) {
            "retrospective, inviting the reader to look back on their day"
        } else {
            "anticipatory, inviting the reader to look ahead to their day"
        }
        return "- Include a reflection challenge: one open-ended question, exactly 1 sentence and " +
            "under 25 words, addressed to the reader in second person, drawn from the " +
            "insight/implication/inspiration above. Make it $framing. " +
            "Phrase the challenge in plain, everyday language, words a middle schooler would understand. " +
            "Avoid theological or academic vocabulary here, even though the rest of the reflection stays " +
            "in the figure's voice. Keep the underlying idea the same; just make the question itself simple, " +
            "short, and direct."
    }

    private val RESPONSE_FORMAT = """
        Respond ONLY with JSON in this exact format:
        {
          "scriptureReference": "<e.g. Psalm 46:10>",
          "scriptureText": "<full verse text>",
          "insight": "<2 sentences>",
          "implication": "<2 sentences>",
          "inspiration": "<2 sentences>",
          "sources": ["<source title>"],
          "challenge": "<one open-ended question, 1 sentence, under 25 words, second person>"
        }
    """.trimIndent()
}
