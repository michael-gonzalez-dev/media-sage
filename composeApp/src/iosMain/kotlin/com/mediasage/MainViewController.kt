package com.mediasage

import androidx.compose.ui.window.ComposeUIViewController
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.di.appModule
import com.mediasage.di.databaseModule
import com.mediasage.di.headlinesModule
import com.mediasage.di.notificationModule
import com.mediasage.di.sharedModule
import com.mediasage.di.themeModule
import com.mediasage.di.userModule
import org.koin.core.context.startKoin

/** [analyticsService] is implemented in Swift on top of the Firebase iOS SDK (see `FirebaseAnalyticsService.swift`). */
fun initKoin(supabaseUrl: String, supabaseAnonKey: String, analyticsService: AnalyticsService) {
    startKoin {
        modules(
            databaseModule,
            themeModule,
            userModule,
            headlinesModule,
            notificationModule,
            sharedModule(
                serverBaseUrl = "https://media-sage-production.up.railway.app",
                supabaseUrl = supabaseUrl,
                supabaseAnonKey = supabaseAnonKey,
                analyticsServiceFactory = { analyticsService },
            ),
            appModule,
        )
    }
}

fun MainViewController(isDebugBuild: Boolean = false, appVersion: String = "") =
    ComposeUIViewController { App(isDebugBuild = isDebugBuild, appVersion = appVersion, isIos = true) }