package com.mediasage.appserver.service

import com.mediasage.appserver.repository.HeadlineRepository
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

/**
 * Fetches all relevant GNews categories and stores the results, so clients read from the
 * shared cache instead of triggering a live provider call per device/request.
 *
 * GNews rejects short bursts of requests with 429, so categories are fetched one at a time with
 * [categoryDelayMillis] between them, and a rate-limited category is retried once after
 * [retryDelayMillis]. A category that still fails keeps its previously stored headlines.
 */
class HeadlineFetchService(
    private val newsApiClient: NewsApiClient,
    private val headlineRepository: HeadlineRepository,
    private val scraperService: ArticleScraperService,
    private val categoryDelayMillis: Long = DEFAULT_CATEGORY_DELAY_MILLIS,
    private val retryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS
) {
    companion object {
        val CATEGORIES = listOf("general", "world", "nation", "business", "technology", "science", "health")
        const val DEFAULT_CATEGORY_DELAY_MILLIS = 3_000L
        const val DEFAULT_RETRY_DELAY_MILLIS = 10_000L
        private val log = LoggerFactory.getLogger(HeadlineFetchService::class.java)
    }

    suspend fun fetchAndStoreAll(nowMillis: Long = System.currentTimeMillis()): FetchSummary {
        val succeeded = mutableListOf<String>()
        val failed = mutableListOf<String>()

        CATEGORIES.forEachIndexed { index, category ->
            if (index > 0) delay(categoryDelayMillis)
            try {
                val articles = fetchCategory(category)
                headlineRepository.replaceCategory(category, articles, nowMillis)
                scraperService.preScrape(articles.map { it.url })
                succeeded += category
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.warn("Headline fetch failed for category '{}', keeping previous headlines: {}", category, e.message)
                failed += category
            }
        }

        log.info("Headline fetch run finished: succeeded={}, failed={}", succeeded, failed)
        return FetchSummary(succeeded = succeeded, failed = failed)
    }

    private suspend fun fetchCategory(category: String): List<NewsArticle> = try {
        newsApiClient.getTopHeadlines(category = category)
    } catch (e: NewsApiException) {
        if (e.statusCode != HttpStatusCode.TooManyRequests.value) throw e
        log.info("Headline fetch rate-limited for category '{}', retrying in {} ms", category, retryDelayMillis)
        delay(retryDelayMillis)
        newsApiClient.getTopHeadlines(category = category)
    }
}

data class FetchSummary(val succeeded: List<String>, val failed: List<String>)
