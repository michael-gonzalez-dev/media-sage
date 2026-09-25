package com.mediasage.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.LocalAppVersion
import com.mediasage.LocalIsIos
import com.mediasage.theme.MediaSageTheme
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.about_app_name
import mediasage.composeapp.generated.resources.about_apple_eula
import mediasage.composeapp.generated.resources.about_copyright
import mediasage.composeapp.generated.resources.about_developed_by
import mediasage.composeapp.generated.resources.about_developer_credit
import mediasage.composeapp.generated.resources.about_privacy_policy
import mediasage.composeapp.generated.resources.about_section_legal
import mediasage.composeapp.generated.resources.about_section_support
import mediasage.composeapp.generated.resources.about_send_feedback
import mediasage.composeapp.generated.resources.about_terms_of_service
import mediasage.composeapp.generated.resources.about_website
import mediasage.composeapp.generated.resources.app_icon
import mediasage.composeapp.generated.resources.onos_monos_logo
import mediasage.composeapp.generated.resources.title_about
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val WEBSITE_URL = "https://thecouragepost.app"
private const val PRIVACY_POLICY_URL = "https://thecouragepost.app/privacy"
private const val TERMS_OF_SERVICE_URL = "https://thecouragepost.app/terms"
private const val FEEDBACK_MAILTO_URL = "mailto:support@thecouragepost.app?subject=The%20Courage%20Post%20Feedback"

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSection: (AboutSection) -> Unit = {},
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(title = stringResource(Res.string.title_about), onNavigateBack = onNavigateBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                AboutHeader()
                AboutSection.entries.forEach { section ->
                    SettingsNavRow(label = stringResource(section.title), onClick = { onNavigateToSection(section) })
                }
                Spacer(modifier = Modifier.height(24.dp))
                SupportSection()
                Spacer(modifier = Modifier.height(24.dp))
                LegalSection()
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AboutFooter()
            }
        }
    }
}

@Composable
private fun AboutHeader() {
    val appVersion = LocalAppVersion.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            // Decorative — the app name directly below announces what it is.
            contentDescription = null,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp)),
        )
        Text(
            text = stringResource(Res.string.about_app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (appVersion.isNotEmpty()) {
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SupportSection() {
    val uriHandler = LocalUriHandler.current
    SettingsSectionHeader(stringResource(Res.string.about_section_support))
    SettingsNavRow(
        label = stringResource(Res.string.about_send_feedback),
        onClick = { openUriSafely(uriHandler, FEEDBACK_MAILTO_URL) },
    )
    SettingsNavRow(
        label = stringResource(Res.string.about_website),
        onClick = { openUriSafely(uriHandler, WEBSITE_URL) },
    )
}

@Composable
private fun LegalSection() {
    val uriHandler = LocalUriHandler.current
    SettingsSectionHeader(stringResource(Res.string.about_section_legal))
    SettingsNavRow(
        label = stringResource(Res.string.about_terms_of_service),
        onClick = { openUriSafely(uriHandler, TERMS_OF_SERVICE_URL) },
    )
    SettingsNavRow(
        label = stringResource(Res.string.about_privacy_policy),
        onClick = { openUriSafely(uriHandler, PRIVACY_POLICY_URL) },
    )
    if (LocalIsIos.current) {
        Text(
            text = stringResource(Res.string.about_apple_eula),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun AboutFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DeveloperCredit()
        Text(
            text = stringResource(Res.string.about_copyright),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun DeveloperCredit() {
    // The logo is a single-color mark, tinted to match the credit text so the two always read as one unit.
    val creditColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.onos_monos_logo),
            // Decorative — the adjacent text already announces the studio name to screen readers.
            contentDescription = null,
            colorFilter = ColorFilter.tint(creditColor),
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = stringResource(Res.string.about_developed_by),
                style = MaterialTheme.typography.labelSmall,
                color = creditColor,
            )
            Text(
                text = stringResource(Res.string.about_developer_credit),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = creditColor,
            )
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    MediaSageTheme {
        CompositionLocalProvider(LocalAppVersion provides "v1.0 (build 130)") {
            AboutScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenDarkIosPreview() {
    MediaSageTheme(darkTheme = true) {
        CompositionLocalProvider(LocalAppVersion provides "v1.0 (build 130)", LocalIsIos provides true) {
            AboutScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenLargeTextPreview() {
    MediaSageTheme(textScalePercent = 150) {
        CompositionLocalProvider(LocalAppVersion provides "v1.0 (build 130)") {
            AboutScreen()
        }
    }
}

// endregion
