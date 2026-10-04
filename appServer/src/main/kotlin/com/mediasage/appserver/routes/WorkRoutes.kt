package com.mediasage.appserver.routes

import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.repository.WorksResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.workRoutes() {
    val workRepository: WorkRepository by inject()

    // Always the full list: the bibliography is a few hundred rows, so the app replaces its copy on every sync.
    get("/api/works") {
        call.respond(HttpStatusCode.OK, WorksResponse(works = workRepository.getAllForEnabledFigures()))
    }
}
