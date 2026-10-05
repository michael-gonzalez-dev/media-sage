package com.mediasage.appserver

import com.mediasage.appserver.db.HeadlineTable
import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.repository.HeadlineRepository
import com.mediasage.appserver.routes.DEFAULT_HEADLINES_LIMIT
import com.mediasage.appserver.routes.MAX_HEADLINES_LIMIT
import com.mediasage.appserver.routes.newsRoutes
import com.mediasage.appserver.service.NewsArticle
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NewsRoutesTest {

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(HeadlineTable)
            SchemaUtils.create(HeadlineTable)
            HeadlineTable.insert {
                it[uuid] = "world-1"
                it[category] = "world"
                it[title] = "World headline"
                it[url] = "https://example.com/world"
                it[fetchedAt] = 1000L
            }
            HeadlineTable.insert {
                it[uuid] = "business-1"
                it[category] = "business"
                it[title] = "Business headline"
                it[url] = "https://example.com/business"
                it[fetchedAt] = 1000L
            }
        }
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(HeadlineTable) }
    }

    // Only the stored-headline repository is provided: the news routes have no way to reach the live provider,
    // so no request to them can spend GNews quota.
    private fun testKoinModule() = module {
        single { HeadlineRepository() }
    }

    private fun ApplicationTestBuilder.newsClient() = run {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(testKoinModule()) }
        routing { newsRoutes() }
        createClient { install(ContentNegotiation) { json() } }
    }

    @Test
    fun headlinesEndpoint_returnsStoredArticlesAcrossAllCategories() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(testKoinModule()) }
        routing { newsRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/news/headlines")

        assertEquals(HttpStatusCode.OK, response.status)
        val articles = response.body<List<NewsArticle>>()
        assertEquals(2, articles.size)
        assertEquals(setOf("world", "business"), articles.map { it.categories.single() }.toSet())
    }

    @Test
    fun headlinesEndpoint_filtersByCategoryQueryParam() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(testKoinModule()) }
        routing { newsRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/news/headlines?category=business")

        assertEquals(HttpStatusCode.OK, response.status)
        val articles = response.body<List<NewsArticle>>()
        assertEquals(1, articles.size)
        assertEquals(listOf("business"), articles[0].categories)
    }

    private fun seedTiedCategory(category: String, count: Int) = transaction {
        repeat(count) { i ->
            HeadlineTable.insert {
                it[uuid] = "$category-$i"
                it[HeadlineTable.category] = category
                it[title] = "${category.replaceFirstChar(Char::uppercase)} headline $i"
                it[url] = "https://example.com/$category-$i"
                it[fetchedAt] = 1000L
            }
        }
    }

    // A single fetchAndStoreAll() run writes every category's rows with the same fetchedAt, so
    // once a category holds more rows than `limit` a flat ORDER BY has no tiebreak power and can
    // return every slot from one category. Seed two categories well past `limit` here to prove
    // the no-category read still spreads across categories instead of collapsing to one.
    @Test
    fun headlinesEndpoint_withoutCategory_interleavesAcrossTiedCategories() = testApplication {
        seedTiedCategory("nation", count = 5)
        seedTiedCategory("technology", count = 5)

        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(testKoinModule()) }
        routing { newsRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/news/headlines?limit=4")

        assertEquals(HttpStatusCode.OK, response.status)
        val articles = response.body<List<NewsArticle>>()
        assertEquals(4, articles.size)
        assertEquals(4, articles.map { it.categories.single() }.toSet().size)
    }

    @Test
    fun headlinesEndpoint_capsLimitAboveMaximum() = testApplication {
        seedTiedCategory("nation", count = MAX_HEADLINES_LIMIT + 20)
        val client = newsClient()

        val articles = client.get("/api/news/headlines?category=nation&limit=10000").body<List<NewsArticle>>()

        assertEquals(MAX_HEADLINES_LIMIT, articles.size)
    }

    @Test
    fun headlinesEndpoint_servesTheAppsFullRequestUncut() = testApplication {
        seedTiedCategory("nation", count = MAX_HEADLINES_LIMIT)
        val client = newsClient()

        val articles = client.get("/api/news/headlines?category=nation&limit=$MAX_HEADLINES_LIMIT").body<List<NewsArticle>>()

        assertEquals(MAX_HEADLINES_LIMIT, articles.size)
    }

    @Test
    fun headlinesEndpoint_invalidLimitFallsBackToDefault() = testApplication {
        seedTiedCategory("nation", count = DEFAULT_HEADLINES_LIMIT + 20)
        val client = newsClient()

        listOf("0", "-1", "abc").forEach { limit ->
            val response = client.get("/api/news/headlines?category=nation&limit=$limit")

            assertEquals(HttpStatusCode.OK, response.status, "limit=$limit")
            assertEquals(DEFAULT_HEADLINES_LIMIT, response.body<List<NewsArticle>>().size, "limit=$limit")
        }
    }

    @Test
    fun searchEndpoint_isGone() = testApplication {
        val client = newsClient()

        val response = client.get("/api/news/search?query=earthquake")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
