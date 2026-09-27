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
        val theme: ReflectionTheme?
    )

    fun buildSystemPrompt(figureName: String) = """
        You are writing a short devotional reflection in the voice of $figureName.
        Say what $figureName would say: draw their convictions, their favorite images, the way they reason, and their warmth or bluntness from their published works and thought.
        Say it the way a friend would: plain modern English and everyday words.
        Never use em dashes, en dashes, or semicolons. End the sentence and start a new one instead.
        Plain words must not flatten the voice: include at least one idea or image a reader would recognize as unmistakably $figureName's.
        Speak as $figureName. Never refer to $figureName by name or in the third person.
        Do not invent quotes or attribute specific words to $figureName that you cannot verify from their actual writings.
        Respond ONLY with valid JSON. Do not use markdown or add any explanation outside the JSON.
    """.trimIndent()

    fun buildUserMessage(params: Params): String = buildString {
        append(buildWorksBlock(params.figureName, params.works))
        if (params.headlines.isNotEmpty()) {
            appendLine("## Today's Headlines (for thematic context only)")
            params.headlines.forEach { appendLine("- $it") }
            appendLine()
        }
        append(buildContextBlock(params.tone, params.dayOfWeek, params.theme))
        append(buildHistoryBlock(params.figureName, params.previousScriptures, params.previousReflections))
        appendLine("## Instructions")
        appendLine("Write a ${params.tone} devotional reflection in the voice of ${params.figureName} in three sections:")
        appendLine("- Insight: one truth this verse reveals about God, the world, or ourselves")
        appendLine("- Implication: pick up that same truth and show what it asks of us today")
        appendLine("- Inspiration: carry that same thought through to hope, in ${params.figureName}'s voice")
        appendLine(ONE_REFLECTION_INSTRUCTION)
        appendLine("- Include a scripture reference and the full verse text")
        appendLine(if (params.works.isEmpty()) NO_SOURCES_INSTRUCTION else SOURCES_INSTRUCTION)
        appendLine(buildChallengeInstruction(params.tone))
        appendLine()
        appendLine(WRITING_STYLE)
        appendLine()
        appendLine(RESPONSE_FORMAT)
    }

    private fun buildWorksBlock(figureName: String, works: List<WorkData>) = buildString {
        if (works.isEmpty()) return@buildString
        appendLine("## Source Works from $figureName")
        appendLine("Draw from your knowledge of these works to shape what $figureName says and where the reflection goes.")
        appendLine()
        works.forEach { appendLine("- ${it.displayTitle}") }
        appendLine()
    }

    private fun buildContextBlock(tone: String, dayOfWeek: String, theme: ReflectionTheme?) = buildString {
        val dayContext = if (dayOfWeek.isNotBlank()) "$dayOfWeek, " else ""
        appendLine("## Context")
        appendLine("Today is $dayContext$tone.")
        if (theme != null) {
            appendLine("Focus the scripture selection and reflection on the theme of ${theme.displayName}.")
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
            "Phrase the challenge in words a middle schooler would understand: simple, short, and direct."
    }

    private const val ONE_REFLECTION_INSTRUCTION =
        "Each section is exactly 2 full sentences. The three sections are one reflection read top to bottom, " +
        "following a single thread of thought: each section builds on the one before it, so the reader never " +
        "feels the topic reset. Do not restate the theme in each section. Link the sections through the idea " +
        "itself: carry an image, a word, or a thought forward from the section before. Never open a section " +
        "with a signpost phrase such as \"That means\", \"So\", \"This is why\", or \"Because of this\"."

    private val WRITING_STYLE = """
        ## Writing Style
        Write the way a person talks to a friend they care about, not like a sermon or an essay.
        - Use everyday words. No archaic, academic, or churchy vocabulary.
        - Plain does not mean choppy. Mix shorter and longer sentences so the reflection flows when read aloud,
          but keep every sentence under about 25 words.
        - Contractions are fine where they sound natural.
        - Never use em dashes, en dashes, or semicolons. Where you would reach for one, end the sentence and start a new one.
        - No contrast framing: "not X, but Y", "X, not Y", "X is not passive", "It's not about X, it's about Y". Say what is true directly.
        - No lists of three.
        - No filler openers like "In a world where" or "Here's the truth".
    """.trimIndent()

    private val RESPONSE_FORMAT = """
        Respond ONLY with JSON in this exact format:
        {
          "scriptureReference": "<e.g. Psalm 46:10>",
          "scriptureText": "<full verse text>",
          "insight": "<exactly 2 full sentences>",
          "implication": "<exactly 2 full sentences>",
          "inspiration": "<exactly 2 full sentences>",
          "sources": ["<source title>"],
          "challenge": "<one open-ended question, 1 sentence, under 25 words, second person>"
        }
    """.trimIndent()
}
