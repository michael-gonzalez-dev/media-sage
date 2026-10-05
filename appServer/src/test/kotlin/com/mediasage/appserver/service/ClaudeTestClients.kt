package com.mediasage.appserver.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * A [ClaudeApiClient] whose every call answers with [status] and, when OK, a message whose text is [reply] given the
 * request's user message.
 */
internal fun claudeApiClientReplying(
    status: HttpStatusCode = HttpStatusCode.OK,
    reply: (userMessage: String) -> String = { "" }
): ClaudeApiClient = ClaudeApiClient(
    HttpClient(
        MockEngine { request ->
            val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
            val userMessage = body.getValue("messages").jsonArray[0].jsonObject.getValue("content").jsonPrimitive.content
            val content = if (status == HttpStatusCode.OK) claudeMessage(reply(userMessage)) else """{"error":"failed"}"""
            respond(content, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    ) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    },
    "test-key"
)

private fun claudeMessage(text: String): String = buildJsonObject {
    put("id", "msg_1")
    put("type", "message")
    put("role", "assistant")
    put("model", "test")
    put("content", JsonArray(listOf(buildJsonObject { put("type", "text"); put("text", text) })))
    put("usage", buildJsonObject { put("input_tokens", 1); put("output_tokens", 1) })
}.toString()
