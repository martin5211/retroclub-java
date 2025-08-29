package com.retroclub.retroclub.ui.media.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.util.UnstableApi

class PipManager @Inject constructor(
    @ActivityContext private val context: Context
) {
    private val activity = context as? AppCompatActivity
    private val _isPipMode: MutableState<Boolean> = mutableStateOf(false)
    private var wasPlayingBeforePip = false

    @UnstableApi
    fun togglePipMode(player: ExoPlayerManager) {
        if (activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true) {
            if (!_isPipMode.value) {
                // Wait for video size to be available
                val videoSize = player.getPlayer()?.videoSize
                val aspectRatio = if (videoSize != null && videoSize.height > 0 && videoSize.width > 0) {
                    // Ensure aspect ratio is within Android's supported range (0.4184 to 2.39)
                    val ratio = videoSize.width.toFloat() / videoSize.height.toFloat()
                    Rational(
                        videoSize.width.coerceAtMost(239 * videoSize.height),
                        videoSize.height.coerceAtMost(videoSize.width * 239 / 100)
                    )
                } else {
                    Rational(16, 9) // Fallback to 16:9
                }
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                activity.enterPictureInPictureMode(params)
                _isPipMode.value = true
                wasPlayingBeforePip = player.isPlaying()
                if (wasPlayingBeforePip) {
                    player.play()
                }
            }
        }
    }

    @UnstableApi
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, player: ExoPlayerManager) {
        _isPipMode.value = isInPictureInPictureMode
        if (!isInPictureInPictureMode) {
            // Stop video and sound when exiting PIP mode
            player.stop()
        } else if (isInPictureInPictureMode) {
            wasPlayingBeforePip = player.isPlaying()
        }
    }

    fun isInPipMode(): Boolean = _isPipMode.value
}