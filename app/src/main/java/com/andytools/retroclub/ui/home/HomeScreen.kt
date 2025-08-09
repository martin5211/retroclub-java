package com.andytools.retroclub.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.common.utils.Logger
import com.andytools.retroclub.domain.model.MediaItem
import com.andytools.retroclub.ui.components.MediaList
import com.andytools.retroclub.ui.media.player.MediaPlayerViewModel
import com.andytools.retroclub.ui.theme.ThemeManager

@Composable
fun HomeScreen(
    viewModel: MediaPlayerViewModel,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaItems by viewModel.mediaItems.observeAsState(emptyList())
    val mediaItemsVersion by viewModel.mediaItemsVersion.observeAsState(0)

    MediaList(
        mediaItems = mediaItems,
        themeManager = themeManager,
        version = mediaItemsVersion,
        onItemClick = { item ->
            Logger.d("Media item clicked: ${item.title}")
            // Handle item click
        }
    )
}

fun openWhatsAppGroup(context: android.content.Context) {
    try {
        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(Constants.WHATSAPP_GROUP_URL)
            setPackage("com.whatsapp")
        }

        if (whatsappIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(whatsappIntent)
        } else {
            whatsappIntent.setPackage("com.whatsapp.w4b")
            if (whatsappIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(whatsappIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.WHATSAPP_GROUP_URL))
                context.startActivity(browserIntent)
            }
        }
    } catch (e: Exception) {
        Logger.e("Error opening WhatsApp: ${e.message}")
        Toast.makeText(context, "Error opening WhatsApp group", Toast.LENGTH_SHORT).show()
    }
}