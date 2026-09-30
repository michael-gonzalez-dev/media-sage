package com.mediasage.appserver.service

import com.mediasage.appserver.prompts.DailyReflectionPrompt
import com.mediasage.appserver.prompts.ReflectionTheme
import com.mediasage.appserver.repository.WorkData
import com.mediasage.appserver.repository.WorkRepository
import java.time.Clock
import java.time.LocalDate

class DailyReflectionService(
    private val claudeApiClient: ClaudeApiClient,
    private val workRepository: WorkRepository,
    private val clock: Clock = Clock.systemUTC()
) {
    suspend fun generate(request: DailyReflectionRequest): DailyReflectionResult {
        val bibliography = workRepository.getByFigureId(request.figureId)
        val works = offeredWorks(request, bibliography)

        val systemPrompt = DailyReflectionPrompt.buildSystemPrompt(request.figureName)
        val userMessage = DailyReflectionPrompt.buildUserMessage(
            DailyReflectionPrompt.Params(
                figureName = request.figureName,
                works = works,
                headlines = request.headlines,
                tone = request.tone,
                dayOfWeek = request.dayOfWeek,
                previousScriptures = request.previousScriptures,
                previousReflections = request.previousReflections,
                theme = request.theme,
                writingsOnly = request.writingsOnly,
                timeOfDay = request.timeOfDay
            )
        )

        val result = claudeApiClient.generateDailyReflection(systemPrompt, userMessage, request.tone)
        // A Writings briefing is based on one work, so it may cite only one of the works it was offered.
        if (request.writingsOnly) {
            return result.copy(sources = SourceWorks.matchSources(result.sources, works).take(1))
        }
        // Any of the figure's own works is a legitimate source, not just today's window, but a title that
        // isn't in the bibliography is never shown. A recorded work counts only when it was offered today,
        // so a figure with a full window of their own works is never credited to someone else's book.
        val citable = bibliography.filterNot { it.isRecorded } + works.filter { it.isRecorded }
        return result.copy(sources = SourceWorks.matchSources(result.sources, citable))
    }

    // A Writings briefing is offered one or two works to base itself on; every other lens gets a wider window.
    private fun offeredWorks(request: DailyReflectionRequest, bibliography: List<WorkData>): List<WorkData> {
        val epochDay = LocalDate.now(clock).toEpochDay()
        val isEvening = request.tone.equals("evening", ignoreCase = true)
        return if (request.writingsOnly) {
            SourceWorks.writingsChoice(bibliography, request.previousReflections, epochDay, isEvening, MAX_WRITINGS_IN_A_ROW)
        } else {
            SourceWorks.rotationWindow(works = bibliography, epochDay = epochDay, isEvening = isEvening, size = MAX_WORKS)
        }
    }

    data class DailyReflectionRequest(
        val figureId: Long,
        val figureName: String,
        val headlines: List<String> = emptyList(),
        val tone: String = "morning",
        val dayOfWeek: String = "",
        val previousScriptures: List<String> = emptyList(),
        val previousReflections: List<String> = emptyList(),
        val theme: ReflectionTheme? = null,
        val writingsOnly: Boolean = false,
        val timeOfDay: String = tone
    )

    companion object {
        private const val MAX_WORKS = 5
        private const val MAX_WRITINGS_IN_A_ROW = 3
    }
}

data class DailyReflectionResult(
    val scriptureReference: String,
    val scriptureText: String,
    val insight: String,
    val implication: String,
    val inspiration: String,
    val sources: List<String>,
    val tone: String,
    val challenge: String? = null
)
