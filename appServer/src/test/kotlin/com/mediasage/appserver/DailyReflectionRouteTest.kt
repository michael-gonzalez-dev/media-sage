package com.mediasage.appserver

import com.mediasage.appserver.prompts.ReflectionTheme
import com.mediasage.appserver.routes.DailyReflectionRequest
import com.mediasage.appserver.routes.toServiceRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DailyReflectionRouteTest {

    @Test
    fun rejectsMissingFigureName() = testApplication {
        environment { config = MapApplicationConfig("app.db.path" to ":memory:") }
        application { module() }

        val response = client.post("/api/analysis/daily-reflection") {
            contentType(ContentType.Application.Json)
            setBody("""{"figureId":1,"figureName":"","headlines":[],"tone":"morning"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("required"))
    }

    @Test
    fun rejectsZeroFigureId() = testApplication {
        environment { config = MapApplicationConfig("app.db.path" to ":memory:") }
        application { module() }

        val response = client.post("/api/analysis/daily-reflection") {
            contentType(ContentType.Application.Json)
            setBody("""{"figureId":0,"figureName":"C.S. Lewis","headlines":[],"tone":"morning"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("required"))
    }

    @Test
    fun writingsLensAsksForTheFiguresWorksWithNoTheme() {
        val request = DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", theme = "WRITINGS").toServiceRequest()

        assertTrue(request.writingsOnly)
        assertNull(request.theme)
        assertEquals(emptyList(), request.headlines)
    }

    @Test
    fun themeLensKeepsItsThemeAndIsNotWritingsOnly() {
        val request = DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", theme = "HOPE").toServiceRequest()

        assertEquals(ReflectionTheme.HOPE, request.theme)
        assertFalse(request.writingsOnly)
    }

    @Test
    fun headlinesLensSendsNoThemeAndIsNotWritingsOnly() {
        val request = DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", headlines = listOf("Floods")).toServiceRequest()

        assertNull(request.theme)
        assertFalse(request.writingsOnly)
        assertEquals(listOf("Floods"), request.headlines)
    }
}
