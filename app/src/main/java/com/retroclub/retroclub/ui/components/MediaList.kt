package com.retroclub.retroclub.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.retroclub.retroclub.domain.model.MediaItem
import com.retroclub.retroclub.ui.theme.ThemeManager

@Composable
fun MediaList(
    mediaItems: List<MediaItem>,
    themeManager: ThemeManager,
    version: Int,
    onItemClick: (MediaItem) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val backgroundColor = if (themeManager.isLightTheme) {
        Color(android.graphics.Color.WHITE)
    } else {
        Color(android.graphics.Color.BLACK)
    }
    val textColor = if (themeManager.isLightTheme) {
        Color.Black
    } else {
        Color.White
    }

    if (isLandscape) {
        // In landscape: Use regular Column (no internal scrolling)
        // The NestedScrollView will handle scrolling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .padding(8.dp)
        ) {
            mediaItems.forEach { mediaItem ->
                MediaItemRow(
                    mediaItem = mediaItem,
                    textColor = textColor,
                    onClick = { onItemClick(mediaItem) }
                )
            }
        }
    } else {
        // In portrait: Also use Column and let NestedScrollView handle scrolling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .padding(8.dp)
        ) {
            mediaItems.forEach { mediaItem ->
                MediaItemRow(
                    mediaItem = mediaItem,
                    textColor = textColor,
                    onClick = { onItemClick(mediaItem) }
                )
            }
        }
    }
}