package com.andytools.retroclub.ui.media.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.common.utils.Logger
import com.andytools.retroclub.domain.usecase.AuthenticateSettingsUseCase
import com.andytools.retroclub.domain.usecase.GetStreamUrlUseCase
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
    private var liveTimeBar: LiveTimeBar? = null

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
                liveTimeBar?.resetVirtualTimer()
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

                    // Only change streamUrl if it is different to currentStreamUrl
                    if (streamUrl != currentStreamUrl) {
                        val mediaItem = createLiveMediaItem(streamUrl)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()
                        currentStreamUrl = streamUrl
                        Logger.d("Stream URL changed, updating media item: $streamUrl")
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
    
    fun setLiveTimeBar(timeBar: LiveTimeBar) {
        this.liveTimeBar = timeBar
        timeBar.setPlayer(exoPlayer)
    }
    
    fun seekBackward(seekBackMs: Long = 30000L) {
        val currentPosition = exoPlayer.currentPosition
        val bufferedPosition = exoPlayer.bufferedPosition
        val duration = exoPlayer.duration
        val totalBufferedDuration = exoPlayer.totalBufferedDuration
        
        Logger.d("=== SEEK BACKWARD DEBUG ===")
        Logger.d("Current position: $currentPosition ms")
        Logger.d("Buffered position: $bufferedPosition ms") 
        Logger.d("Total buffered duration: $totalBufferedDuration ms")
        Logger.d("Duration: $duration ms")
        Logger.d("Requested seek back: $seekBackMs ms")
        
        // Simple approach - just seek back, let ExoPlayer handle it
        val targetPosition = maxOf(0L, currentPosition - seekBackMs)
        
        Logger.d("Target position: $targetPosition ms")
        Logger.d("Calling exoPlayer.seekTo($targetPosition)")
        
        try {
            exoPlayer.seekTo(targetPosition)
            Logger.d("Seek completed successfully")
        } catch (e: Exception) {
            Logger.e("Seek failed: ${e.message}", e)
        }
    }
    
    fun seekForward(seekForwardMs: Long = 30000L) {
        val currentPosition = exoPlayer.currentPosition
        val bufferedPosition = exoPlayer.bufferedPosition
        val newPosition = minOf(bufferedPosition, currentPosition + seekForwardMs)
        Logger.d("Seeking forward: from $currentPosition to $newPosition")
        exoPlayer.seekTo(newPosition)
    }
}