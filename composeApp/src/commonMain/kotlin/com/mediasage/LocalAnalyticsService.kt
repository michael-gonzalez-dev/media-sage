package com.mediasage

import androidx.compose.runtime.compositionLocalOf
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.analytics.NoOpAnalyticsService

val LocalAnalyticsService = compositionLocalOf<AnalyticsService> { NoOpAnalyticsService() }
