package com.mediasage.appserver.routes

import com.mediasage.appserver.repository.QuoteRepository
import com.mediasage.appserver.repository.QuotesResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.quoteRoutes() {
    val quoteRepository: QuoteRepository by inject()

    get("/api/quotes") {
        call.respond(HttpStatusCode.OK, QuotesResponse(quotes = quoteRepository.getLibrary()))
    }
}
