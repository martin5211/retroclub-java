package com.retroclub.retroclub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.retroclub.retroclub.common.utils.Logger

// CompositionLocal to provide ThemeManager colors
val LocalThemeManager = compositionLocalOf<ThemeManager> { error("No ThemeManager provided") }

@Composable
fun ThemeManagerTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    var themeChanged by remember { mutableStateOf(0) }

    // Listen for theme changes
    DisposableEffect(themeManager) {
        themeManager.setOnThemeChangedListener {
            Logger.d("ThemeManagerTheme: Theme changed")
            themeChanged++ // Trigger recomposition
        }
        onDispose {
            themeManager.setOnThemeChangedListener {} // Clear listener
        }
    }

    // Map ThemeManager colors to MaterialTheme
    val colorScheme = if (themeManager.isLightTheme) {
        lightColorScheme(
            primary = Color(themeManager.getHeaderBgColor().toColorInt()),
            background = Color(themeManager.getBackgroundColor()),
            surface = Color(themeManager.getBackgroundColor())
        )
    } else {
        darkColorScheme(
            primary = Color(themeManager.getHeaderBgColor().toColorInt()),
            background = Color(themeManager.getBackgroundColor()),
            surface = Color(themeManager.getBackgroundColor())
        )
    }

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}