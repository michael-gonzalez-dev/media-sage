package com.mediasage.appserver.plugins

import com.mediasage.appserver.service.ClaudeApiException
import com.mediasage.appserver.service.DailyLimitExceededException
import com.mediasage.appserver.service.NewsApiException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

internal const val BAD_REQUEST_MESSAGE = "Bad request"
internal const val NOT_FOUND_MESSAGE = "Not found"
internal const val UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Unsupported media type"
internal const val PAYLOAD_TOO_LARGE_MESSAGE = "Request too large"
internal const val DAILY_LIMIT_MESSAGE = "Daily limit reached. Try again tomorrow."
internal const val UPSTREAM_ERROR_MESSAGE = "A service this request depends on is unavailable. Try again later."
internal const val INTERNAL_ERROR_MESSAGE = "Internal server error"

/**
 * Maps exceptions to JSON error responses that never carry exception text: no messages, class names,
 * SQL, file paths or upstream provider bodies reach the caller. The full detail goes to the server log.
 *
 * Messages a route writes for callers on purpose (e.g. "headlineTitle is required") are responded
 * directly by the route and don't pass through here.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        clientErrors()
        exception<ClaudeApiException> { call, cause -> call.respondUpstreamError("Claude", cause.statusCode, cause) }
        exception<NewsApiException> { call, cause -> call.respondUpstreamError("GNews", cause.statusCode, cause) }
        exception<DailyLimitExceededException> { call, _ ->
            call.respond(HttpStatusCode.TooManyRequests, ErrorResponse(429, DAILY_LIMIT_MESSAGE))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(500, INTERNAL_ERROR_MESSAGE))
        }
    }
}

// Malformed or unacceptable requests. Ktor throws BadRequestException when a body can't be parsed into the
// expected type, and kotlinx-serialization errors are IllegalArgumentExceptions.
private fun StatusPagesConfig.clientErrors() {
    exception<BadRequestException> { call, cause -> call.respondClientError(HttpStatusCode.BadRequest, BAD_REQUEST_MESSAGE, cause) }
    exception<IllegalArgumentException> { call, cause ->
        call.respondClientError(HttpStatusCode.BadRequest, BAD_REQUEST_MESSAGE, cause)
    }
    exception<NotFoundException> { call, cause -> call.respondClientError(HttpStatusCode.NotFound, NOT_FOUND_MESSAGE, cause) }
    exception<UnsupportedMediaTypeException> { call, cause ->
        call.respondClientError(HttpStatusCode.UnsupportedMediaType, UNSUPPORTED_MEDIA_TYPE_MESSAGE, cause)
    }
    // A body in a format no installed converter reads (e.g. text/plain where JSON is expected) never reaches the
    // exception handlers: Ktor answers it directly with a 415 whose text names the internal class it tried to
    // build. No route sends 415 on purpose, so every 415 response is replaced with the generic one.
    status(HttpStatusCode.UnsupportedMediaType) { call, status ->
        call.respond(status, ErrorResponse(status.value, UNSUPPORTED_MEDIA_TYPE_MESSAGE))
    }
    exception<PayloadTooLargeException> { call, cause ->
        call.respondClientError(HttpStatusCode.PayloadTooLarge, PAYLOAD_TOO_LARGE_MESSAGE, cause)
    }
}

private suspend fun ApplicationCall.respondClientError(status: HttpStatusCode, message: String, cause: Throwable) {
    application.log.info("Rejected {} {}: {}", request.local.method.value, request.local.uri, cause.toString())
    respond(status, ErrorResponse(status.value, message))
}

// The provider's own status is not passed through: a Claude 401 caused by our key is not the caller's fault.
private suspend fun ApplicationCall.respondUpstreamError(provider: String, upstreamStatus: Int, cause: Throwable) {
    application.log.warn("{} API error (status {}): {}", provider, upstreamStatus, cause.message)
    respond(HttpStatusCode.BadGateway, ErrorResponse(HttpStatusCode.BadGateway.value, UPSTREAM_ERROR_MESSAGE))
}
