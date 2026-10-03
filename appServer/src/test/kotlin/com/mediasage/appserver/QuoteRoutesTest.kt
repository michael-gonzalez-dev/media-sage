package com.mediasage.appserver

import com.mediasage.appserver.db.FigureTable
import com.mediasage.appserver.db.QuoteTable
import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.repository.QuoteRepository
import com.mediasage.appserver.repository.QuotesResponse
import com.mediasage.appserver.routes.quoteRoutes
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

class QuoteRoutesTest {

    @BeforeTest
    fun setup() {
        ServerDatabase.init(":memory:")
        transaction {
            SchemaUtils.drop(QuoteTable, FigureTable)
            SchemaUtils.create(FigureTable, QuoteTable)
        }
        val augustine = insertFigure("Augustine", enabled = true)
        val wesley = insertFigure("John Wesley", enabled = false)
        insertQuote(augustine, "Our heart is restless until it rests in you.", "Confessions, I.1 (397)", verified = true)
        insertQuote(augustine, "An unverified line.", "Unknown", verified = false)
        insertQuote(wesley, "Do all the good you can.", "Attributed", verified = true)
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(QuoteTable, FigureTable) }
    }

    private fun insertFigure(figureName: String, enabled: Boolean): Long = transaction {
        FigureTable.insert {
            it[name] = figureName
            it[category] = "theologian"
            it[century] = "4th"
            it[role] = ""
            it[lifespan] = ""
            it[bio] = ""
            it[isEnabled] = enabled
        }[FigureTable.id]
    }

    private fun insertQuote(figure: Long, quoteText: String, source: String, verified: Boolean) = transaction {
        QuoteTable.insert {
            it[figureId] = figure
            it[text] = quoteText
            it[sourceText] = source
            it[QuoteTable.verified] = verified
        }
    }

    @Test
    fun returnsOnlyVerifiedQuotesOfEnabledFiguresWithTheirSource() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
        install(Koin) { modules(module { single { QuoteRepository() } }) }
        routing { quoteRoutes() }

        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.get("/api/quotes")

        assertEquals(HttpStatusCode.OK, response.status)
        val quote = response.body<QuotesResponse>().quotes.single()
        assertEquals("Our heart is restless until it rests in you.", quote.text)
        assertEquals("Confessions, I.1 (397)", quote.source)
    }
}
