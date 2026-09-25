package com.mediasage.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.theme.MediaSageTheme
import org.jetbrains.compose.resources.stringResource

/**
 * One long-form About page. [section] is the route argument — static content, so it stands in for
 * the `state` a ViewModel-backed screen would receive.
 */
@Composable
fun AboutDetailScreen(
    section: AboutSection,
    onNavigateBack: () -> Unit = {},
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(title = stringResource(section.title), onNavigateBack = onNavigateBack)
            Text(
                text = stringResource(section.body),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            )
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun AboutDetailScreenPreview() {
    MediaSageTheme {
        AboutDetailScreen(section = AboutSection.WHY)
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutDetailScreenDarkPreview() {
    MediaSageTheme(darkTheme = true) {
        AboutDetailScreen(section = AboutSection.DISCLAIMER)
    }
}

// endregion
