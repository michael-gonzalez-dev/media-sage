package com.mediasage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.uikit.LocalUIViewController
import com.mediasage.theme.MediaSageTheme
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController

@Composable
actual fun rememberInAppBrowserUriHandler(): UriHandler {
    val viewController = LocalUIViewController.current
    val isDark = MediaSageTheme.isDark
    return remember(viewController, isDark) { SafariUriHandler(viewController, isDark) }
}

private class SafariUriHandler(
    private val presenter: UIViewController,
    private val isDark: Boolean,
) : UriHandler {
    override fun openUri(uri: String) {
        // SFSafariViewController raises an uncatchable NSException for anything but http(s).
        val url = NSURL.URLWithString(uri)?.takeIf { it.scheme == "http" || it.scheme == "https" } ?: return
        val safari = SFSafariViewController(uRL = url)
        safari.overrideUserInterfaceStyle = if (isDark) {
            UIUserInterfaceStyle.UIUserInterfaceStyleDark
        } else {
            UIUserInterfaceStyle.UIUserInterfaceStyleLight
        }
        presenter.presentViewController(safari, animated = true, completion = null)
    }
}
