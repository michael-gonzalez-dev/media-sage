package com.mediasage.appserver

import com.mediasage.appserver.db.ClaudeCallLimitTable
import com.mediasage.appserver.db.EncouragementCacheTable
import com.mediasage.appserver.db.FigureTable
import com.mediasage.appserver.db.HeadlineTable
import com.mediasage.appserver.db.QuoteTable
import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.repository.ClaudeCallLimitRepository
import com.mediasage.appserver.repository.EncouragementCacheRepository
import com.mediasage.appserver.plugins.CallerRateLimits
import com.mediasage.appserver.plugins.configureRateLimiting
import com.mediasage.appserver.plugins.configureStatusPages
import com.mediasage.appserver.repository.FigureRepository
import com.mediasage.appserver.repository.HeadlineRepository
import com.mediasage.appserver.routes.MAX_ARTICLE_SNIPPET_LENGTH
import com.mediasage.appserver.routes.MAX_ARTICLE_URL_LENGTH
import com.mediasage.appserver.routes.MAX_HEADLINE_TITLE_LENGTH
import com.mediasage.appserver.routes.MAX_LOCALE_LENGTH
import com.mediasage.appserver.routes.analysisRoutes
import com.mediasage.appserver.service.ArticleScraperService
import com.mediasage.appserver.service.ClaudeApiClient
import com.mediasage.appserver.service.EncourageResult
import com.mediasage.appserver.service.EncourageTone
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.content.TextContent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

private fun claudeResponseBody(quoteId: Long) = """
{
    "id": "msg_1",
    "type": "message",
    "role": "assistant",
    "model": "claude-sonnet-4-6",
    "content": [{"type":"text","text":"{\"selectedQuoteId\":$quoteId,\"summary\":\"Stay hopeful\",\"scriptureReference\":\"Psalm 46:10\",\"scriptureText\":\"Be still, and know that I am God.\",\"explanation\":\"A call to trust.\",\"connectionThemes\":[\"peace\"],\"matchTheme\":\"peace\",\"tone\":\"COMFORT\"}"}],
    "usage": {"input_tokens": 10, "output_tokens": 10}
}
"""

private fun sampleEncourageResult(figureName: String) = EncourageResult(
    summary = "Stay hopeful",
    quoteText = "Be still and know that I am God.",
    quoteSource = "Psalms",
    figureName = figureName,
    figureRole = "Prophet",
    scriptureReference = "Psalm 46:10",
    scriptureText = "Be still, and know that I am God.",
    explanation = "A call to trust.",
    connectionThemes = listOf("peace"),
    matchTheme = "peace",
    tone = EncourageTone.COMFORT
)

private fun mockClaudeApiClient(quoteId: Long, onCall: () -> Unit, onPrompt: (String) -> Unit): ClaudeApiClient {
    val httpClient = HttpClient(MockEngine { request ->
        onCall()
        onPrompt((request.body as TextContent).text)
        respond(
            content = claudeResponseBody(quoteId),
            status = HttpStatusCode.OK,
            headers = headersOf(io.ktor.http.HttpHeaders.ContentType, "application/json")
        )
    }) {
        install(ClientContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    return ClaudeApiClient(httpClient, "test-key")
}

private fun ApplicationTestBuilder.installRoutes(
    quoteId: Long,
    claudeCallCount: () -> Unit,
    dailyClaudeCallLimit: Int = 300,
    encouragePerHour: Int = 1_000,
    onPrompt: (String) -> Unit = {}
) {
    install(ContentNegotiation) { json() }
    application { configureRateLimiting(CallerRateLimits(encouragePerHour = encouragePerHour, dailyReflectionPerHour = 1_000)) }
    application { configureStatusPages() }
    install(Koin) {
        modules(
            module {
                single { mockClaudeApiClient(quoteId, claudeCallCount, onPrompt) }
                single { HeadlineRepository() }
                single { ArticleScraperService() }
                single { FigureRepository("http://localhost:8080") }
                single { EncouragementCacheRepository() }
                single { ClaudeCallLimitRepository() }
                single<Int>(named("dailyClaudeCallLimit")) { dailyClaudeCallLimit }
            }
        )
    }
    routing { analysisRoutes() }
}

class EncourageRoutesTest {

    private var quoteId: Long = 0

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(FigureTable, QuoteTable, EncouragementCacheTable, ClaudeCallLimitTable, HeadlineTable)
            SchemaUtils.create(FigureTable, QuoteTable, EncouragementCacheTable, ClaudeCallLimitTable, HeadlineTable)
            val figureId = FigureTable.insert {
                it[name] = "Test Figure"
                it[category] = "theologian"
                it[century] = "16th"
                it[role] = "Prophet"
            }[FigureTable.id]
            quoteId = QuoteTable.insert {
                it[QuoteTable.figureId] = figureId
                it[text] = "Be still and know that I am God."
                it[sourceText] = "Psalms"
                it[verified] = true
            }[QuoteTable.id]
        }
    }

    @Test
    fun quoteCandidatesExcludeQuotesWhoseAttributionIsUnverified() {
        transaction {
            val figureId = FigureTable.insert {
                it[name] = "Unverified Figure"
                it[category] = "theologian"
                it[century] = "18th"
            }[FigureTable.id]
            QuoteTable.insert {
                it[QuoteTable.figureId] = figureId
                it[text] = "A saying no source supports."
                it[verified] = false
            }
        }

        val candidates = ServerDatabase.fetchQuoteCandidates()

        assertEquals(listOf(quoteId), candidates.map { it.quoteId })
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(FigureTable, QuoteTable, EncouragementCacheTable, ClaudeCallLimitTable, HeadlineTable)
        }
    }

    @Test
    fun cacheLookupIsScopedPerLocaleNotJustArticleUrl() = runTest {
        val repository = EncouragementCacheRepository()
        val enResult = sampleEncourageResult(figureName = "English Figure")
        val esResult = sampleEncourageResult(figureName = "Spanish Figure")
        val articleUrl = "https://example.com/locale"

        repository.insert(articleUrl, "en", enResult, cachedAt = 1L)
        repository.insert(articleUrl, "es", esResult, cachedAt = 2L)

        assertEquals("English Figure", repository.getByArticleUrlAndLocale(articleUrl, "en")?.figureName)
        assertEquals("Spanish Figure", repository.getByArticleUrlAndLocale(articleUrl, "es")?.figureName)
        assertEquals(null, repository.getByArticleUrlAndLocale(articleUrl, "fr"))
    }

    @Test
    fun newArticleTriggersClaudeCallAndIsCached() = testApplication {
        var callCount = 0
        installRoutes(quoteId, claudeCallCount = { callCount++ })

        val response = client.post("/api/analysis/encourage") {
            contentType(ContentType.Application.Json)
            setBody("""{"headlineTitle":"Markets rally","articleUrl":"https://example.com/a"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, callCount)
    }

    private fun storeHeadline(url: String, title: String) = transaction {
        HeadlineTable.insert {
            it[uuid] = url
            it[category] = "general"
            it[HeadlineTable.title] = title
            it[HeadlineTable.url] = url
            it[fetchedAt] = 1000L
        }
    }

    private suspend fun ApplicationTestBuilder.encourage(body: String, callerIp: String? = null) =
        client.post("/api/analysis/encourage") {
            contentType(ContentType.Application.Json)
            callerIp?.let { header("X-Real-IP", it) }
            setBody(body)
        }

    @Test
    fun repeatedArticleUrlDoesNotTriggerSecondClaudeCall() = testApplication {
        storeHeadline("https://example.com/b", "Markets rally")
        var callCount = 0
        installRoutes(quoteId, claudeCallCount = { callCount++ })

        repeat(2) {
            val response = client.post("/api/analysis/encourage") {
                contentType(ContentType.Application.Json)
                setBody("""{"headlineTitle":"Markets rally","articleUrl":"https://example.com/b"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }

        assertEquals(1, callCount)
    }

    @Test
    fun dailyLimitBlocksFurtherCallsWithErrorResponse() = testApplication {
        var callCount = 0
        installRoutes(quoteId, claudeCallCount = { callCount++ }, dailyClaudeCallLimit = 1)

        val first = client.post("/api/analysis/encourage") {
            contentType(ContentType.Application.Json)
            setBody("""{"headlineTitle":"Headline one","articleUrl":"https://example.com/c"}""")
        }
        assertEquals(HttpStatusCode.OK, first.status)

        val second = client.post("/api/analysis/encourage") {
            contentType(ContentType.Application.Json)
            setBody("""{"headlineTitle":"Headline two","articleUrl":"https://example.com/d"}""")
        }

        assertEquals(HttpStatusCode.TooManyRequests, second.status)
        assertEquals(1, callCount)
    }

    @Test
    fun urlNotInTheFeedIsEncouragedButNotCachedForOthers() = testApplication {
        installRoutes(quoteId, claudeCallCount = {})
        val url = "https://example.com/rotated-out"

        val response = encourage("""{"headlineTitle":"Markets rally","articleUrl":"$url"}""")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(null, EncouragementCacheRepository().getByArticleUrlAndLocale(url, "en"))
    }

    // A caller sending a real feed URL with a made-up title must not decide what that article's
    // shared, cached encouragement says: Claude gets the server's own copy of the headline.
    @Test
    fun feedHeadlineIsEncouragedFromTheServersOwnTitleNotTheCallers() = testApplication {
        storeHeadline("https://example.com/real", "Rescuers reach flood victims")
        val prompts = mutableListOf<String>()
        installRoutes(quoteId, claudeCallCount = {}, onPrompt = { prompts += it })

        encourage("""{"headlineTitle":"Made-up title","articleUrl":"https://example.com/real"}""")

        assertTrue(prompts.single().contains("Rescuers reach flood victims"))
        assertFalse(prompts.single().contains("Made-up title"))
    }

    @Test
    fun callerSuppliedUrlIsNeverFetched() = testApplication {
        val hits = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange ->
                hits.incrementAndGet()
                exchange.sendResponseHeaders(200, -1)
                exchange.close()
            }
            start()
        }
        try {
            installRoutes(quoteId, claudeCallCount = {})

            val url = "http://127.0.0.1:${server.address.port}/internal"
            val response = encourage("""{"headlineTitle":"Markets rally","articleUrl":"$url"}""")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, hits.get())
        } finally {
            server.stop(0)
        }
    }

    // The app shows a failed encouragement for any error response, so an oversized request is trimmed
    // before it reaches Claude rather than rejected.
    @Test
    fun oversizedFieldsAreTrimmedBeforeClaudeNotRejected() = testApplication {
        val prompts = mutableListOf<String>()
        installRoutes(quoteId, claudeCallCount = {}, onPrompt = { prompts += it })
        val longTitle = "t".repeat(MAX_HEADLINE_TITLE_LENGTH + 500)
        val longSnippet = "s".repeat(MAX_ARTICLE_SNIPPET_LENGTH + 500)

        val response = encourage(
            """{"headlineTitle":"$longTitle","articleSnippet":"$longSnippet","locale":"${"x".repeat(MAX_LOCALE_LENGTH + 5)}"}"""
        )

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(prompts.single().contains("t".repeat(MAX_HEADLINE_TITLE_LENGTH)))
        assertFalse(prompts.single().contains("t".repeat(MAX_HEADLINE_TITLE_LENGTH + 1)))
        assertFalse(prompts.single().contains("s".repeat(MAX_ARTICLE_SNIPPET_LENGTH + 1)))
    }

    @Test
    fun overlongUrlIsTreatedAsNotInTheFeedAndNotCached() = testApplication {
        installRoutes(quoteId, claudeCallCount = {})
        val url = "https://example.com/" + "x".repeat(MAX_ARTICLE_URL_LENGTH)

        val response = encourage("""{"headlineTitle":"Markets rally","articleUrl":"$url"}""")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(null, EncouragementCacheRepository().getByArticleUrlAndLocale(url, "en"))
    }

    @Test
    fun oneCallerIsRateLimitedBeforeTheDailyBudgetRunsOut() = testApplication {
        var callCount = 0
        installRoutes(quoteId, claudeCallCount = { callCount++ }, encouragePerHour = 1)

        val first = encourage("""{"headlineTitle":"Headline one","articleUrl":"https://example.com/e"}""", callerIp = "203.0.113.7")
        val second = encourage("""{"headlineTitle":"Headline two","articleUrl":"https://example.com/f"}""", callerIp = "203.0.113.7")

        assertEquals(HttpStatusCode.OK, first.status)
        assertEquals(HttpStatusCode.TooManyRequests, second.status)
        assertEquals(1, callCount)
    }

    // Without X-Real-IP every request would share Railway's proxy address, so one bucket would throttle
    // the whole app. Those requests are not limited per caller; the daily budget still applies.
    @Test
    fun requestsWithoutACallerAddressAreNotLimitedPerCaller() = testApplication {
        storeHeadline("https://example.com/g", "Headline one")
        installRoutes(quoteId, claudeCallCount = {}, encouragePerHour = 1)

        // The second request is a cache hit, so it passes only if the per-caller limit let it through.
        val first = encourage("""{"headlineTitle":"Headline one","articleUrl":"https://example.com/g"}""")
        val second = encourage("""{"headlineTitle":"Headline one","articleUrl":"https://example.com/g"}""")

        assertEquals(HttpStatusCode.OK, first.status)
        assertEquals(HttpStatusCode.OK, second.status)
    }
}
