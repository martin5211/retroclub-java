package com.retroclub.retroclub.ui.media.cast

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.retroclub.retroclub.common.constants.Constants
import com.retroclub.retroclub.common.utils.Logger
import com.retroclub.retroclub.domain.usecase.AuthenticateSettingsUseCase
import com.retroclub.retroclub.domain.usecase.GetStreamUrlUseCase
import com.retroclub.retroclub.ui.media.player.ExoPlayerManager
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
class CastManager @Inject constructor(
    @ActivityContext private val context: Context,
    private val castContext: CastContext?,
    private val authenticateUserUseCase: AuthenticateSettingsUseCase,
    private val getStreamUrlUseCase: GetStreamUrlUseCase
) {
    private var castSession: CastSession? = null
    private var remoteMediaClient: com.google.android.gms.cast.framework.media.RemoteMediaClient? = null
    private var onCastStateChanged: ((Boolean) -> Unit)? = null
    private var accessToken: String? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var exoPlayerManager: ExoPlayerManager? = null
    private var isCasting = false

    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        @UnstableApi
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            Logger.d("Cast session started - preparing to load media")
            isCasting = true
            castSession = session
            remoteMediaClient = session.remoteMediaClient
            loadMediaToCast()
            onCastStateChanged?.invoke(true)
        }

        @UnstableApi
        override fun onSessionEnded(session: CastSession, error: Int) {
            Logger.d("Cast session ended - resuming local playback")
            isCasting = false
            castSession = null
            remoteMediaClient = null
            exoPlayerManager?.play()
            onCastStateChanged?.invoke(false)
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
        override fun onSessionStartFailed(session: CastSession, error: Int) {}
        override fun onSessionResumeFailed(session: CastSession, error: Int) {}
    }

    fun initialize() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            castContext!!.sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        } else {
            Logger.e("Google Play Services not available for Chromecast")
        }
    }

    fun setOnCastStateChangedListener(listener: (Boolean) -> Unit) {
        onCastStateChanged = listener
    }

    @UnstableApi
    fun setExoPlayerManager(playerManager: ExoPlayerManager) {
        this.exoPlayerManager = playerManager
    }

    private suspend fun authenticate() {
        accessToken = authenticateUserUseCase.execute("andy", "nomeacuerdo")
        Logger.d("Authentication successful for cast")
    }

    private suspend fun loadStreamUrl(): String {
        return getStreamUrlUseCase.execute(accessToken)
    }

    private fun loadMediaToCast() {
        scope.launch {
            try {
                val streamUrl = try {
                    if (accessToken == null) authenticate()
                    loadStreamUrl()
                } catch (e: Exception) {
                    Logger.e("Failed to get authenticated stream URL for cast, using fallback: ${e.message}", e)
                    Constants.VIDEO_URL
                }

                remoteMediaClient?.let { client ->
                    val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                        putString(MediaMetadata.KEY_TITLE, "Retroclub")
                    }
                    val mediaInfo = MediaInfo.Builder(streamUrl)
                        .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                        .setContentType("application/x-mpegURL")
                        .setMetadata(metadata)
                        .build()

                    val requestData = MediaLoadRequestData.Builder()
                        .setMediaInfo(mediaInfo)
                        .setAutoplay(true)
                        .setCurrentTime(0)
                        .build()

                    client.load(requestData).setResultCallback { result ->
                        if (!result.status.isSuccess) {
                            Logger.e("Failed to load stream to Chromecast: ${result.status.statusMessage}")
                            // Resume local playback if cast fails
                            exoPlayerManager?.play()
                        } else {
                            Logger.d("Successfully loaded stream to Chromecast: $streamUrl")
                            // Only pause local playback after successful cast load
                            exoPlayerManager?.pause()
                            // Ensure cast starts playing
                            client.play()
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("Error in loadMediaToCast: ${e.message}", e)
            }
        }
    }

    fun resumeLocalPlayback(player: Player) {
        player.playWhenReady = true
    }

    fun pauseLocalPlayback(player: Player) {
        player.pause()
    }

    fun isCasting(): Boolean = isCasting

    fun release() {
        castContext?.sessionManager?.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
    }
}