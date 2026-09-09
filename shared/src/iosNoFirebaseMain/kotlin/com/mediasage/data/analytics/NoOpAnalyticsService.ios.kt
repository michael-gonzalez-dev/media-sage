package com.mediasage.data.analytics

/**
 * Used only when GoogleService-Info.plist is absent (CI's `:shared:build`, and any local iOS
 * build before a developer has fetched the real Firebase config) — the CocoaPods block in
 * shared/build.gradle.kts that would normally provide the real Firebase-backed actuals is skipped
 * in that case, so this keeps iOS compilation green without the pods present (MS-683).
 */
actual fun createAnalyticsService(): AnalyticsService = NoOpAnalyticsService()

actual fun triggerTestCrash(): Nothing =
    throw RuntimeException("MS-683 test crash — iOS (Firebase not configured for this build)")
