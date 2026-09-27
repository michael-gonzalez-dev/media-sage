package com.mediasage.appserver.eval

import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.service.ClaudeApiClient
import com.mediasage.appserver.service.DailyReflectionResult
import com.mediasage.appserver.service.DailyReflectionService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test

/**
 * Generates each scenario in `briefing-scenarios.json` through the real [DailyReflectionService] and the
 * real Claude API and prints it to read.
 *
 * `./gradlew :appServer:briefingEval` runs every scenario; add `-Pscenario=<name>` for one.
 */
class BriefingEval {

    @Test
    fun briefingScenarios(): Unit = runBlocking {
        val apiKey = System.getenv("CLAUDE_API_KEY")
        require(!apiKey.isNullOrBlank()) { "CLAUDE_API_KEY must be set: the eval calls the real Claude API" }
        EvalDatabase.init()
        claudeHttpClient().use { http ->
            selectedScenarios().forEach { scenario -> printBriefing(scenario, generate(scenario, ClaudeApiClient(http, apiKey))) }
        }
    }

    private fun selectedScenarios(): List<BriefingScenario> {
        val all = BriefingScenario.loadAll()
        val name = System.getProperty("briefingEval.scenario") ?: return all
        return all.filter { it.name == name }.ifEmpty {
            error("No scenario named '$name'. Available: ${all.joinToString { it.name }}")
        }
    }

    // A fixed clock on the scenario's date, so the same scenario always gets the same bibliography works.
    private suspend fun generate(scenario: BriefingScenario, claude: ClaudeApiClient): DailyReflectionResult {
        val noonUtc = scenario.localDate.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC)
        val service = DailyReflectionService(claude, WorkRepository(), Clock.fixed(noonUtc, ZoneOffset.UTC))
        return service.generate(scenario.toRequest())
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

    private fun printBriefing(scenario: BriefingScenario, result: DailyReflectionResult) {
        val lens = scenario.theme?.lowercase() ?: "headlines"
        println("\n━━━ ${scenario.name} ━━━")
        if (scenario.description.isNotBlank()) println(scenario.description)
        println("${scenario.figureName} · ${scenario.tone} · ${scenario.localDate} · lens: $lens")
        println()
        println("Verse:        ${result.scriptureReference} — ${result.scriptureText}")
        println("Insight:      ${result.insight}")
        println("Implication:  ${result.implication}")
        println("Inspiration:  ${result.inspiration}")
        println("Challenge:    ${result.challenge}")
        println("Sources:      ${result.sources.ifEmpty { listOf("(none)") }.joinToString("; ")}")
    }
}
