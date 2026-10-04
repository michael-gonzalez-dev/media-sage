package com.mediasage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mediasage.theme.MediaSageTheme

/**
 * A single-line search field with a clear button. The field keeps its own text and passes each change on, so typing
 * never waits for the ViewModel's state to come back. In the Reporters deck the field sits inside a layout that
 * composes a frame late, and a field fed from that state lost keystrokes typed quickly.
 */
@Composable
fun SearchField(query: String, label: String, onQueryChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    val textState = rememberTextFieldState(initialText = query)
    val latestOnQueryChanged by rememberUpdatedState(onQueryChanged)
    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }.collect { latestOnQueryChanged(it) }
    }
    OutlinedTextField(
        state = textState,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        label = { Text(label) },
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = MaterialTheme.shapes.medium,
        trailingIcon = {
            if (textState.text.isNotBlank()) {
                IconButton(onClick = { textState.clearText() }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                }
            }
        }
    )
}

// region Previews

@Preview
@Composable
private fun SearchFieldPreview() {
    MediaSageTheme { SearchField(query = "Tozer", label = "Search reporters…", onQueryChanged = {}) }
}

// endregion
