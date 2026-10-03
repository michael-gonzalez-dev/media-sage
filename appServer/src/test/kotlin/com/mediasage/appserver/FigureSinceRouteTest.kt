package com.mediasage.appserver

import com.mediasage.appserver.db.FigureTable
import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.repository.FigureRepository
import com.mediasage.appserver.repository.FiguresResponse
import com.mediasage.appserver.routes.figureRoutes
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
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

class FigureSinceRouteTest {

    private val oldTimestamp = 1_000_000L
    private val recentTimestamp = 9_000_000L

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(FigureTable)
            SchemaUtils.create(FigureTable)
        }
        insertFigure("Augustine", "theologian", "4th", "Bishop & Theologian", "354-430", enabled = true, updated = oldTimestamp)
        insertFigure("C.S. Lewis", "author", "20th", "Author & Apologist", "1898-1963", enabled = true, updated = recentTimestamp)
        insertFigure("John Wesley", "reformer", "18th", "Evangelist", "1703-1791", enabled = false, updated = recentTimestamp)
        insertFigure("John Calvin", "reformer", "16th", "Reformer", "1509-1564", enabled = false, updated = oldTimestamp)
    }

    private fun insertFigure(
        figureName: String,
        figureCategory: String,
        figureCentury: String,
        figureRole: String,
        figureLifespan: String,
        enabled: Boolean,
        updated: Long,
    ) = transaction {
        FigureTable.insert {
            it[name] = figureName
            it[category] = figureCategory
            it[century] = figureCentury
            it[role] = figureRole
            it[lifespan] = figureLifespan
            it[bio] = ""
            it[isEnabled] = enabled
            it[updatedAt] = updated
        }
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(FigureTable) }
    }

    @Test
    fun noSinceParamReturnsAllEnabledFigures() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(module { single { FigureRepository("http://localhost:8080") } }) }
        routing { figureRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/figures")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<FiguresResponse>()
        assertEquals(2, body.figures.size)
    }

    @Test
    fun sincePastOldTimestampReturnsOnlyNewerFigures() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(module { single { FigureRepository("http://localhost:8080") } }) }
        routing { figureRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/figures?since=$oldTimestamp")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<FiguresResponse>()
        assertEquals(1, body.figures.size)
        assertEquals("C.S. Lewis", body.figures.first().name)
    }

    @Test
    fun sinceFutureTimestampReturnsEmptyList() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(module { single { FigureRepository("http://localhost:8080") } }) }
        routing { figureRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/figures?since=9999999999999")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<FiguresResponse>()
        assertEquals(0, body.figures.size)
    }

    @Test
    fun sinceListsOnlyFiguresDisabledAfterItAndNeverReturnsThemAsFigures() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(module { single { FigureRepository("http://localhost:8080") } }) }
        routing { figureRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val body = client.get("/api/figures?since=$oldTimestamp").body<FiguresResponse>()
        assertEquals(listOf(3L), body.disabledIds)
        assertEquals(listOf("C.S. Lewis"), body.figures.map { it.name })
    }

    @Test
    fun noSinceParamListsNoDisabledIds() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(module { single { FigureRepository("http://localhost:8080") } }) }
        routing { figureRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val body = client.get("/api/figures").body<FiguresResponse>()
        assertEquals(emptyList(), body.disabledIds)
    }
}
