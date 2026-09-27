package com.mediasage.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler

/**
 * A [UriHandler] that shows `http`/`https` pages in the platform's in-app browser — Chrome Custom Tabs on
 * Android, `SFSafariViewController` on iOS — styled to the app's current light/dark mode, so the user never
 * leaves the app. Web links only: keep non-web schemes like `mailto:` on `LocalUriHandler`. If no browser can
 * show the page, [UriHandler.openUri] does nothing.
 */
@Composable
expect fun rememberInAppBrowserUriHandler(): UriHandler
