package com.mediasage.appserver

import com.mediasage.appserver.plugins.BAD_REQUEST_MESSAGE
import com.mediasage.appserver.plugins.DAILY_LIMIT_MESSAGE
import com.mediasage.appserver.plugins.INTERNAL_ERROR_MESSAGE
import com.mediasage.appserver.plugins.UNSUPPORTED_MEDIA_TYPE_MESSAGE
import com.mediasage.appserver.plugins.UPSTREAM_ERROR_MESSAGE
import com.mediasage.appserver.plugins.configureContentNegotiation
import com.mediasage.appserver.plugins.configureStatusPages
import com.mediasage.appserver.service.ClaudeApiException
import com.mediasage.appserver.service.DailyLimitExceededException
import com.mediasage.appserver.service.NewsApiException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private const val SECRET = "org.postgresql.util.PSQLException: ERROR: LIMIT must not be negative at /app/db"

@Serializable
private data class SampleBody(val name: String)

class StatusPagesTest {

    private fun ApplicationTestBuilder.serverThrowing() {
        application {
            configureContentNegotiation()
            configureStatusPages()
        }
        routing {
            failures.forEach { (path, failure) -> get(path) { throw failure() } }
            post("/body") { call.respond(call.receive<SampleBody>()) }
        }
    }

    // Every exception carries the kind of text that must never reach a caller.
    private val failures: Map<String, () -> Throwable> = mapOf(
        "/unexpected" to { IllegalStateException(SECRET) },
        "/claude" to { ClaudeApiException(401, "Claude API error (401): {\"error\":\"invalid x-api-key\"} $SECRET") },
        "/news" to { NewsApiException(429, "News API error (429): quota exceeded $SECRET") },
        "/illegal-argument" to { IllegalArgumentException(SECRET) },
        "/daily-limit" to { DailyLimitExceededException(SECRET) }
    )

    private suspend fun HttpResponse.assertGeneric(status: HttpStatusCode, message: String) {
        val body = bodyAsText()
        assertEquals(status, this.status)
        val json = Json.parseToJsonElement(body).jsonObject
        assertEquals(status.value, json.getValue("status").jsonPrimitive.int)
        assertEquals(message, json.getValue("message").jsonPrimitive.content)
        assertFalse(body.contains("PSQLException"), body)
        assertFalse(body.contains("x-api-key"), body)
        assertFalse(body.contains("quota"), body)
    }

    @Test
    fun unexpectedErrorReturnsGeneric500() = testApplication {
        serverThrowing()
        client.get("/unexpected").assertGeneric(HttpStatusCode.InternalServerError, INTERNAL_ERROR_MESSAGE)
    }

    // A provider failure is reported as our gateway failing, never with the provider's own status:
    // a Claude 401 caused by our key must not tell the caller they are unauthorized.
    @Test
    fun upstreamApiFailuresReturnGeneric502() = testApplication {
        serverThrowing()
        client.get("/claude").assertGeneric(HttpStatusCode.BadGateway, UPSTREAM_ERROR_MESSAGE)
        client.get("/news").assertGeneric(HttpStatusCode.BadGateway, UPSTREAM_ERROR_MESSAGE)
    }

    @Test
    fun illegalArgumentReturnsGeneric400() = testApplication {
        serverThrowing()
        client.get("/illegal-argument").assertGeneric(HttpStatusCode.BadRequest, BAD_REQUEST_MESSAGE)
    }

    @Test
    fun dailyLimitReturnsItsFixedMessage() = testApplication {
        serverThrowing()
        client.get("/daily-limit").assertGeneric(HttpStatusCode.TooManyRequests, DAILY_LIMIT_MESSAGE)
    }

    @Test
    fun malformedBodyReturnsGeneric400Not500() = testApplication {
        serverThrowing()
        listOf("{not json", """{"name": 42, "extra": "${SECRET}"}""", "{}").forEach { body ->
            client.post("/body") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.assertGeneric(HttpStatusCode.BadRequest, BAD_REQUEST_MESSAGE)
        }
    }

    @Test
    fun bodyWithUnsupportedContentTypeReturnsGeneric415() = testApplication {
        serverThrowing()
        client.post("/body") {
            contentType(ContentType.Text.Plain)
            setBody("name=x")
        }.assertGeneric(HttpStatusCode.UnsupportedMediaType, UNSUPPORTED_MEDIA_TYPE_MESSAGE)
    }
}
