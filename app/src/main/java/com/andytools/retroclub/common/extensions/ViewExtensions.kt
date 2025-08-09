package com.andytools.retroclub.common.extensions

import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowInsetsCompat
import androidx.media3.ui.PlayerView

fun PlayerView.applyInsets(isFullscreen: Boolean, insets: WindowInsetsCompat) {
    if (!isFullscreen) {
        setPadding(0, 0, 0, 0) // No top padding
    } else {
        setPadding(0, 0, 0, 0)
    }
}

fun ComposeView.applyInsets(insets: WindowInsetsCompat) {
    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    setPadding(0, 0, 0, systemBars.bottom)
}