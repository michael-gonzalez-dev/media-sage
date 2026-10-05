package com.mediasage.appserver.plugins

import io.ktor.server.application.*
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
            requestWeight { _, key -> if (key == UNKNOWN_CALLER) 0 else 1 }
        }
        register(DAILY_REFLECTION_RATE_LIMIT) {
            rateLimiter(limit = limits.dailyReflectionPerHour, refillPeriod = 1.hours)
            requestKey { call -> call.callerKey() }
            requestWeight { _, key -> if (key == UNKNOWN_CALLER) 0 else 1 }
        }
    }
}

private const val UNKNOWN_CALLER = "unknown-caller"

// Railway's edge sets X-Real-IP to the client's address. Without it the only address left is the connection's,
// which in production is Railway's proxy, shared by every user, so keying on it would put the whole app in one
// bucket. Requests with no X-Real-IP therefore weigh nothing (no per-caller limit, fail open), and the app-wide
// daily budgets still cap the cost.
private fun ApplicationCall.callerKey(): String =
    request.headers["X-Real-IP"]?.takeIf { it.isNotBlank() } ?: UNKNOWN_CALLER
