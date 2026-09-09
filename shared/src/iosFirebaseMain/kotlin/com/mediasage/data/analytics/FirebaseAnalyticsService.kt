package com.mediasage.data.analytics

import cocoapods.FirebaseAnalytics.FIRAnalytics
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class FirebaseAnalyticsService : AnalyticsService {

    override fun logEvent(name: String, params: Map<String, String>) {
        FIRAnalytics.logEventWithName(name, params as Map<Any?, *>)
    }

    override fun logScreenView(screenName: String) {
        FIRAnalytics.logEventWithName("screen_view", mapOf("screen_name" to screenName) as Map<Any?, *>)
    }
}

actual fun createAnalyticsService(): AnalyticsService = FirebaseAnalyticsService()
