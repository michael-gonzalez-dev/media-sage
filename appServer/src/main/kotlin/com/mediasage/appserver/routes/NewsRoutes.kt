package com.mediasage.appserver.routes

import com.mediasage.appserver.repository.HeadlineRepository
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

internal const val DEFAULT_HEADLINES_LIMIT = 10

// The app syncs up to 100 headlines per request (FETCH_LIMIT in the shared HeadlineRepositoryImpl), so the cap
// matches it: the app's feed is never cut short, but no single request can ask for more.
internal const val MAX_HEADLINES_LIMIT = 100

fun Route.newsRoutes() {
    val headlineRepository by inject<HeadlineRepository>()

    route("/api/news") {
        get("/headlines") {
            val category = call.parameters["category"]
            val limit = headlinesLimit(call.parameters["limit"])

            // Served from the twice-daily cache populated by HeadlineFetchService — no live
            // provider call here, so read volume never increases the number of GNews requests.
            val articles = headlineRepository.getStored(category = category, limit = limit)

            call.respond(articles)
        }
    }
}

// A missing, non-numeric, zero or negative limit falls back to the default. A negative LIMIT must never reach the
// query: SQLite reads it as "no limit" (every stored headline) and Postgres rejects it with an error.
internal fun headlinesLimit(raw: String?): Int =
    raw?.toIntOrNull()?.takeIf { it > 0 }?.coerceAtMost(MAX_HEADLINES_LIMIT) ?: DEFAULT_HEADLINES_LIMIT
