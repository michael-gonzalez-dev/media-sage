package com.mediasage.data.analytics

/**
 * Thin wrapper over the platform's Firebase Analytics instance. `logEvent` is for
 * business-logic byproducts the caller already owns (e.g. a ViewModel logging alongside a
 * state mutation it's already performing); `logScreenView` is for the UI-owned screen-view
 * signal that also backs Firebase's derived time-on-screen metric.
 */
interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun logScreenView(screenName: String)
}

expect fun createAnalyticsService(): AnalyticsService

/** Default for [com.mediasage.LocalAnalyticsService] — keeps `@Preview`s from needing a real instance. */
class NoOpAnalyticsService : AnalyticsService {
    override fun logEvent(name: String, params: Map<String, String>) = Unit
    override fun logScreenView(screenName: String) = Unit
}
