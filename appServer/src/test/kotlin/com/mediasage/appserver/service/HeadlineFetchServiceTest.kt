package com.mediasage.appserver.service

import com.mediasage.appserver.db.HeadlineTable
import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.repository.HeadlineRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadlineFetchServiceTest {

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(HeadlineTable)
            SchemaUtils.create(HeadlineTable)
        }
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(HeadlineTable) }
    }

    private val sampleResponse = """
    {
        "totalArticles": 1,
        "articles": [
            {
                "title": "Sample headline",
                "description": "desc",
                "content": "content",
                "url": "https://example.com/article",
                "image": null,
                "publishedAt": "2026-04-19T10:00:00Z",
                "source": { "name": "Reuters", "url": "https://reuters.com" }
            }
        ]
    }
    """.trimIndent()

    private val requestsByCategory = mutableMapOf<String, Int>()

    private fun createClient(failingCategory: String): HttpClient =
        createClient { category, _ -> if (category == failingCategory) HttpStatusCode.InternalServerError else HttpStatusCode.OK }

    // [statusFor] receives the category and which attempt this is for it (1 = first request).
    private fun createClient(statusFor: (category: String, attempt: Int) -> HttpStatusCode): HttpClient = HttpClient(
        MockEngine { request ->
            val category = request.url.parameters["category"].orEmpty()
            val attempt = (requestsByCategory[category] ?: 0) + 1
            requestsByCategory[category] = attempt
            val status = statusFor(category, attempt)
            respond(
                content = if (status == HttpStatusCode.OK) sampleResponse else """{"errors":["blocked"]}""",
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    ) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    // Every category returns these (title, url) articles.
    private fun createClientReturning(vararg articles: Pair<String, String>): HttpClient {
        val json = articles.joinToString(prefix = """{"totalArticles":${articles.size},"articles":[""", postfix = "]}") { (title, url) ->
            """{"title":"$title","url":"$url","source":{"name":"Source","url":"https://source.com"}}"""
        }
        return HttpClient(
            MockEngine { respond(json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        ) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    @Test
    fun fetchAndStoreAll_onFullSuccess_fetchesAndStoresAllSevenCategories() = runTest {
        val headlineRepository = HeadlineRepository()
        val newsApiClient = NewsApiClient(createClient(failingCategory = "none"), "test-key")
        val service = HeadlineFetchService(newsApiClient, headlineRepository, ArticleScraperService())

        val summary = service.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals(HeadlineFetchService.CATEGORIES.toSet(), summary.succeeded.toSet())
        assertEquals(emptyList(), summary.failed)
        HeadlineFetchService.CATEGORIES.forEach { category ->
            val stored = headlineRepository.getStored(category = category)
            assertEquals(1, stored.size)
            assertEquals(listOf(category), stored[0].categories)
        }
    }

    @Test
    fun fetchAndStoreAll_onOneCategoryFailure_otherCategoriesStillPopulate() = runTest {
        val headlineRepository = HeadlineRepository()
        val newsApiClient = NewsApiClient(createClient(failingCategory = "science"), "test-key")
        val service = HeadlineFetchService(newsApiClient, headlineRepository, ArticleScraperService())

        val summary = service.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals(listOf("science"), summary.failed)
        assertEquals(HeadlineFetchService.CATEGORIES.size - 1, summary.succeeded.size)
        assertEquals(emptyList(), headlineRepository.getStored(category = "science"))
        assertEquals(1, headlineRepository.getStored(category = "world").size)
    }

    @Test
    fun fetchAndStoreAll_calledTwice_replacesRatherThanAccumulatesPerCategory() = runTest {
        val headlineRepository = HeadlineRepository()
        val newsApiClient = NewsApiClient(createClient(failingCategory = "none"), "test-key")
        val service = HeadlineFetchService(newsApiClient, headlineRepository, ArticleScraperService())

        service.fetchAndStoreAll(nowMillis = 1000L)
        service.fetchAndStoreAll(nowMillis = 2000L)

        assertEquals(1, headlineRepository.getStored(category = "world").size)
    }

    @Test
    fun fetchAndStoreAll_categoryRateLimitedOnce_retriesAndStoresIt() = runTest {
        val headlineRepository = HeadlineRepository()
        val client = createClient { category, attempt ->
            if (category == "technology" && attempt == 1) HttpStatusCode.TooManyRequests else HttpStatusCode.OK
        }
        val service = HeadlineFetchService(NewsApiClient(client, "test-key"), headlineRepository, ArticleScraperService())

        val summary = service.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals(HeadlineFetchService.CATEGORIES.toSet(), summary.succeeded.toSet())
        assertEquals(emptyList(), summary.failed)
        assertEquals(2, requestsByCategory["technology"])
        assertEquals(1, headlineRepository.getStored(category = "technology").size)
    }

    @Test
    fun fetchAndStoreAll_categoryAlwaysRateLimited_retriesOnceThenReportsItFailed() = runTest {
        val headlineRepository = HeadlineRepository()
        val client = createClient { category, _ ->
            if (category == "health") HttpStatusCode.TooManyRequests else HttpStatusCode.OK
        }
        val service = HeadlineFetchService(NewsApiClient(client, "test-key"), headlineRepository, ArticleScraperService())

        val summary = service.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals(listOf("health"), summary.failed)
        assertEquals(2, requestsByCategory["health"])
        assertEquals(emptyList(), headlineRepository.getStored(category = "health"))
    }

    @Test
    fun fetchAndStoreAll_nonRateLimitError_isNotRetried() = runTest {
        val service = HeadlineFetchService(
            NewsApiClient(createClient(failingCategory = "science"), "test-key"),
            HeadlineRepository(),
            ArticleScraperService()
        )

        service.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals(1, requestsByCategory["science"])
    }

    @Test
    fun fetchAndStoreAll_waitsBetweenCategoriesButNotBeforeTheFirst() = runTest {
        val service = HeadlineFetchService(
            NewsApiClient(createClient(failingCategory = "none"), "test-key"),
            HeadlineRepository(),
            ArticleScraperService(),
            categoryDelayMillis = 3_000L
        )

        service.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals((HeadlineFetchService.CATEGORIES.size - 1) * 3_000L, testScheduler.currentTime)
    }

    @Test
    fun fetchAndStoreAll_dropsNonNewsAndStoresTheRest() = runTest {
        val headlineRepository = HeadlineRepository()
        val client = createClientReturning(
            "43 Things Men Go Through That Women Never Realize" to "https://www.buzzfeed.com/a/43-things",
            "Indonesia ferry death toll hits 66" to "https://www.reuters.com/world/asia/ferry"
        )
        val service = HeadlineFetchService(NewsApiClient(client, "test-key"), headlineRepository, ArticleScraperService())

        service.fetchAndStoreAll(nowMillis = 1000L)

        val stored = headlineRepository.getStored(category = "world")
        assertEquals(listOf("Indonesia ferry death toll hits 66"), stored.map { it.title })
    }

    @Test
    fun fetchAndStoreAll_whenEveryHeadlineIsDropped_keepsThePreviousHeadlines() = runTest {
        val headlineRepository = HeadlineRepository()
        val realNews = createClientReturning("Indonesia ferry death toll hits 66" to "https://www.reuters.com/world/asia/ferry")
        HeadlineFetchService(NewsApiClient(realNews, "test-key"), headlineRepository, ArticleScraperService())
            .fetchAndStoreAll(nowMillis = 1000L)

        val allJunk = createClientReturning("Horoscope for Wednesday" to "https://www.sfgate.com/horoscope/article/wednesday")
        HeadlineFetchService(NewsApiClient(allJunk, "test-key"), headlineRepository, ArticleScraperService())
            .fetchAndStoreAll(nowMillis = 2000L)

        val stored = headlineRepository.getStored(category = "world")
        assertEquals(listOf("Indonesia ferry death toll hits 66"), stored.map { it.title })
    }
}
