package com.mediasage.appserver.routes

import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.plugins.ENCOURAGE_RATE_LIMIT
import com.mediasage.appserver.repository.ClaudeCallLimitRepository
import com.mediasage.appserver.repository.EncouragementCacheRepository
import com.mediasage.appserver.repository.FigureRepository
import com.mediasage.appserver.repository.HeadlineRepository
import com.mediasage.appserver.service.ArticleScraperService
import com.mediasage.appserver.service.ClaudeApiClient
import com.mediasage.appserver.service.DailyLimitExceededException
import com.mediasage.appserver.service.EncourageResult
import com.mediasage.appserver.service.NewsArticle
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.time.ZoneOffset

// Generous ceilings for what the app sends (a GNews title, a 200-character snippet, a GNews URL, a locale code).
// Anything past a ceiling is trimmed before it reaches Claude, never rejected: the app shows a failed
// encouragement for any error response, so an app request that happens to run long must still succeed.
internal const val MAX_HEADLINE_TITLE_LENGTH = 512
internal const val MAX_ARTICLE_SNIPPET_LENGTH = 1_000
internal const val MAX_ARTICLE_URL_LENGTH = 1_024
internal const val MAX_LOCALE_LENGTH = 16

@Serializable
private data class EncourageRequest(
    val headlineTitle: String,
    val locale: String = "en",
    val articleUrl: String? = null,
    val articleSnippet: String? = null
)

// A URL longer than the stored feed's URL column can't be a feed headline, so it is dropped rather than trimmed:
// a trimmed URL could collide with a different article's cache entry.
private fun EncourageRequest.bounded() = copy(
    headlineTitle = headlineTitle.take(MAX_HEADLINE_TITLE_LENGTH),
    locale = locale.take(MAX_LOCALE_LENGTH),
    articleUrl = articleUrl?.takeIf { it.length <= MAX_ARTICLE_URL_LENGTH },
    articleSnippet = articleSnippet?.take(MAX_ARTICLE_SNIPPET_LENGTH)
)

/** Analysis endpoints — Claude AI provides encouragement for headlines. */
fun Route.analysisRoutes() {
    val claudeClient by inject<ClaudeApiClient>()
    val scraperService by inject<ArticleScraperService>()
    val figureRepository by inject<FigureRepository>()
    val headlineRepository by inject<HeadlineRepository>()
    val encouragementCacheRepository by inject<EncouragementCacheRepository>()
    val claudeCallLimitRepository by inject<ClaudeCallLimitRepository>()
    val dailyClaudeCallLimit by inject<Int>(named("dailyClaudeCallLimit"))

    route("/api/analysis") {
        rateLimit(ENCOURAGE_RATE_LIMIT) {
            encourageRoute(
                EncourageDependencies(
                    claudeClient,
                    scraperService,
                    headlineRepository,
                    encouragementCacheRepository,
                    claudeCallLimitRepository,
                    dailyClaudeCallLimit
                ),
                figureRepository
            )
        }
    }
}

private class EncourageDependencies(
    val claudeClient: ClaudeApiClient,
    val scraperService: ArticleScraperService,
    val headlineRepository: HeadlineRepository,
    val encouragementCacheRepository: EncouragementCacheRepository,
    val claudeCallLimitRepository: ClaudeCallLimitRepository,
    val dailyClaudeCallLimit: Int
)

private fun Route.encourageRoute(deps: EncourageDependencies, figureRepository: FigureRepository) {
    post("/encourage") {
        val request = call.receive<EncourageRequest>().bounded()

        if (request.headlineTitle.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "headlineTitle is required"))
            return@post
        }

        val cached = request.articleUrl?.let {
            deps.encouragementCacheRepository.getByArticleUrlAndLocale(it, request.locale)
        }
        if (cached != null) {
            call.respond(cached.copy(figureImageUrl = figureRepository.getPortraitUrl(cached.figureName)))
            return@post
        }

        val result = generateAndCacheEncouragement(request, deps)
        call.respond(result.copy(figureImageUrl = figureRepository.getPortraitUrl(result.figureName)))
    }
}

// The server-side cache is shared by every user, so only an encouragement built from the server's own copy
// of a headline is cached. Otherwise a caller could send a real article URL with a made-up title and have
// that encouragement served to everyone who opens the real article. A headline that has rotated out of the
// feed is still encouraged from what the caller sent; it just isn't cached for others.
private suspend fun generateAndCacheEncouragement(request: EncourageRequest, deps: EncourageDependencies): EncourageResult {
    val callDate = LocalDate.now(ZoneOffset.UTC).toString()
    if (!deps.claudeCallLimitRepository.tryConsumeCall(callDate, deps.dailyClaudeCallLimit)) {
        throw DailyLimitExceededException()
    }

    val stored = request.articleUrl?.let { deps.headlineRepository.findByUrl(it) }
    val result = if (stored != null) {
        generateEncouragement(stored.title, request.locale, storedArticleText(stored, deps.scraperService), deps.claudeClient)
    } else {
        generateEncouragement(request.headlineTitle, request.locale, request.articleSnippet, deps.claudeClient)
    }
    stored?.let {
        deps.encouragementCacheRepository.insert(it.url, request.locale, result, System.currentTimeMillis())
    }
    return result
}

private fun storedArticleText(stored: NewsArticle, scraperService: ArticleScraperService): String? =
    stored.snippet.ifBlank { null } ?: scraperService.getArticleText(stored.url)

private suspend fun generateEncouragement(
    headlineTitle: String,
    locale: String,
    articleText: String?,
    claudeClient: ClaudeApiClient
): EncourageResult {
    val candidates = ServerDatabase.fetchQuoteCandidates()
    return claudeClient.encourageHeadline(
        headlineTitle = headlineTitle,
        candidates = candidates,
        locale = locale,
        articleText = articleText
    )
}
