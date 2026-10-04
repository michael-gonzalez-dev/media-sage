package com.mediasage.appserver

import com.mediasage.appserver.db.FigureTable
import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.db.WorkTable
import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.repository.WorksResponse
import com.mediasage.appserver.routes.workRoutes
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkRoutesTest {

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(WorkTable, FigureTable)
            SchemaUtils.create(FigureTable, WorkTable)
            val tozer = insertFigure("A.W. Tozer", enabled = true)
            val hidden = insertFigure("Hidden Figure", enabled = false)
            WorkTable.insert {
                it[figureId] = tozer
                it[title] = "The Pursuit of God"
                it[year] = 1948
            }
            WorkTable.insert {
                it[figureId] = tozer
                it[title] = "A Recorded Book"
                it[recordedBy] = "A Friend"
                it[forQuotesOnly] = true
                it[coverUrl] = "https://example.com/cover.jpg"
            }
            WorkTable.insert {
                it[figureId] = hidden
                it[title] = "Hidden Work"
            }
        }
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(WorkTable, FigureTable) }
    }

    @Test
    fun worksEndpointReturnsEveryWorkOfEnabledFiguresIncludingQuotesOnlyBooks() = testApplication {
        val response = worksApp().client.get("/api/works")
        assertEquals(HttpStatusCode.OK, response.status)

        val works = Json.decodeFromString<WorksResponse>(response.bodyAsText()).works
        assertEquals(listOf("The Pursuit of God", "A Recorded Book"), works.map { it.title })
    }

    @Test
    fun worksEndpointCarriesYearRecorderAndCoverUrl() = testApplication {
        val works = Json.decodeFromString<WorksResponse>(worksApp().client.get("/api/works").bodyAsText()).works

        val own = works.first { it.title == "The Pursuit of God" }
        assertEquals(1948, own.year)
        assertNull(own.recordedBy)
        assertNull(own.coverUrl)

        val recorded = works.first { it.title == "A Recorded Book" }
        assertNull(recorded.year)
        assertEquals("A Friend", recorded.recordedBy)
        assertEquals("https://example.com/cover.jpg", recorded.coverUrl)
    }

    private fun ApplicationTestBuilder.worksApp(): ApplicationTestBuilder {
        install(ContentNegotiation) { json() }
        install(Koin) { modules(module { single { WorkRepository() } }) }
        routing { workRoutes() }
        return this
    }

    private fun insertFigure(figureName: String, enabled: Boolean): Long =
        FigureTable.insert {
            it[name] = figureName
            it[category] = "theologian"
            it[century] = "20th"
            it[role] = "Pastor"
            it[lifespan] = "1897-1963"
            it[bio] = ""
            it[isEnabled] = enabled
        }[FigureTable.id]
}
