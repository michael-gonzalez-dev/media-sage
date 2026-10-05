package com.mediasage.appserver

import com.mediasage.appserver.prompts.ReflectionTheme
import com.mediasage.appserver.routes.DailyReflectionRequest
import com.mediasage.appserver.routes.MAX_PREVIOUS_REFLECTION_LENGTH
import com.mediasage.appserver.routes.MAX_REFLECTION_HEADLINES
import com.mediasage.appserver.routes.toServiceRequest
import com.mediasage.appserver.routes.validationError
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
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

    @Test
    fun keepsTheTimeOfDayANewerAppSendsWithoutChangingTheTone() {
        val request = DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", tone = "morning", timeOfDay = "afternoon")
            .toServiceRequest()

        assertEquals("morning", request.tone)
        assertEquals("afternoon", request.timeOfDay)
    }

    @Test
    fun anOlderAppWithNoTimeOfDayIsWordedByItsTone() {
        val morning = DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", tone = "morning").toServiceRequest()
        val evening = DailyReflectionRequest(figureId = 19, figureName = "A.W. Tozer", tone = "evening").toServiceRequest()

        assertEquals("morning", morning.timeOfDay)
        assertEquals("evening", evening.timeOfDay)
    }

    private fun ApplicationTestBuilder.startServer(vararg extraConfig: Pair<String, String>) {
        environment { config = MapApplicationConfig("app.db.path" to ":memory:", *extraConfig) }
        application { module() }
    }

    private suspend fun ApplicationTestBuilder.postReflection(body: String) = client.post("/api/analysis/daily-reflection") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    // The biggest request the real app can send: every stored headline, and a full week of briefings
    // across both times of day and all 10 lenses. It must never be rejected.
    @Test
    fun theLargestRequestTheAppSendsIsAccepted() {
        val request = DailyReflectionRequest(
            figureId = 19,
            figureName = "A.W. Tozer",
            headlines = List(100) { "A typical news headline of about this length, number $it" },
            previousScriptures = List(140) { "Psalm $it:1" },
            previousReflections = List(140) { "Monday morning, Hope lens (drew on The Pursuit of God): " + "word ".repeat(150) }
        )

        assertNull(request.validationError())
    }

    @Test
    fun oversizedRequestsAreRejectedBeforeAnyClaudeCall() = testApplication {
        startServer()
        val tooManyHeadlines = List(MAX_REFLECTION_HEADLINES + 1) { "\"h$it\"" }.joinToString(",")
        val tooLongReflection = "x".repeat(MAX_PREVIOUS_REFLECTION_LENGTH + 1)

        listOf(
            """{"figureId":1,"figureName":"C.S. Lewis","headlines":[$tooManyHeadlines]}""",
            """{"figureId":1,"figureName":"C.S. Lewis","previousReflections":["$tooLongReflection"]}""",
            """{"figureId":1,"figureName":"${"x".repeat(101)}"}"""
        ).forEach { body ->
            assertEquals(HttpStatusCode.BadRequest, postReflection(body).status)
        }
    }

    @Test
    fun refusesOnceTheDailyBudgetIsUsedUp() = testApplication {
        startServer("app.claude.dailyReflectionLimit" to "0")

        val response = postReflection("""{"figureId":1,"figureName":"C.S. Lewis","tone":"morning"}""")

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
    }

    @Test
    fun oneCallerIsRateLimited() = testApplication {
        startServer("app.claude.reflectionPerCallerPerHour" to "1")

        val first = postReflection("""{"figureId":0,"figureName":"C.S. Lewis"}""")
        val second = postReflection("""{"figureId":0,"figureName":"C.S. Lewis"}""")

        assertEquals(HttpStatusCode.BadRequest, first.status)
        assertEquals(HttpStatusCode.TooManyRequests, second.status)
    }
}
