package com.mediasage.appserver.service

import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.db.WorkTable
import com.mediasage.appserver.repository.WorkRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DailyReflectionServiceTest {

    private val sentPrompts = mutableListOf<String>()

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(WorkTable)
            SchemaUtils.create(WorkTable)
        }
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(WorkTable) }
    }

    private fun seedWorks(
        figureId: Long,
        titles: List<String>,
        recordedBy: String? = null,
        isLifeOfAnother: Boolean = false,
        forQuotesOnly: Boolean = false,
    ) = transaction {
        titles.forEach { title ->
            WorkTable.insert {
                it[WorkTable.figureId] = figureId
                it[WorkTable.title] = title
                it[WorkTable.year] = null
                it[WorkTable.recordedBy] = recordedBy
                it[WorkTable.isLifeOfAnother] = isLifeOfAnother
                it[WorkTable.forQuotesOnly] = forQuotesOnly
            }
        }
    }

    private fun service(returnedSources: List<String>, day: String = "2026-09-24") = DailyReflectionService(
        claudeApiClient = ClaudeApiClient(claudeClient(returnedSources), "test-key"),
        workRepository = WorkRepository(),
        clock = Clock.fixed(Instant.parse("${day}T12:00:00Z"), ZoneOffset.UTC)
    )

    private fun claudeClient(returnedSources: List<String>) = HttpClient(
        MockEngine { request ->
            sentPrompts += (request.body as TextContent).text
            respond(
                content = claudeResponse(returnedSources),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    ) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun claudeResponse(sources: List<String>): String {
        val reflection = buildJsonObject {
            put("scriptureReference", "Psalm 46:10")
            put("scriptureText", "Be still, and know that I am God.")
            put("insight", "i")
            put("implication", "im")
            put("inspiration", "in")
            put("sources", JsonArray(sources.map { JsonPrimitive(it) }))
            put("challenge", "c")
        }.toString()
        return buildJsonObject {
            put("id", "msg_1")
            put("type", "message")
            put("role", "assistant")
            put("model", "test")
            put("content", JsonArray(listOf(buildJsonObject { put("type", "text"); put("text", reflection) })))
            put("usage", buildJsonObject { put("input_tokens", 1); put("output_tokens", 1) })
        }.toString()
    }

    private fun request(tone: String = "morning") =
        DailyReflectionService.DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", tone = tone)

    @Test
    fun generate_dropsReturnedSourcesThatAreNotInTheFiguresBibliography() = runTest {
        seedWorks(19, listOf("The Pursuit of God", "The Knowledge of the Holy"))

        val result = service(returnedSources = listOf("The Pursuit of God", "A Treatise Tozer Never Wrote"))
            .generate(request())

        assertEquals(listOf("The Pursuit of God"), result.sources)
    }

    @Test
    fun generate_pointsConsecutiveDaysAtDifferentWorks() = runTest {
        seedWorks(19, (1..12).map { "Work $it" })

        service(emptyList(), day = "2026-09-24").generate(request())
        service(emptyList(), day = "2026-09-25").generate(request())

        assertNotEquals(worksInPrompt(sentPrompts[0]), worksInPrompt(sentPrompts[1]))
        assertEquals(5, worksInPrompt(sentPrompts[0]).size)
    }

    @Test
    fun generate_stillProducesABriefingForAFigureWithNoBibliography() = runTest {
        val result = service(returnedSources = listOf("Something Invented")).generate(request())

        assertEquals("Psalm 46:10", result.scriptureReference)
        assertTrue(result.sources.isEmpty())
        assertTrue(sentPrompts.single().contains("Return an empty sources list"))
    }

    @Test
    fun generate_citesTheRecordOfTheirWordsForAFigureWhoWroteNothing() = runTest {
        seedWorks(53, listOf("Scenes in the Life of Harriet Tubman"), recordedBy = "Sarah Bradford")

        val result = service(returnedSources = listOf("words recorded by Sarah Bradford in Scenes in the Life of Harriet Tubman"))
            .generate(DailyReflectionService.DailyReflectionRequest(figureId = 53, figureName = "Harriet Tubman"))

        assertEquals(listOf("words recorded by Sarah Bradford in Scenes in the Life of Harriet Tubman"), result.sources)
        assertTrue(sentPrompts.single().contains("Draw on Harriet Tubman's recorded words in it"))
    }

    @Test
    fun generate_neverCreditsAFigureWithAFullWindowOfTheirOwnWorksToARecordedOne() = runTest {
        seedWorks(19, (1..5).map { "Work $it" })
        seedWorks(19, listOf("A Biography of Tozer"), recordedBy = "A Biographer")

        val result = service(returnedSources = listOf("Work 1", "A Biography of Tozer")).generate(request())

        assertEquals(listOf("Work 1"), result.sources)
        assertFalse(sentPrompts.single().contains("A Biography of Tozer"))
    }

    @Test
    fun generate_neverOffersOrCitesARecordKeptOnlyForQuotes() = runTest {
        seedWorks(82, listOf("Hudson Taylor's Spiritual Secret"), recordedBy = "Howard Taylor and Geraldine Taylor")
        seedWorks(82, listOf("Hudson Taylor in Early Years"), recordedBy = "Howard Taylor and Geraldine Taylor", forQuotesOnly = true)

        val result = service(returnedSources = listOf("Hudson Taylor's Spiritual Secret", "Hudson Taylor in Early Years"))
            .generate(DailyReflectionService.DailyReflectionRequest(figureId = 82, figureName = "Hudson Taylor"))

        assertEquals(listOf("words recorded by Howard Taylor and Geraldine Taylor in Hudson Taylor's Spiritual Secret"), result.sources)
        assertFalse(sentPrompts.single().contains("Hudson Taylor in Early Years"))
    }

    private fun writingsRequest(history: List<String> = emptyList()) = DailyReflectionService.DailyReflectionRequest(
        figureId = 19, figureName = "A.W. Tozer", writingsOnly = true, previousReflections = history
    )

    @Test
    fun generate_writingsBriefingListsExactlyOneSourceWithNoChapter() = runTest {
        seedWorks(19, listOf("The Pursuit of God"))

        val result = service(returnedSources = listOf("The Pursuit of God, Chapter 7", "The Pursuit of God"))
            .generate(writingsRequest())

        assertEquals(listOf("The Pursuit of God"), result.sources)
    }

    @Test
    fun generate_writingsBriefingNeverCitesAWorkItWasNotOffered() = runTest {
        seedWorks(19, (1..10).map { "Work $it" })
        val history = listOf("Monday morning, Writings lens (drew on Work 3): An idea.")

        val result = service(returnedSources = listOf("Work 9", "Work 4")).generate(writingsRequest(history))

        assertEquals(setOf("Work 3", "Work 4"), worksInPrompt(sentPrompts.single()))
        assertEquals(listOf("Work 4"), result.sources)
    }

    @Test
    fun generate_writingsBriefingAsksForJustOneWork() = runTest {
        seedWorks(19, listOf("The Pursuit of God", "The Knowledge of the Holy"))

        service(emptyList()).generate(writingsRequest())

        assertTrue(sentPrompts.single().contains("just one of the source works above"))
    }

    @Test
    fun generate_otherLensesStillListEveryWorkTheyDrewOn() = runTest {
        seedWorks(19, (1..5).map { "Work $it" })

        val result = service(returnedSources = listOf("Work 1", "Work 2", "Work 3")).generate(request())

        assertEquals(listOf("Work 1", "Work 2", "Work 3"), result.sources)
        assertFalse(sentPrompts.single().contains("just one of the source works above"))
    }

    @Test
    fun generate_otherLensesStillOfferAndCiteALifeOfAnother() = runTest {
        seedWorks(19, listOf("Wingspread"), isLifeOfAnother = true)
        seedWorks(19, listOf("The Pursuit of God"))

        val result = service(returnedSources = listOf("Wingspread", "The Pursuit of God")).generate(request())

        assertEquals(listOf("Wingspread", "The Pursuit of God"), result.sources)
        assertTrue(sentPrompts.single().contains("- Wingspread"))
    }

    @Test
    fun generate_writingsBriefingIsNeverOfferedALifeOfAnother() = runTest {
        seedWorks(19, listOf("The Pursuit of God"))
        seedWorks(19, listOf("Wingspread"), isLifeOfAnother = true)
        val history = listOf("Monday morning, Writings lens (drew on The Pursuit of God): An idea.")

        service(emptyList()).generate(writingsRequest(history))

        assertFalse(sentPrompts.single().contains("Wingspread"))
    }

    private fun worksInPrompt(requestBody: String): Set<String> =
        Regex("""- (Work \d+)""").findAll(requestBody).map { it.groupValues[1] }.toSet()
}
