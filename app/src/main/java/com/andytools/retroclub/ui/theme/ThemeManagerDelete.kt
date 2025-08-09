package com.andytools.retroclub.ui.theme

import android.content.Context
import android.content.res.Configuration

/*class ThemeManagerDelete(private val context: Context) {

    private var overrideTheme: Boolean? = null

    val isLightTheme: Boolean
        get() = overrideTheme ?: !isSystemInDarkTheme()

    private fun isSystemInDarkTheme(): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private var onThemeChangedListener: (() -> Unit)? = null

    fun setOnThemeChangedListener(listener: () -> Unit) {
        onThemeChangedListener = listener
    }

    fun getBackgroundColor(): Int {
        return if (isLightTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    }

    fun getTintColor(): String {
        return if (isLightTheme) "#000000" else "#FFFFFF"
    }
}*/