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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Curation as seen through a fetch run: what each tab stores when Claude's choice is applied, and when it isn't. */
class HeadlineCurationTest {

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

    private val headlineRepository = HeadlineRepository()
    private val userMessages = mutableListOf<String>()
    private val requestedMaxes = mutableListOf<String?>()

    private val andrewNpr = "Former Prince Andrew files legal action to quash search warrants"
    private val andrewCnn = "Andrew Mountbatten-Windsor takes legal action against UK police"
    private val opinion = "Trump Tries to Escape Reality With Bonkers AI Takeover Fantasy"
    private val minor = "Woman dead after crash south of Lincoln"
    private val misfiled = "Researchers ask if heating the body can treat depression"
    private val generalOnly = "Dennis Hastert, disgraced former House speaker, dies at 84"

    // One article filed under both general and world, a duplicate story from another outlet, an opinion-framed
    // headline, a minor local story, a story GNews filed under the wrong category and a General-only story.
    private val feed = mapOf(
        "world" to listOf(
            andrewNpr to "https://www.npr.org/andrew",
            andrewCnn to "https://www.cnn.com/andrew",
            opinion to "https://www.thedailybeast.com/bonkers",
            minor to "https://www.koln.com/crash"
        ),
        "nation" to listOf(misfiled to "https://www.npr.org/depression"),
        "general" to listOf(generalOnly to "https://www.cbsnews.com/hastert", andrewNpr to "https://www.npr.org/andrew")
    )

    private fun newsClient(byCategory: Map<String, List<Pair<String, String>>>) = NewsApiClient(
        HttpClient(
            MockEngine { request ->
                requestedMaxes += request.url.parameters["max"]
                val articles = byCategory[request.url.parameters["category"]].orEmpty()
                val json = articles.joinToString(prefix = """{"totalArticles":${articles.size},"articles":[""", postfix = "]}") {
                    """{"title":"${it.first}","url":"${it.second}","source":{"name":"Source","url":"https://source.com"}}"""
                }
                respond(json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        ) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        },
        "test-key"
    )

    private fun service(
        byCategory: Map<String, List<Pair<String, String>>> = feed,
        claudeStatus: HttpStatusCode = HttpStatusCode.OK,
        reply: (userMessage: String) -> String
    ) = HeadlineFetchService(
        newsClient(byCategory),
        headlineRepository,
        ArticleScraperService(),
        HeadlineCurationService(claudeApiClientReplying(claudeStatus) { userMessages += it; reply(it) }),
        categoryDelayMillis = 0L
    )

    // Claude's reply choosing each (title, tab), using the ids the curation prompt gave those titles.
    private fun picks(userMessage: String, vararg chosen: Pair<String, String>): String =
        chosen.joinToString(prefix = """{"picks":[""", postfix = "]}") { (title, tab) ->
            val id = Regex("""\[(\d+)][^\n]*\nTitle: ${Regex.escape(title)}\n""").find(userMessage)!!.groupValues[1]
            """{"id":$id,"tab":"$tab"}"""
        }

    private suspend fun stored(tab: String) = headlineRepository.getStored(category = tab).map { it.title }

    @Test
    fun fetchRun_appliesClaudesChoiceToTheShownTabs() = runTest {
        val summary = service { picks(it, andrewNpr to "world", generalOnly to "nation", misfiled to "science") }
            .fetchAndStoreAll(nowMillis = 1000L)

        assertTrue(summary.curated)
        assertEquals(listOf(andrewNpr), stored("world"))
        assertEquals(listOf(generalOnly), stored("nation"))
        assertEquals(listOf(misfiled), stored("science"))
    }

    @Test
    fun fetchRun_makesOneCurationCallThatOffersEachArticleOnceWithAllItsCategories() = runTest {
        service { picks(it, andrewNpr to "world") }.fetchAndStoreAll(nowMillis = 1000L)

        val message = userMessages.single()
        assertEquals(1, Regex(Regex.escape(andrewNpr)).findAll(message).count())
        assertTrue(message.contains("Filed under: general, world | Source"))
    }

    @Test
    fun fetchRun_requests25HeadlinesPerCategory() = runTest {
        service { picks(it, andrewNpr to "world") }.fetchAndStoreAll(nowMillis = 1000L)

        assertEquals(List<String?>(HeadlineFetchService.CATEGORIES.size) { "25" }, requestedMaxes)
    }

    @Test
    fun fetchRun_whenClaudeFails_eachCategoryStoresItsOwnFilteredHeadlines() = runTest {
        val summary = service(claudeStatus = HttpStatusCode.InternalServerError) { "" }.fetchAndStoreAll(nowMillis = 1000L)

        assertFalse(summary.curated)
        assertEquals(listOf(andrewNpr, andrewCnn, opinion, minor).sorted(), stored("world").sorted())
        assertEquals(listOf(misfiled), stored("nation"))
    }

    @Test
    fun fetchRun_whenClaudesReplyIsUnusable_fallsBackToUncuratedHeadlines() = runTest {
        val unusableReplies = listOf(
            "not json",
            """{"picks":[]}""",
            """{"picks":[{"id":999,"tab":"world"}]}""",
            """{"picks":[{"id":1,"tab":"general"}]}""",
            """{"picks":[{"id":1,"tab":"world"},{"id":1,"tab":"nation"}]}"""
        )
        unusableReplies.forEach { reply ->
            val summary = service { reply }.fetchAndStoreAll(nowMillis = 1000L)

            assertFalse(summary.curated, "reply should fall back: $reply")
            assertEquals(4, stored("world").size, "reply should fall back: $reply")
        }
    }

    @Test
    fun fetchRun_storesAtMostTenPerTab_curatedOrNot() = runTest {
        val twelve = mapOf("world" to (1..12).map { "World story $it" to "https://example.com/world/$it" })

        service(twelve, claudeStatus = HttpStatusCode.InternalServerError) { "" }.fetchAndStoreAll(nowMillis = 1000L)
        assertEquals(10, stored("world").size)

        service(twelve) { message -> picks(message, *twelve.getValue("world").map { it.first to "world" }.toTypedArray()) }
            .fetchAndStoreAll(nowMillis = 2000L)
        assertEquals((1..10).map { "World story $it" }, stored("world").sortedBy { it.substringAfterLast(" ").toInt() })
    }

    @Test
    fun fetchRun_aShownTabClaudeLeavesEmpty_keepsItsPreviousHeadlines() = runTest {
        val business = mapOf("business" to listOf("Texas Stock Exchange challenges NYSE" to "https://example.com/tse"))
        service(business, claudeStatus = HttpStatusCode.InternalServerError) { "" }.fetchAndStoreAll(nowMillis = 1000L)

        service { picks(it, andrewNpr to "world") }.fetchAndStoreAll(nowMillis = 2000L)

        assertEquals(listOf("Texas Stock Exchange challenges NYSE"), stored("business"))
    }
}
