package com.mediasage.appserver.eval

import com.mediasage.appserver.repository.WorkData
import com.mediasage.appserver.service.DailyReflectionResult
import com.mediasage.appserver.service.SourceWorks

data class CheckResult(val name: String, val passed: Boolean, val detail: String = "")

/**
 * The automatic checks run on every briefing. They cover what can be graded mechanically; voice and
 * freshness of ideas are left to the human reading the printed output.
 */
object BriefingChecks {
    private const val MAX_SECTION_SENTENCES = 2
    private const val MAX_CHALLENGE_WORDS = 25

    fun run(
        scenario: BriefingScenario,
        result: DailyReflectionResult,
        rawSources: List<String>,
        bibliography: List<WorkData>
    ): List<CheckResult> = listOf(
        validResponse(result),
        freshVerse(result.scriptureReference, scenario.previousScriptures),
        sourcesInBibliography(rawSources, bibliography),
        sectionLength("insight", result.insight),
        sectionLength("implication", result.implication),
        sectionLength("inspiration", result.inspiration),
        challengeLength(result.challenge)
    )

    fun validResponse(result: DailyReflectionResult): CheckResult {
        val blank = listOf(
            "scriptureReference" to result.scriptureReference,
            "scriptureText" to result.scriptureText,
            "insight" to result.insight,
            "implication" to result.implication,
            "inspiration" to result.inspiration,
            "challenge" to result.challenge.orEmpty()
        ).filter { it.second.isBlank() }.map { it.first }
        return CheckResult("valid response", blank.isEmpty(), if (blank.isEmpty()) "" else "blank: ${blank.joinToString()}")
    }

    fun freshVerse(reference: String, history: List<String>): CheckResult {
        val reused = history.filter { VerseReference.overlaps(reference, it) }
        return CheckResult(
            "verse not in history",
            reused.isEmpty(),
            if (reused.isEmpty()) "${history.size} past verses" else "$reference overlaps ${reused.joinToString()}"
        )
    }

    /** Checks Claude's own list, before the service drops unmatched titles. No bibliography means no sources. */
    fun sourcesInBibliography(rawSources: List<String>, bibliography: List<WorkData>): CheckResult {
        val unmatched = rawSources.filter { SourceWorks.matchSources(listOf(it), bibliography).isEmpty() }
        return CheckResult(
            "sources in bibliography",
            unmatched.isEmpty(),
            if (unmatched.isEmpty()) "${rawSources.size} cited" else "not in bibliography: ${unmatched.joinToString("; ")}"
        )
    }

    fun sectionLength(section: String, text: String): CheckResult {
        val sentences = sentenceCount(text)
        return CheckResult("$section is 1–$MAX_SECTION_SENTENCES sentences", sentences in 1..MAX_SECTION_SENTENCES, "$sentences")
    }

    fun challengeLength(challenge: String?): CheckResult {
        val words = challenge.orEmpty().split(WHITESPACE).count { it.isNotBlank() }
        return CheckResult("challenge under $MAX_CHALLENGE_WORDS words", words in 1 until MAX_CHALLENGE_WORDS, "$words words")
    }

    /**
     * Sentence-ending punctuation (optionally followed by a closing quote or bracket) before whitespace or the
     * end. A final fragment with no punctuation counts as one more. Abbreviations like "St." over-count, which
     * shows up as a visible failure rather than a silent pass.
     */
    fun sentenceCount(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        val endings = SENTENCE_END.findAll(trimmed).toList()
        val endsWithPunctuation = endings.lastOrNull()?.range?.last == trimmed.lastIndex
        return if (endsWithPunctuation) endings.size else endings.size + 1
    }

    private val SENTENCE_END = Regex("""[.!?]+["'”’)\]]*(?=\s|$)""")
    private val WHITESPACE = Regex("""\s+""")
}

/** Compares scripture references by book, chapter and verse range, so "Isaiah 40:31" matches "Isaiah 40:28–31". */
object VerseReference {
    private data class Range(val book: String, val chapter: Int, val first: Int, val last: Int)

    fun overlaps(a: String, b: String): Boolean {
        val ra = parse(a)
        val rb = parse(b)
        if (ra == null || rb == null) return normalize(a) == normalize(b)
        return ra.book == rb.book && ra.chapter == rb.chapter && ra.first <= rb.last && rb.first <= ra.last
    }

    private fun parse(reference: String): Range? {
        val match = REFERENCE.matchEntire(normalize(reference)) ?: return null
        val groups = match.groupValues
        val first = groups[FIRST_VERSE].toInt()
        return Range(groups[BOOK], groups[CHAPTER].toInt(), first, groups[LAST_VERSE].toIntOrNull() ?: first)
    }

    private fun normalize(reference: String): String =
        reference.trim().lowercase()
            .replace(DASHES, "-")
            .replace(WHITESPACE, " ")
            .replace(PSALMS, "psalm ")

    private const val BOOK = 1
    private const val CHAPTER = 2
    private const val FIRST_VERSE = 3
    private const val LAST_VERSE = 4
    // A trailing verse-part letter ("15a") is ignored, so the range still parses.
    private val REFERENCE = Regex("""(.+?) (\d+):(\d+)[a-z]?(?: ?- ?(\d+)[a-z]?)?""")
    private val DASHES = Regex("[–—]")
    private val WHITESPACE = Regex("""\s+""")
    private val PSALMS = Regex("^psalms ")
}
