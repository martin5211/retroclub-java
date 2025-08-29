package com.retroclub.retroclub.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

class ThemeManager(private val context: Context) {
    var isLightTheme: Boolean = !isSystemInDarkTheme()

    private fun isSystemInDarkTheme(): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private var listener: (() -> Unit)? = null

    fun setOnThemeChangedListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun notifyThemeChanged() {
        listener?.invoke()
    }

    fun getBackgroundColor(): Int = if (isLightTheme) Color.WHITE else Color.BLACK
    fun getHeaderBgColor(): String = if (isLightTheme) "#A1CEDC" else "#1D3D47"
}