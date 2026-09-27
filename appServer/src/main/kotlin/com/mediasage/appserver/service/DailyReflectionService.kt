package com.mediasage.appserver.service

import com.mediasage.appserver.prompts.DailyReflectionPrompt
import com.mediasage.appserver.prompts.ReflectionTheme
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
        val works = SourceWorks.rotationWindow(
            works = bibliography,
            epochDay = LocalDate.now(clock).toEpochDay(),
            isEvening = request.tone.equals("evening", ignoreCase = true),
            size = MAX_WORKS
        )

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
                theme = request.theme
            )
        )

        val result = claudeApiClient.generateDailyReflection(systemPrompt, userMessage, request.tone)
        // Checked against the whole bibliography, not just today's window — any real work of the
        // figure is a legitimate source, but a title that isn't in it is never shown to the user.
        return result.copy(sources = SourceWorks.matchSources(result.sources, bibliography))
    }

    data class DailyReflectionRequest(
        val figureId: Long,
        val figureName: String,
        val headlines: List<String> = emptyList(),
        val tone: String = "morning",
        val dayOfWeek: String = "",
        val previousScriptures: List<String> = emptyList(),
        val previousReflections: List<String> = emptyList(),
        val theme: ReflectionTheme? = null
    )

    companion object {
        private const val MAX_WORKS = 5
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
