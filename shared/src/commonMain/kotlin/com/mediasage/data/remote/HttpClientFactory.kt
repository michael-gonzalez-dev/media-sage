package com.mediasage.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/** Creates a platform-specific HttpClient engine. */
expect fun createPlatformHttpClient(): HttpClient

/** Creates a fully configured HttpClient with JSON and timeout settings. */
fun createHttpClient(): HttpClient {
    return createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000  // Scraping + Claude can take 30-45s
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }

        // Throw ResponseException for non-2xx responses so callers receive a typed HTTP error
        // rather than a cryptic JSON deserialization failure from trying to parse an error body.
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) throw ResponseException(response, response.status.description)
            }
        }
    }
}
