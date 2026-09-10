package com.mediasage.data.analytics

import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsService : AnalyticsService {

    private val firebaseAnalytics: FirebaseAnalytics =
        FirebaseAnalytics.getInstance(FirebaseApp.getInstance().applicationContext)

    override fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) -> putString(key, value) }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}

actual fun createAnalyticsService(): AnalyticsService = FirebaseAnalyticsService()
