package com.mediasage.appserver.eval

import com.mediasage.appserver.prompts.ReflectionTheme
import com.mediasage.appserver.service.DailyReflectionService.DailyReflectionRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * One briefing to generate: the figure, the lens, and the week of history the model has to stay fresh
 * against. Fields mirror the request the app sends, so any real briefing can be reproduced as a scenario.
 */
@Serializable
data class BriefingScenario(
    val name: String,
    val description: String = "",
    val figureId: Long,
    val figureName: String,
    val tone: String = "morning",
    /** A [ReflectionTheme] name such as "HOPE". Omitted for the Headlines lens. */
    val theme: String? = null,
    val headlines: List<String> = emptyList(),
    /** ISO date of the briefing. Drives the bibliography rotation and the day of week; defaults to today (UTC). */
    val date: String? = null,
    val previousScriptures: List<String> = emptyList(),
    val previousReflections: List<String> = emptyList()
) {
    val localDate: LocalDate get() = date?.let(LocalDate::parse) ?: LocalDate.now(ZoneOffset.UTC)

    fun toRequest() = DailyReflectionRequest(
        figureId = figureId,
        figureName = figureName,
        headlines = headlines,
        tone = tone,
        dayOfWeek = localDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
        previousScriptures = previousScriptures,
        previousReflections = previousReflections,
        // valueOf, not the route's lenient parse: a typo in the scenario file should fail, not silently drop the theme.
        theme = theme?.let { ReflectionTheme.valueOf(it.uppercase()) }
    )

    companion object {
        private const val RESOURCE = "/briefing-scenarios.json"

        // Strict on unknown keys so a misspelled field in the scenario file fails instead of being ignored.
        private val json = Json

        fun loadAll(): List<BriefingScenario> {
            val text = BriefingScenario::class.java.getResource(RESOURCE)?.readText()
                ?: error("$RESOURCE not found on the eval classpath")
            return json.decodeFromString<List<BriefingScenario>>(text)
        }
    }
}
