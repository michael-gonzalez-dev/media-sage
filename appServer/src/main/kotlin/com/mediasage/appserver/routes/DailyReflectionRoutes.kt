package com.mediasage.appserver.routes

import com.mediasage.appserver.plugins.DAILY_REFLECTION_RATE_LIMIT
import com.mediasage.appserver.prompts.ReflectionTheme
import com.mediasage.appserver.prompts.WRITINGS_LENS
import com.mediasage.appserver.repository.ClaudeCallLimitRepository
import com.mediasage.appserver.service.DailyLimitExceededException
import com.mediasage.appserver.service.DailyReflectionResult
import com.mediasage.appserver.service.DailyReflectionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.time.ZoneOffset

// Ceilings well above what the app sends. A briefing lists every stored headline title (up to 100), and its
// history covers 7 days x 2 times of day x 10 lenses of short (under ~150 word) briefings. Anything past a
// ceiling is trimmed before it reaches Claude, never rejected: the app shows a failed briefing for any error
// response, so an app request that happens to run long must still succeed.
internal const val MAX_SHORT_FIELD_LENGTH = 100
internal const val MAX_REFLECTION_HEADLINES = 150
internal const val MAX_PREVIOUS_SCRIPTURES = 300
internal const val MAX_PREVIOUS_REFLECTIONS = 150
internal const val MAX_PREVIOUS_REFLECTION_LENGTH = 2_000

fun Route.dailyReflectionRoutes() {
    val service: DailyReflectionService by inject()
    val callLimitRepository by inject<ClaudeCallLimitRepository>(named("reflectionCallLimit"))
    val dailyReflectionCallLimit by inject<Int>(named("dailyReflectionCallLimit"))

    rateLimit(DAILY_REFLECTION_RATE_LIMIT) {
        post("/api/analysis/daily-reflection") {
            val request = call.receive<DailyReflectionRequest>()
            request.validationError()?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }
            if (!callLimitRepository.tryConsumeCall(LocalDate.now(ZoneOffset.UTC).toString(), dailyReflectionCallLimit)) {
                throw DailyLimitExceededException()
            }
            val result = service.generate(request.bounded().toServiceRequest())
            call.respond(HttpStatusCode.OK, result.toResponse())
        }
    }
}

internal fun DailyReflectionRequest.validationError(): String? =
    if (figureId <= 0 || figureName.isBlank()) "figureId and figureName are required" else null

// History lists are trimmed from the front, so the most recent entries (the app sends them oldest first) are kept.
internal fun DailyReflectionRequest.bounded() = copy(
    figureName = figureName.take(MAX_SHORT_FIELD_LENGTH),
    headlines = headlines.take(MAX_REFLECTION_HEADLINES).map { it.take(MAX_HEADLINE_TITLE_LENGTH) },
    tone = tone.take(MAX_SHORT_FIELD_LENGTH),
    dayOfWeek = dayOfWeek.take(MAX_SHORT_FIELD_LENGTH),
    previousScriptures = previousScriptures.takeLast(MAX_PREVIOUS_SCRIPTURES).map { it.take(MAX_SHORT_FIELD_LENGTH) },
    previousReflections = previousReflections.takeLast(MAX_PREVIOUS_REFLECTIONS).map { it.take(MAX_PREVIOUS_REFLECTION_LENGTH) },
    theme = theme?.take(MAX_SHORT_FIELD_LENGTH),
    timeOfDay = timeOfDay?.take(MAX_SHORT_FIELD_LENGTH)
)

@Serializable
data class DailyReflectionRequest(
    val figureId: Long,
    val figureName: String,
    val headlines: List<String> = emptyList(),
    val tone: String = "morning",
    val dayOfWeek: String = "",
    val previousScriptures: List<String> = emptyList(),
    val previousReflections: List<String> = emptyList(),
    val theme: String? = null,
    // Sent by newer apps only: "afternoon" for a daytime briefing first opened after noon.
    val timeOfDay: String? = null
)

internal fun DailyReflectionRequest.toServiceRequest() = DailyReflectionService.DailyReflectionRequest(
    figureId = figureId,
    figureName = figureName,
    headlines = headlines,
    tone = tone.ifBlank { "morning" },
    dayOfWeek = dayOfWeek,
    previousScriptures = previousScriptures,
    previousReflections = previousReflections,
    theme = theme?.let { runCatching { ReflectionTheme.valueOf(it.uppercase()) }.getOrNull() },
    writingsOnly = theme.equals(WRITINGS_LENS, ignoreCase = true),
    timeOfDay = timeOfDay?.takeIf { it.isNotBlank() } ?: tone.ifBlank { "morning" }
)

@Serializable
data class DailyReflectionResponse(
    val scriptureReference: String,
    val scriptureText: String,
    val insight: String,
    val implication: String,
    val inspiration: String,
    val sources: List<String>,
    val tone: String,
    val challenge: String? = null
)

private fun DailyReflectionResult.toResponse() = DailyReflectionResponse(
    scriptureReference = scriptureReference,
    scriptureText = scriptureText,
    insight = insight,
    implication = implication,
    inspiration = inspiration,
    sources = sources,
    tone = tone,
    challenge = challenge
)
