package com.retroclub.retroclub.ui.media.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.retroclub.retroclub.common.constants.Constants
import com.retroclub.retroclub.common.utils.Logger
import com.retroclub.retroclub.domain.usecase.AuthenticateSettingsUseCase
import com.retroclub.retroclub.domain.usecase.GetStreamUrlUseCase
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
class ExoPlayerManager @Inject constructor(
    @ActivityContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val authenticateUserUseCase: AuthenticateSettingsUseCase,
    private val getStreamUrlUseCase: GetStreamUrlUseCase
) {
    private val trackSelector = exoPlayer.trackSelector as? DefaultTrackSelector
    private var accessToken: String? = null
    private var streamUrl: String = Constants.VIDEO_URL
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var refreshJob: Job? = null
    private var isRefreshLoopActive = false
    private var currentStreamUrl: String? = null
    private var isAudioOnlyModeEnabled = false
    private var castManager: com.retroclub.retroclub.ui.media.cast.CastManager? = null

    private suspend fun authenticate() {
        accessToken = authenticateUserUseCase.execute("andy", "nomeacuerdo")
        Logger.d("Authentication successful")
    }

    private suspend fun loadStreamUrl() {
        streamUrl = getStreamUrlUseCase.execute(accessToken)
    }

    private fun createLiveMediaItem(streamUrl: String): MediaItem {
        return MediaItem.fromUri(streamUrl)
    }

    fun initializePlayer(
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        scope.launch {
            try {
                authenticate()
                loadStreamUrl()
                startRefreshLoop()
                val mediaItem = createLiveMediaItem(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                currentStreamUrl = streamUrl
                onSuccess?.invoke()
            } catch (e: Exception) {
                Logger.e("Error starting playback: ${e.message}", e)
                onError?.invoke(e)
            }
        }
    }

    private fun startRefreshLoop() {
        if (isRefreshLoopActive) return
        isRefreshLoopActive = true
        refreshJob = scope.launch {
            while (isRefreshLoopActive) {
                delay(Constants.REFRESH_INTERVAL_MS)
                try {
                    if (accessToken == null) authenticate()
                    val previousStreamUrl = streamUrl
                    loadStreamUrl()

                    Logger.d("Refresh cycle - Previous URL: $previousStreamUrl")
                    Logger.d("Refresh cycle - Current URL: $streamUrl")
                    Logger.d("Refresh cycle - URLs same: ${streamUrl == currentStreamUrl}")

                    // Only change streamUrl if it is different to currentStreamUrl and not casting
                    if (streamUrl != currentStreamUrl) {
                        if (castManager?.isCasting() == true) {
                            Logger.d("Casting active, skipping local player update")
                            currentStreamUrl = streamUrl // Update URL but don't change player
                        } else {
                            val mediaItem = createLiveMediaItem(streamUrl)
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            currentStreamUrl = streamUrl
                            Logger.d("Stream URL changed, updating media item: $streamUrl")
                        }
                    } else {
                        Logger.d("Stream URL unchanged, skipping setMediaItem")
                    }
                } catch (e: Exception) {
                    Logger.e("Error during refresh: ${e.message}", e)
                    if (e.message?.contains("Authentication") == true || e.message?.contains("401") == true) {
                        accessToken = null
                    }
                }
            }
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
        // Ensure refresh loop is active when playing
        if (!isRefreshLoopActive) {
            startRefreshLoop()
        }
    }

    fun isPlaying(): Boolean = exoPlayer.isPlaying

    fun stop() {
        exoPlayer.stop()
        stopRefreshLoop()
    }

    fun stopRefreshLoop() {
        isRefreshLoopActive = false
        refreshJob?.cancel()
        refreshJob = null
    }

    fun setAudioOnlyMode(audioOnly: Boolean) {
        trackSelector?.let { selector ->
            val parametersBuilder = selector.parameters.buildUpon()
            if (audioOnly) {
                // Disable video tracks
                parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
                Logger.d("Audio-only mode enabled - video tracks disabled")
            } else {
                // Enable video tracks
                parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                Logger.d("Audio-only mode disabled - video tracks enabled")
            }
            selector.setParameters(parametersBuilder.build())
            isAudioOnlyModeEnabled = audioOnly
        }
    }

    fun isAudioOnlyMode(): Boolean {
        return isAudioOnlyModeEnabled
    }
    
    
    fun setCastManager(castManager: com.retroclub.retroclub.ui.media.cast.CastManager) {
        this.castManager = castManager
    }
    
    fun refreshStream() {
        scope.launch {
            try {
                if (accessToken == null) authenticate()
                val previousStreamUrl = streamUrl
                loadStreamUrl()

                Logger.d("Stream refresh - Previous URL: $previousStreamUrl")
                Logger.d("Stream refresh - Current URL: $streamUrl")
                Logger.d("Stream refresh - URLs same: ${streamUrl == currentStreamUrl}")

                // Only change streamUrl if it is different to currentStreamUrl and not casting
                if (streamUrl != currentStreamUrl) {
                    if (castManager?.isCasting() == true) {
                        Logger.d("Casting active, skipping local player refresh")
                        currentStreamUrl = streamUrl // Update URL but don't change player
                    } else {
                        val mediaItem = createLiveMediaItem(streamUrl)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()
                        currentStreamUrl = streamUrl
                        Logger.d("Stream URL refreshed, updating media item: $streamUrl")
                    }
                } else {
                    Logger.d("Stream URL unchanged during refresh")
                }
            } catch (e: Exception) {
                Logger.e("Error during stream refresh: ${e.message}", e)
            }
        }
    }
    fun refreshStreamOnDemand() {
        scope.launch {
            try {
                if (accessToken == null) authenticate()
                loadStreamUrl()

                // Change streamUrl on-demand
                val mediaItem = createLiveMediaItem(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                Logger.d("Stream URL refreshed, updating media item: $streamUrl")

            } catch (e: Exception) {
                Logger.e("Error during stream refresh: ${e.message}", e)
            }
        }
    }
    
}