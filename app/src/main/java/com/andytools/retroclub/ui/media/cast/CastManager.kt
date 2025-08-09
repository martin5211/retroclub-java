package com.andytools.retroclub.ui.media.cast

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.Player
import com.andytools.retroclub.R
import com.andytools.retroclub.common.constants.Constants
import com.andytools.retroclub.common.utils.Logger
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class CastManager @Inject constructor(
    @ActivityContext private val context: Context,
    private val castContext: CastContext?
) {
    private var castSession: CastSession? = null
    private var remoteMediaClient: com.google.android.gms.cast.framework.media.RemoteMediaClient? = null
    private var onCastStateChanged: ((Boolean) -> Unit)? = null

    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            castSession = session
            remoteMediaClient = session.remoteMediaClient
            loadMediaToCast()
            onCastStateChanged?.invoke(true)
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            castSession = null
            remoteMediaClient = null
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

    private fun loadMediaToCast() {
        remoteMediaClient?.let { client ->
            val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                putString(MediaMetadata.KEY_TITLE, "Retroclub")
            }
            val mediaInfo = MediaInfo.Builder(Constants.VIDEO_URL)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType("application/x-mpegURL")
                .setMetadata(metadata)
                .build()

            val requestData = MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build()

            client.load(requestData).setResultCallback { result ->
                if (!result.status.isSuccess) {
                    Logger.e("Failed to load stream to Chromecast: ${result.status.statusMessage}")
                }
            }
        }
    }

    fun resumeLocalPlayback(player: Player) {
        player.playWhenReady = true
    }

    fun pauseLocalPlayback(player: Player) {
        player.pause()
    }

    fun release() {
        castContext?.sessionManager?.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
    }
}