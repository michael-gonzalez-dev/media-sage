package com.mediasage.appserver.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.hours

val ENCOURAGE_RATE_LIMIT = RateLimitName("encourage")
val DAILY_REFLECTION_RATE_LIMIT = RateLimitName("daily-reflection")

/** How many Claude-backed requests one caller may make per hour, per endpoint. */
data class CallerRateLimits(val encouragePerHour: Int, val dailyReflectionPerHour: Int)

/**
 * Limits each caller on the endpoints that make Claude calls, so one caller can't use up the
 * app-wide daily budget and lock every real user out. Over the limit, Ktor answers 429.
 */
fun Application.configureRateLimiting(limits: CallerRateLimits) {
    install(RateLimit) {
        register(ENCOURAGE_RATE_LIMIT) {
            rateLimiter(limit = limits.encouragePerHour, refillPeriod = 1.hours)
            requestKey { call -> call.callerKey() }
        }
        register(DAILY_REFLECTION_RATE_LIMIT) {
            rateLimiter(limit = limits.dailyReflectionPerHour, refillPeriod = 1.hours)
            requestKey { call -> call.callerKey() }
        }
    }
}

// Railway's edge sets X-Real-IP to the client's address. The connection's own remote host is the
// edge proxy in production, so it is only the fallback for local runs and tests.
private fun ApplicationCall.callerKey(): String =
    request.headers["X-Real-IP"]?.takeIf { it.isNotBlank() } ?: request.origin.remoteHost
