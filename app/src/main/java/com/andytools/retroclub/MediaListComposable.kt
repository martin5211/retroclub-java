package com.andytools.retroclub

import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.andytools.retroclub.ui.theme.ThemeManager

// Data class for media items
/*data class MediaItem(val title: String, val thumbnailUrl: String)

// Composable for the media list
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
        ComposeColor(Color.WHITE)
    } else {
        ComposeColor(Color.BLACK)
    }
    val textColor = if (themeManager.isLightTheme) {
        ComposeColor.Black
    } else {
        ComposeColor.White
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

// Composable for a single media item row
@Composable
fun MediaItemRow(
    mediaItem: MediaItem,
    textColor: ComposeColor,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Image(
            painter = rememberAsyncImagePainter(model = mediaItem.thumbnailUrl),
            contentDescription = "Thumbnail for ${mediaItem.title}",
            modifier = Modifier
                .size(64.dp)
                .padding(end = 8.dp),
            contentScale = ContentScale.Crop
        )
        // Title
        Text(
            text = mediaItem.title,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
        )
    }
}*/