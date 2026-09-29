package com.mediasage.appserver.eval

import com.mediasage.appserver.repository.WorkData
import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.service.ClaudeApiClient
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
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test

/**
 * Pins Writings briefings to works a reporter wrote about another person's life, to see whether a briefing based
 * on one still speaks the reporter's own thought or drifts into retelling (and misremembering) the life.
 * Each work gets [RUNS] briefings. The history claims the work before it was used three times in a row, so
 * `writingsChoice` has to move on and offers only the pinned work.
 *
 * Opt-in because it makes [RUNS] real Claude calls per work:
 * `./gradlew :appServer:briefingEval --tests '*LivesOfOthersEval' -Plives=all` (or `-Plives=37008` for one work id).
 */
class LivesOfOthersEval {

    @Test
    fun livesOfOthers(): Unit = runBlocking {
        val selected = System.getProperty("briefingEval.lives")
        assumeTrue("Pass -Plives=all or -Plives=<workId> to run the Lives of Others eval", selected != null)
        val apiKey = System.getenv("CLAUDE_API_KEY")
        require(!apiKey.isNullOrBlank()) { "CLAUDE_API_KEY must be set: the eval calls the real Claude API" }
        EvalDatabase.init()
        claudeHttpClient().use { http ->
            val claude = ClaudeApiClient(http, apiKey)
            LIVES.filter { selected == "all" || it.workId.toString() == selected }.forEach { pinTo(it, claude) }
        }
    }

    private suspend fun pinTo(life: Life, claude: ClaudeApiClient) {
        val own = WorkRepository().getByFigureId(life.figureId).filterNot { it.isRecorded || it.isLifeOfAnother }
        val work = own.single { it.id == life.workId }
        val before = own[Math.floorMod(own.indexOf(work) - 1, own.size)]
        println("\n━━━ ${life.figureName} · ${work.displayTitle} (life of ${life.subject}) ━━━")
        val service = DailyReflectionService(claude, WorkRepository(), Clock.fixed(NOON, ZoneOffset.UTC))
        repeat(RUNS) { run ->
            val result = service.generate(request(life, history(before)))
            println("\nRun ${run + 1} · sources: ${result.sources.ifEmpty { listOf("(none)") }.joinToString("; ")}")
            println("  Verse:       ${result.scriptureReference}")
            println("  Insight:     ${result.insight}")
            println("  Implication: ${result.implication}")
            println("  Inspiration: ${result.inspiration}")
        }
    }

    // Mirrors the app's history label, so the server reads `before` as used three times in a row.
    private fun history(before: WorkData) = listOf("Sunday morning", "Sunday evening", "Monday morning")
        .map { "$it, Writings lens (drew on ${before.displayTitle}): An idea from ${before.title}." }

    private fun request(life: Life, history: List<String>) = DailyReflectionRequest(
        figureId = life.figureId,
        figureName = life.figureName,
        tone = "evening",
        dayOfWeek = "Monday",
        previousReflections = history,
        writingsOnly = true,
    )

    // Configured like serverModule's client.
    private fun claudeHttpClient() = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }
    }

    private data class Life(val figureId: Long, val figureName: String, val workId: Long, val subject: String)

    private companion object {
        const val RUNS = 3
        val NOON: Instant = Instant.parse("2026-09-28T12:00:00Z")

        val LIVES = listOf(
            Life(31, "Bernard of Clairvaux", 31010, "St. Malachy"),
            Life(37, "Athanasius", 37008, "Antony"),
            Life(46, "Gregory of Nyssa", 46009, "Macrina"),
            Life(49, "Jerome", 49005, "135 Christian writers"),
            Life(49, "Jerome", 49006, "Paul the First Hermit"),
            Life(49, "Jerome", 49007, "Hilarion"),
            Life(49, "Jerome", 49008, "Malchus"),
            Life(49, "Jerome", 49016, "Paula"),
        )
    }
}
