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
 *
 * Every category's filtered headlines then go to [HeadlineCurationService] in one Claude call, which chooses
 * what each shown tab stores. If curation fails, each category stores its own filtered headlines instead.
 */
class HeadlineFetchService(
    private val newsApiClient: NewsApiClient,
    private val headlineRepository: HeadlineRepository,
    private val scraperService: ArticleScraperService,
    private val curationService: HeadlineCurationService,
    private val categoryDelayMillis: Long = DEFAULT_CATEGORY_DELAY_MILLIS,
    private val retryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS
) {
    companion object {
        val CATEGORIES = listOf("general", "world", "nation", "business", "technology", "science", "health")

        /**
         * The categories the app shows as Headlines tabs (HeadlineCategoryFilter in :shared). Curation places
         * headlines only in these; the other fetched categories are stored uncurated, as before.
         */
        val SHOWN_TABS = listOf("world", "nation", "business", "science", "health")
        const val FETCH_LIMIT = 25
        const val MAX_STORED_PER_CATEGORY = 10
        const val DEFAULT_CATEGORY_DELAY_MILLIS = 3_000L
        const val DEFAULT_RETRY_DELAY_MILLIS = 10_000L
        private val log = LoggerFactory.getLogger(HeadlineFetchService::class.java)
    }

    suspend fun fetchAndStoreAll(nowMillis: Long = System.currentTimeMillis()): FetchSummary {
        val fetched = linkedMapOf<String, List<NewsArticle>>()
        val failed = mutableListOf<String>()

        CATEGORIES.forEachIndexed { index, category ->
            if (index > 0) delay(categoryDelayMillis)
            try {
                fetched[category] = filterCategory(category, fetchCategory(category))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.warn("Headline fetch failed for category '{}', keeping previous headlines: {}", category, e.message)
                failed += category
            }
        }

        val curated = if (fetched.isEmpty()) null else curationService.curate(fetched, SHOWN_TABS, MAX_STORED_PER_CATEGORY)
        val uncurated = fetched.mapValues { (_, articles) -> articles.take(MAX_STORED_PER_CATEGORY) }
        val toStore = if (curated != null) uncurated.filterKeys { it !in SHOWN_TABS } + curated else uncurated
        toStore.forEach { (category, articles) -> storeCategory(category, articles, nowMillis) }

        log.info("Headline fetch run finished: succeeded={}, failed={}, curated={}", fetched.keys, failed, curated != null)
        return FetchSummary(succeeded = fetched.keys.toList(), failed = failed, curated = curated != null)
    }

    // Drops non-news (see HeadlineFilter), logging each drop.
    private fun filterCategory(category: String, fetched: List<NewsArticle>): List<NewsArticle> {
        val kept = fetched.filter { article ->
            val reason = HeadlineFilter.dropReason(article) ?: return@filter true
            log.info("Dropped headline in '{}' ({}): {} <{}>", category, reason, article.title, article.url)
            false
        }
        if (kept.isEmpty() && fetched.isNotEmpty()) {
            log.warn("All {} headlines in '{}' were dropped", fetched.size, category)
        }
        return kept
    }

    // A category with nothing to store keeps its previous headlines, so the Headlines tab and briefings never get an
    // empty category from filtering or curation.
    private suspend fun storeCategory(category: String, articles: List<NewsArticle>, nowMillis: Long) {
        if (articles.isEmpty()) {
            log.warn("No headlines to store in '{}', keeping previous headlines", category)
            return
        }
        headlineRepository.replaceCategory(category, articles, nowMillis)
        scraperService.preScrape(articles.map { it.url })
    }

    private suspend fun fetchCategory(category: String): List<NewsArticle> = try {
        newsApiClient.getTopHeadlines(category = category, limit = FETCH_LIMIT)
    } catch (e: NewsApiException) {
        if (e.statusCode != HttpStatusCode.TooManyRequests.value) throw e
        log.info("Headline fetch rate-limited for category '{}', retrying in {} ms", category, retryDelayMillis)
        delay(retryDelayMillis)
        newsApiClient.getTopHeadlines(category = category, limit = FETCH_LIMIT)
    }
}

data class FetchSummary(val succeeded: List<String>, val failed: List<String>, val curated: Boolean = false)
