package com.mediasage.agentruntime.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

internal const val BAD_REQUEST_MESSAGE = "Bad request"
internal const val INTERNAL_ERROR_MESSAGE = "Internal server error"
internal const val UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Unsupported media type"

/**
 * Maps exceptions to JSON error responses that never carry exception text: no messages, class names,
 * SQL or upstream provider bodies reach the caller. The full detail goes to the server log.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        // Ktor throws BadRequestException for an unparseable body; kotlinx-serialization errors are
        // IllegalArgumentExceptions.
        exception<BadRequestException> { call, cause -> call.respondBadRequest(cause) }
        exception<IllegalArgumentException> { call, cause -> call.respondBadRequest(cause) }
        // A body in a format no installed converter reads never reaches the exception handlers: Ktor answers it
        // directly with a 415 whose text names the internal target class. No route sends 415 on purpose.
        status(HttpStatusCode.UnsupportedMediaType) { call, status ->
            call.respond(status, ErrorResponse(status.value, UNSUPPORTED_MEDIA_TYPE_MESSAGE))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(500, INTERNAL_ERROR_MESSAGE))
        }
    }
}

private suspend fun ApplicationCall.respondBadRequest(cause: Throwable) {
    application.log.info("Rejected {} {}: {}", request.local.method.value, request.local.uri, cause.toString())
    respond(HttpStatusCode.BadRequest, ErrorResponse(400, BAD_REQUEST_MESSAGE))
}
