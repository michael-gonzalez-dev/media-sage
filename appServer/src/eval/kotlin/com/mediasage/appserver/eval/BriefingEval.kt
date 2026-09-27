package com.mediasage.appserver.eval

import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.service.ClaudeApiClient
import com.mediasage.appserver.service.DailyReflectionResult
import com.mediasage.appserver.service.DailyReflectionService
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Runs each scenario in `briefing-scenarios.json` through the real [DailyReflectionService] and the real
 * Claude API, prints the briefing with its check results, and fails if any check failed.
 *
 * `./gradlew :appServer:briefingEval` runs every scenario; add `-Pscenario=<name>` for one.
 */
class BriefingEval {

    @Test
    fun briefingScenarios(): Unit = runBlocking {
        val apiKey = System.getenv("CLAUDE_API_KEY")
        require(!apiKey.isNullOrBlank()) { "CLAUDE_API_KEY must be set: the eval calls the real Claude API" }
        EvalDatabase.init()
        val http = RecordingClaudeHttp()
        val outcomes = selectedScenarios().map { runScenario(it, http, apiKey) }
        http.client.close()

        val failed = outcomes.filterNot { it.passed }.map { it.scenario.name }
        println("\n${outcomes.size - failed.size}/${outcomes.size} scenarios passed every check")
        assertTrue(failed.isEmpty(), "Scenarios with failing checks: ${failed.joinToString()}")
    }

    private fun selectedScenarios(): List<BriefingScenario> {
        val all = BriefingScenario.loadAll()
        val name = System.getProperty("briefingEval.scenario") ?: return all
        return all.filter { it.name == name }.ifEmpty {
            error("No scenario named '$name'. Available: ${all.joinToString { it.name }}")
        }
    }

    private suspend fun runScenario(scenario: BriefingScenario, http: RecordingClaudeHttp, apiKey: String): Outcome {
        http.clear()
        val noonUtc = scenario.localDate.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC)
        val service = DailyReflectionService(
            claudeApiClient = ClaudeApiClient(http.client, apiKey),
            workRepository = WorkRepository(),
            clock = Clock.fixed(noonUtc, ZoneOffset.UTC)
        )
        val bibliography = WorkRepository().getByFigureId(scenario.figureId)
        BriefingReport.printHeader(scenario, bibliography.size)
        val checks = try {
            val result = service.generate(scenario.toRequest())
            val rawSources = http.lastRawSources()
            BriefingReport.printBriefing(result, rawSources)
            BriefingChecks.run(scenario, result, rawSources, bibliography)
        } catch (e: Exception) {
            listOf(CheckResult("valid response", false, "${e::class.simpleName}: ${e.message}"))
        }
        BriefingReport.printChecks(checks)
        return Outcome(scenario, checks.all { it.passed })
    }

    private data class Outcome(val scenario: BriefingScenario, val passed: Boolean)
}

private object BriefingReport {
    fun printHeader(scenario: BriefingScenario, bibliographySize: Int) {
        val lens = scenario.theme?.lowercase() ?: "headlines"
        println("\n━━━ ${scenario.name} ━━━")
        if (scenario.description.isNotBlank()) println(scenario.description)
        println(
            "${scenario.figureName} · ${scenario.tone} · ${scenario.localDate} · lens: $lens · " +
                "$bibliographySize works · ${scenario.previousReflections.size} past briefings"
        )
    }

    fun printBriefing(result: DailyReflectionResult, rawSources: List<String>) {
        println()
        println("Verse:        ${result.scriptureReference} — ${result.scriptureText}")
        println("Insight:      ${result.insight}")
        println("Implication:  ${result.implication}")
        println("Inspiration:  ${result.inspiration}")
        println("Challenge:    ${result.challenge}")
        println("Sources:      ${result.sources.ifEmpty { listOf("(none)") }.joinToString("; ")}")
        if (rawSources.size != result.sources.size) println("  Claude cited: ${rawSources.joinToString("; ")}")
    }

    fun printChecks(checks: List<CheckResult>) {
        println()
        checks.forEach { check ->
            val detail = if (check.detail.isBlank()) "" else " (${check.detail})"
            println("  ${if (check.passed) "PASS" else "FAIL"}  ${check.name}$detail")
        }
    }
}
