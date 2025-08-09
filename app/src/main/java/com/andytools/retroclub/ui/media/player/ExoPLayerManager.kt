package com.andytools.retroclub.ui.media.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.common.utils.Logger
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class ExoPlayerManager @Inject constructor(
    @ActivityContext private val context: Context,
    private val exoPlayer: ExoPlayer
) {
    fun initializePlayer() {
        try {
            val mediaItem = MediaItem.fromUri(Constants.VIDEO_URL)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            Logger.e("Error starting playback: ${e.message}", e)
        }
    }

    fun getPlayer(): Player = exoPlayer

    fun releasePlayer() {
        exoPlayer.release()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun play() {
        exoPlayer.play()
    }

    fun isPlaying(): Boolean = exoPlayer.isPlaying
}