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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                section.blocks.forEachIndexed { index, block ->
                    AboutBlockContent(block = block, isFirst = index == 0)
                }
            }
        }
    }
}

@Composable
private fun AboutBlockContent(block: AboutBlock, isFirst: Boolean) {
    when (block) {
        is AboutBlock.Heading -> Text(
            text = stringResource(block.text),
            // titleLarge, not titleMedium: this theme's bodyLarge (17sp) is larger than titleMedium (16sp),
            // so titleMedium would render section headings smaller than the text beneath them.
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = if (isFirst) 0.dp else 24.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        is AboutBlock.Paragraph -> Text(
            text = stringResource(block.text),
            style = MaterialTheme.typography.bodyLarge,
        )
        is AboutBlock.LabeledItem -> {
            val label = stringResource(block.label)
            val text = stringResource(block.text)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(label) }
                    append(" ")
                    append(text)
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 12.dp),
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
private fun AboutDetailScreenDisclaimerPreview() {
    MediaSageTheme {
        AboutDetailScreen(section = AboutSection.DISCLAIMER)
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutDetailScreenDarkLargeTextPreview() {
    MediaSageTheme(darkTheme = true, textScalePercent = 150) {
        AboutDetailScreen(section = AboutSection.DISCLAIMER)
    }
}

// endregion
