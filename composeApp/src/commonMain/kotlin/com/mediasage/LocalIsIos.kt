package com.mediasage

import androidx.compose.runtime.compositionLocalOf

/** True when running on iOS — provided once from `MainViewController`, like [LocalIsDebugBuild]. */
val LocalIsIos = compositionLocalOf { false }
