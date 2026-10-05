package com.mediasage.agentruntime

import com.mediasage.agentruntime.plugins.BAD_REQUEST_MESSAGE
import com.mediasage.agentruntime.plugins.INTERNAL_ERROR_MESSAGE
import com.mediasage.agentruntime.plugins.UNSUPPORTED_MEDIA_TYPE_MESSAGE
import com.mediasage.agentruntime.plugins.configureContentNegotiation
import com.mediasage.agentruntime.plugins.configureStatusPages
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

private const val SECRET = "org.postgresql.util.PSQLException: relation \"jobs\" does not exist"

@Serializable
private data class SampleBody(val name: String)

class StatusPagesTest {

    private fun ApplicationTestBuilder.serverThrowing() {
        application {
            configureContentNegotiation()
            configureStatusPages()
        }
        routing {
            get("/unexpected") { throw IllegalStateException(SECRET) }
            get("/illegal-argument") { throw IllegalArgumentException(SECRET) }
            post("/body") { call.respond(call.receive<SampleBody>()) }
        }
    }

    private suspend fun HttpResponse.assertGeneric(status: HttpStatusCode, message: String) {
        val body = bodyAsText()
        assertEquals(status, this.status)
        val json = Json.parseToJsonElement(body).jsonObject
        assertEquals(status.value, json.getValue("status").jsonPrimitive.int)
        assertEquals(message, json.getValue("message").jsonPrimitive.content)
        assertFalse(body.contains("PSQLException"), body)
    }

    @Test
    fun unexpectedErrorReturnsGeneric500() = testApplication {
        serverThrowing()
        client.get("/unexpected").assertGeneric(HttpStatusCode.InternalServerError, INTERNAL_ERROR_MESSAGE)
    }

    @Test
    fun illegalArgumentReturnsGeneric400() = testApplication {
        serverThrowing()
        client.get("/illegal-argument").assertGeneric(HttpStatusCode.BadRequest, BAD_REQUEST_MESSAGE)
    }

    @Test
    fun malformedBodyReturnsGeneric400Not500() = testApplication {
        serverThrowing()
        client.post("/body") {
            contentType(ContentType.Application.Json)
            setBody("{not json")
        }.assertGeneric(HttpStatusCode.BadRequest, BAD_REQUEST_MESSAGE)
    }

    @Test
    fun bodyInAnUnreadableFormatReturnsGeneric415() = testApplication {
        serverThrowing()
        client.post("/body") {
            contentType(ContentType.Text.Plain)
            setBody("name=x")
        }.assertGeneric(HttpStatusCode.UnsupportedMediaType, UNSUPPORTED_MEDIA_TYPE_MESSAGE)
    }
}
