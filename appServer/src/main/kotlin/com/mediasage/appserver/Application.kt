package com.mediasage.appserver

import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.di.serverModule
import com.mediasage.appserver.plugins.*
import com.mediasage.appserver.routes.*
import com.mediasage.appserver.service.HeadlineFetchService
import com.mediasage.appserver.service.launchHeadlineFetchLoop
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

private const val DEFAULT_DAILY_CLAUDE_CALL_LIMIT = 300
private const val DEFAULT_DAILY_REFLECTION_LIMIT = 2000
private const val DEFAULT_ENCOURAGE_PER_CALLER_PER_HOUR = 60
private const val DEFAULT_REFLECTION_PER_CALLER_PER_HOUR = 20

fun main(args: Array<String>) {
    EngineMain.main(args)
}

/** Application module referenced in application.conf. Installs plugins and routes. */
fun Application.module() {
    val claudeApiKey = environment.config.propertyOrNull("app.claude.apiKey")?.getString() ?: ""
    val newsApiKey = environment.config.propertyOrNull("app.news.apiKey")?.getString() ?: ""
    val baseUrl = environment.config.propertyOrNull("app.baseUrl")?.getString() ?: "http://localhost:8080"
    val dailyClaudeCallLimit = intConfig("app.claude.dailyCallLimit", DEFAULT_DAILY_CLAUDE_CALL_LIMIT)
    val dailyReflectionLimit = intConfig("app.claude.dailyReflectionLimit", DEFAULT_DAILY_REFLECTION_LIMIT)

    install(Koin) {
        modules(serverModule(claudeApiKey, newsApiKey, baseUrl, dailyClaudeCallLimit, dailyReflectionLimit))
    }

    configureContentNegotiation()
    configureCORS()
    configureCallLogging()
    configureStatusPages()
    configureRateLimiting(
        CallerRateLimits(
            encouragePerHour = intConfig("app.claude.encouragePerCallerPerHour", DEFAULT_ENCOURAGE_PER_CALLER_PER_HOUR),
            dailyReflectionPerHour = intConfig("app.claude.reflectionPerCallerPerHour", DEFAULT_REFLECTION_PER_CALLER_PER_HOUR)
        )
    )
    configureRouting()

    initDatabase()
    startHeadlineFetchScheduler()
}

private fun Application.intConfig(path: String, default: Int): Int =
    environment.config.propertyOrNull(path)?.getString()?.toIntOrNull() ?: default

private fun Application.startHeadlineFetchScheduler() {
    val headlineFetchService by inject<HeadlineFetchService>()
    CoroutineScope(Dispatchers.IO).launchHeadlineFetchLoop(headlineFetchService)
}

private fun Application.initDatabase() {
    val postgresUrl = environment.config.propertyOrNull("app.supabase.dbUrl")?.getString()
    if (postgresUrl != null) {
        ServerDatabase.init(postgresUrl = postgresUrl)
        return
    }
    val dbPath = environment.config.propertyOrNull("app.db.path")?.getString()
        ?: error("DB_PATH is not set. Export an absolute path: export DB_PATH=<path>")
    ServerDatabase.init(dbPath = dbPath)
}

fun Application.configureRouting() {
    routing {
        healthRoutes()
        newsRoutes()
        analysisRoutes()
        dailyReflectionRoutes()
        figureRoutes()
        workRoutes()
        quoteRoutes()
        assignmentDefaultsRoutes()
    }
}
