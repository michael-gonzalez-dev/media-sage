package com.mediasage.appserver.eval

import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.service.ClaudeApiClient
import com.mediasage.appserver.service.DailyReflectionResult
import com.mediasage.appserver.service.DailyReflectionService
import com.mediasage.appserver.service.DailyReflectionService.DailyReflectionRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assume.assumeTrue
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test

/**
 * Simulates a week of Writings briefings for one reporter: 14 in a row, morning and evening, each one sent
 * the history of the ones before it labelled the way the app labels it. Prints every briefing and a summary
 * of how the week stayed fresh, to compare Writings designs by how the briefings actually read.
 *
 * Opt-in because it makes 14 real Claude calls per reporter:
 * `./gradlew :appServer:briefingEval --tests '*WritingsWeekEval' -Pweek=all` (or `-Pweek=19` for one reporter id).
 * The `--tests` filter keeps the single-briefing scenarios from running alongside it.
 */
class WritingsWeekEval {

    @Test
    fun writingsWeek(): Unit = runBlocking {
        val selected = System.getProperty("briefingEval.week")
        assumeTrue("Pass -Pweek=all or -Pweek=<figureId> to run the Writings week", selected != null)
        val apiKey = System.getenv("CLAUDE_API_KEY")
        require(!apiKey.isNullOrBlank()) { "CLAUDE_API_KEY must be set: the eval calls the real Claude API" }
        EvalDatabase.init()
        claudeHttpClient().use { http ->
            REPORTERS.filter { selected == "all" || it.first.toString() == selected }
                .forEach { (figureId, figureName) -> simulateWeek(figureId, figureName, ClaudeApiClient(http, apiKey)) }
        }
    }

    private suspend fun simulateWeek(figureId: Long, figureName: String, claude: ClaudeApiClient) {
        println("\n━━━ Writings week · $figureName ━━━")
        val history = mutableListOf<Briefing>()
        for (dayIndex in 0 until DAYS) {
            val date = START.plusDays(dayIndex.toLong())
            for (tone in listOf("morning", "evening")) {
                val result = generate(figureId, figureName, date, tone, history, claude)
                val briefing = Briefing(date, tone, result)
                printBriefing(briefing, history.lastOrNull())
                history += briefing
            }
        }
        printSummary(history)
    }

    private suspend fun generate(
        figureId: Long,
        figureName: String,
        date: LocalDate,
        tone: String,
        history: List<Briefing>,
        claude: ClaudeApiClient,
    ): DailyReflectionResult {
        val noonUtc = date.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC)
        val service = DailyReflectionService(claude, WorkRepository(), Clock.fixed(noonUtc, ZoneOffset.UTC))
        val request = DailyReflectionRequest(
            figureId = figureId,
            figureName = figureName,
            tone = tone,
            dayOfWeek = dayName(date),
            previousScriptures = history.map { it.result.scriptureReference }.distinct(),
            previousReflections = history.map { it.historyLine(today = date) },
            writingsOnly = true,
        )
        return service.generate(request)
    }

    private fun printBriefing(briefing: Briefing, previous: Briefing?) {
        val result = briefing.result
        val move = when {
            previous == null -> "first"
            result.sources.isNotEmpty() && result.sources == previous.result.sources -> "stayed"
            result.sources.any { it in previous.result.sources } -> "overlapped"
            else -> "moved"
        }
        val sources = result.sources.ifEmpty { listOf("(none)") }.joinToString("; ")
        println("\n${dayName(briefing.date)} ${briefing.tone} · $move · sources: $sources")
        println("  Verse:       ${result.scriptureReference}")
        println("  Insight:     ${result.insight}")
        println("  Implication: ${result.implication}")
        println("  Inspiration: ${result.inspiration}")
    }

    private fun printSummary(week: List<Briefing>) {
        val sourceCounts = week.map { it.result.sources.size }
        val cited = week.flatMap { it.result.sources }
        val stays = week.zipWithNext().count { (a, b) -> b.result.sources.isNotEmpty() && a.result.sources == b.result.sources }
        val repeatedVerses = week.size - week.map { it.result.scriptureReference }.distinct().size
        println("\n── Summary ──")
        println("Sources per briefing: ${sourceCounts.joinToString(" ")}")
        println("Distinct works cited: ${cited.distinct().size}")
        cited.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }
            .forEach { (work, count) -> println("  $count× $work") }
        println("Stayed on the same sources as the briefing before: $stays of ${week.size - 1}")
        println("Repeated verses: $repeatedVerses")
    }

    private data class Briefing(val date: LocalDate, val tone: String, val result: DailyReflectionResult) {
        // Mirrors the app's history label: day, lens, and the works the briefing drew on.
        fun historyLine(today: LocalDate): String {
            val day = if (date == today) "Earlier today ($tone)" else "${dayName(date)} $tone"
            val label = "$day, Writings lens"
            val drewOn = if (result.sources.isEmpty()) label else "$label (drew on ${result.sources.joinToString("; ")})"
            return "$drewOn: ${result.insight} ${result.implication} ${result.inspiration}"
        }
    }

    // Configured like serverModule's client.
    private fun claudeHttpClient() = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }
    }

    private companion object {
        const val DAYS = 7
        val START: LocalDate = LocalDate.parse("2026-09-28")

        // One widely read bibliography and one lesser-known one.
        val REPORTERS = listOf(19L to "A.W. Tozer", 20L to "Jan Hus")

        fun dayName(date: LocalDate) = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
