package com.mediasage.appserver

import com.mediasage.appserver.prompts.ReflectionTheme
import com.mediasage.appserver.routes.DailyReflectionRequest
import com.mediasage.appserver.routes.MAX_HEADLINE_TITLE_LENGTH
import com.mediasage.appserver.routes.MAX_PREVIOUS_REFLECTIONS
import com.mediasage.appserver.routes.MAX_PREVIOUS_REFLECTION_LENGTH
import com.mediasage.appserver.routes.MAX_REFLECTION_HEADLINES
import com.mediasage.appserver.routes.bounded
import com.mediasage.appserver.routes.toServiceRequest
import com.mediasage.appserver.routes.validationError
import io.ktor.client.request.header
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

    private suspend fun ApplicationTestBuilder.postReflection(body: String, callerIp: String? = null) =
        client.post("/api/analysis/daily-reflection") {
            contentType(ContentType.Application.Json)
            callerIp?.let { header("X-Real-IP", it) }
            setBody(body)
        }

    // The biggest request the real app can send: every stored headline, and a full week of briefings
    // across both times of day and all 10 lenses. Trimming must leave it untouched.
    @Test
    fun theLargestRequestTheAppSendsIsPassedThroughUntrimmed() {
        val request = DailyReflectionRequest(
            figureId = 19,
            figureName = "A.W. Tozer",
            headlines = List(100) { "A typical news headline of about this length, number $it" },
            previousScriptures = List(140) { "Psalm $it:1" },
            previousReflections = List(140) { "Monday morning, Hope lens (drew on The Pursuit of God): " + "word ".repeat(150) }
        )

        assertNull(request.validationError())
        assertEquals(request, request.bounded())
    }

    @Test
    fun oversizedRequestsAreTrimmedNotRejected() {
        val request = DailyReflectionRequest(
            figureId = 19,
            figureName = "A.W. Tozer",
            headlines = List(MAX_REFLECTION_HEADLINES + 50) { "h".repeat(MAX_HEADLINE_TITLE_LENGTH + 10) },
            previousReflections = List(MAX_PREVIOUS_REFLECTIONS + 50) { "r$it " + "x".repeat(MAX_PREVIOUS_REFLECTION_LENGTH) }
        )

        val bounded = request.bounded()

        assertNull(request.validationError())
        assertEquals(MAX_REFLECTION_HEADLINES, bounded.headlines.size)
        assertTrue(bounded.headlines.all { it.length == MAX_HEADLINE_TITLE_LENGTH })
        assertEquals(MAX_PREVIOUS_REFLECTIONS, bounded.previousReflections.size)
        assertTrue(bounded.previousReflections.all { it.length == MAX_PREVIOUS_REFLECTION_LENGTH })
        // The app sends history oldest first, so the most recent entries are the ones kept.
        assertTrue(bounded.previousReflections.last().startsWith("r${MAX_PREVIOUS_REFLECTIONS + 49} "))
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

        val first = postReflection("""{"figureId":0,"figureName":"C.S. Lewis"}""", callerIp = "203.0.113.7")
        val second = postReflection("""{"figureId":0,"figureName":"C.S. Lewis"}""", callerIp = "203.0.113.7")
        val otherCaller = postReflection("""{"figureId":0,"figureName":"C.S. Lewis"}""", callerIp = "198.51.100.4")

        assertEquals(HttpStatusCode.BadRequest, first.status)
        assertEquals(HttpStatusCode.TooManyRequests, second.status)
        assertEquals(HttpStatusCode.BadRequest, otherCaller.status)
    }

    @Test
    fun requestsWithoutACallerAddressAreNotLimitedPerCaller() = testApplication {
        startServer("app.claude.reflectionPerCallerPerHour" to "1")

        repeat(3) {
            assertEquals(HttpStatusCode.BadRequest, postReflection("""{"figureId":0,"figureName":"C.S. Lewis"}""").status)
        }
    }

    // The real route, wired as in production: a body that can't be parsed is a 400 with a generic message,
    // not a 500 that echoes the JSON parser's error.
    @Test
    fun malformedBodyIsAGeneric400() = testApplication {
        startServer()

        val response = postReflection("""{"figureId":"not-a-number","figureName":"C.S. Lewis"}""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"Bad request\""), body)
        assertFalse(body.contains("not-a-number"), body)
    }
}
