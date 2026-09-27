package com.mediasage.ui

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import com.mediasage.theme.MediaSageTheme

@Composable
actual fun rememberInAppBrowserUriHandler(): UriHandler {
    val context = LocalContext.current
    val isDark = MediaSageTheme.isDark
    // Match the app's surface (what the About top bar sits on) instead of Chrome's default tinted toolbar.
    val barColor = MaterialTheme.colorScheme.surface.toArgb()
    return remember(context, isDark, barColor) { CustomTabsUriHandler(context, isDark, barColor) }
}

private class CustomTabsUriHandler(
    private val context: Context,
    private val isDark: Boolean,
    private val barColor: Int,
) : UriHandler {
    override fun openUri(uri: String) {
        val colorScheme = if (isDark) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT
        val barColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(barColor)
            .setNavigationBarColor(barColor)
            .build()
        val intent = CustomTabsIntent.Builder()
            .setColorScheme(colorScheme)
            .setDefaultColorSchemeParams(barColors)
            .build()
        try {
            intent.launchUrl(context, uri.toUri())
        } catch (e: ActivityNotFoundException) {
            // No browser installed to show the page — nothing to do.
        }
    }
}
