package com.andytools.retroclub

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.media3.ui.PlayerView

class AspectRatioPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PlayerView(context, attrs, defStyleAttr) {

    private val aspectRatio = 16f / 9f
    private var isFullscreenMode = false

    fun setFullscreenMode(fullscreen: Boolean) {
        if (isFullscreenMode != fullscreen) {
            isFullscreenMode = fullscreen
            requestLayout() // Trigger remeasure
        }
    }

    fun setPipMode(pipMode: Boolean) {
        isFullscreenMode = pipMode
        requestLayout() // Trigger remeasure
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isFullscreenMode) {
            // In fullscreen: get the actual available space
            val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
            val availableHeight = MeasureSpec.getSize(heightMeasureSpec)

            // Use the full available space if the parent is giving us MATCH_PARENT
            val screenWidth = if (availableWidth > 0 && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                availableWidth
            } else {
                resources.displayMetrics.widthPixels
            }

            val screenHeight = if (availableHeight > 0 && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                availableHeight
            } else {
                resources.displayMetrics.heightPixels
            }

            // Calculate dimensions based on aspect ratio to fit within screen
            //val widthBasedHeight = (screenWidth / aspectRatio).toInt()
            //val heightBasedWidth = (screenHeight * aspectRatio).toInt()

            val finalWidth: Int
            val finalHeight: Int

            finalWidth = screenWidth
            finalHeight = screenHeight

            // Create new measure specs with our calculated dimensions
            val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY)
            val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)

            super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        } else {
            // Normal mode: use width and calculate height with minimum constraint
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = (width / aspectRatio).toInt()

            // Ensure minimum height
            val minHeight = (200 * resources.displayMetrics.density).toInt()
            val finalHeight = maxOf(height, minHeight)

            // Create new measure specs with our calculated dimensions
            val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)

            super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        }
    }
}