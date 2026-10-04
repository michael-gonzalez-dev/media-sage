package com.mediasage.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasage.theme.MediaSageTheme

/** A reporter's portrait cropped to a circle, so they can be recognized by face; their initials when there's no portrait. */
@Composable
fun FigureAvatar(name: String, portraitUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    val avatarModifier = modifier.size(size).clip(CircleShape)
    if (portraitUrl != null) {
        AsyncImage(
            model = portraitUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = avatarModifier,
        )
    } else {
        Box(modifier = avatarModifier, contentAlignment = Alignment.Center) {
            FigurePlaceholder(name = name, size = size)
        }
    }
}

// region Previews

@Preview
@Composable
private fun FigureAvatarPreview() {
    MediaSageTheme { FigureAvatar(name = "A.W. Tozer", portraitUrl = null, size = 36.dp) }
}

// endregion
