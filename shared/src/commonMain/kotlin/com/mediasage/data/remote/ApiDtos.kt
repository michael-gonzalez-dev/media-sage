package com.mediasage.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- News endpoint DTOs ----

@Serializable
data class NewsArticleDto(
    val uuid: String,
    val title: String,
    val description: String = "",
    val snippet: String = "",
    val url: String,
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("published_at")
    val publishedAt: String = "",
    val source: String = "",
    val categories: List<String> = emptyList()
)

// ---- Encourage endpoint DTOs ----

@Serializable
data class EncourageRequestDto(
    val headlineTitle: String,
    val locale: String = "en",
    val articleUrl: String? = null,
    val articleSnippet: String? = null
)

@Serializable
data class EncourageResultDto(
    val summary: String? = null,
    val quoteText: String,
    val figureName: String,
    val figureRole: String,
    val scriptureReference: String,
    val scriptureText: String,
    val explanation: String,
    val connectionThemes: List<String>,
    val matchTheme: String,
    val tone: String,
    val figureImageUrl: String? = null
)

// ---- Legacy Match endpoint DTOs (deprecated — TODO MS-46) ----

@Serializable
data class MatchRequestDto(
    val headlineTitle: String,
    val candidates: List<MatchCandidateDto>
)

@Serializable
data class MatchCandidateDto(
    val id: Long,
    val figureName: String,
    val text: String,
    val source: String,
    val themes: List<String> = emptyList()
)

@Serializable
data class MatchResultDto(
    val selectedQuoteId: Long,
    val confidence: Float,
    val explanation: String,
    val connectionThemes: List<String>
)

// ---- Figure endpoint DTOs ----

@Serializable
data class FiguresResponse(
    val syncedAt: Long,
    val figures: List<FigureDto>,
    // Server ids of figures disabled since the requested time. Empty on a full sync.
    val disabledIds: List<Long> = emptyList()
)

@Serializable
data class FigureDto(
    val id: Long,
    val name: String,
    val category: String,
    val century: String,
    val role: String = "",
    val lifespan: String = "",
    val bio: String = "",
    val themes: String = "",
    val knownFor: String = "",
    @SerialName("portraitUrl")
    val portraitUrl: String? = null,
    val isEnabled: Boolean = true,
    val updatedAt: Long = 0
)

// ---- Library endpoint DTOs ----

@Serializable
data class WorksResponse(val works: List<WorkDto>)

@Serializable
data class WorkDto(
    val id: Long,
    /** Server id of the figure whose words the work holds. */
    val figureId: Long,
    val title: String,
    val year: Int? = null,
    /** Who wrote the book when it isn't the figure; it preserves the figure's words as that person recorded them. */
    val recordedBy: String? = null,
    val coverUrl: String? = null
)

// ---- Quote library endpoint DTOs ----

// Always the whole library: anything a phone saved that isn't in it has been removed or unverified.
@Serializable
data class QuotesResponse(
    val quotes: List<QuoteDto>
)

@Serializable
data class QuoteDto(
    val figureId: Long,
    val text: String,
    val source: String = "",
    val themes: String = ""
)

// ---- Daily Reflection endpoint DTOs ----

@Serializable
data class DailyReflectionRequestDto(
    @SerialName("figureId")
    val figureId: Long,
    @SerialName("figureName")
    val figureName: String,
    val headlines: List<String> = emptyList(),
    val tone: String = "morning",
    val dayOfWeek: String = "",
    val previousScriptures: List<String> = emptyList(),
    val previousReflections: List<String> = emptyList(),
    val theme: String? = null,
    val timeOfDay: String? = null
)

@Serializable
data class DailyReflectionResponseDto(
    val scriptureReference: String,
    val scriptureText: String,
    val insight: String,
    val implication: String,
    val inspiration: String,
    val sources: List<String>,
    val tone: String,
    val challenge: String? = null
)

// ---- Assignment defaults endpoint DTOs ----

@Serializable
data class AssignmentDefaultDto(
    val dayOrdinal: Int,
    val figureName: String,
)
