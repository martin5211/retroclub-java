package com.retroclub.retroclub.ui.media.player

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.retroclub.retroclub.R
import com.retroclub.retroclub.common.utils.Logger
import com.google.android.gms.cast.framework.CastButtonFactory
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.mediarouter.app.MediaRouteButton
import com.retroclub.retroclub.ui.media.cast.CastManager

@UnstableApi
@SuppressLint("ClickableViewAccessibility")
@Composable
fun MediaPlayerScreen(
    playerManager: ExoPlayerManager,
    castManager: CastManager,
    pipManager: PipManager,
    onFullscreenToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean
) {
    val context = LocalContext.current
    var isControllerVisible by remember { mutableStateOf(true) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var isAudioOnlyMode by remember { mutableStateOf(false) }

    // Single AndroidView for both PiP and non-PiP modes wrapped in Box for overlay
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (pipManager.isInPipMode()) {
                    Modifier.fillMaxSize()
                } else if (isFullscreen) {
                    Modifier.fillMaxHeight()
                } else {
                    Modifier.aspectRatio(16f / 9f)
                }
            )
    ) {
        AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = playerManager.getPlayer()
                useController = !pipManager.isInPipMode() // Hide controller in PiP mode
                resizeMode = if (pipManager.isInPipMode()) {
                    AspectRatioFrameLayout.RESIZE_MODE_FILL // Fill the PiP window
                } else if (isFullscreen) {
                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    if (pipManager.isInPipMode()) ViewGroup.LayoutParams.MATCH_PARENT
                    else ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // Store reference to playerView
                playerView = this

                // Fullscreen button setup
                post {
                    val fullscreenButton = findViewById<ImageButton>(R.id.custom_fullscreen)
                    fullscreenButton?.let { button ->
                        updateFullscreenButtonAppearance(button, isFullscreen, ctx)
                        button.setOnClickListener {
                            Logger.d("Fullscreen button clicked. Current state: $isFullscreen")
                            onFullscreenToggle()
                        }
                    }
                }

                // PIP button setup
                findViewById<ImageButton>(R.id.custom_pip)?.let { pipButton ->
                    pipButton.setOnClickListener {
                        Logger.d("PIP button clicked")
                        pipManager.togglePipMode(playerManager)
                    }
                    // Update PIP button visibility based on audio-only mode
                    pipButton.visibility = if (isAudioOnlyMode) View.GONE else View.VISIBLE
                }

                // Chromecast button setup
                findViewById<ImageButton>(R.id.custom_chromecast)?.setOnClickListener {
                    if (com.google.android.gms.common.GoogleApiAvailability.getInstance()
                            .isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS
                    ) {
                        findViewById<MediaRouteButton>(R.id.compose_cast_button)?.performClick()
                    } else {
                        Logger.e("Google Play Services not available for Chromecast")
                    }
                }

                // Audio-only button setup
                findViewById<ImageButton>(R.id.custom_audio_only)?.setOnClickListener {
                    Logger.d("Audio-only button clicked. Current state: $isAudioOnlyMode")
                    isAudioOnlyMode = !isAudioOnlyMode
                    playerManager.setAudioOnlyMode(isAudioOnlyMode)
                }


                // Add MediaRouteButton for Chromecast
                val mediaRouteButton = androidx.mediarouter.app.MediaRouteButton(ctx).apply {
                    id = R.id.compose_cast_button
                    visibility = if (pipManager.isInPipMode()) View.GONE else View.INVISIBLE
                    layoutParams = ViewGroup.LayoutParams(1, 1)
                }
                (this as ViewGroup).addView(mediaRouteButton)
                CastButtonFactory.setUpMediaRouteButton(ctx, mediaRouteButton)

                castManager.initialize()

                // Gesture handling
                val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!pipManager.isInPipMode()) {
                            Logger.d("Double tap detected, toggling fullscreen")
                            onFullscreenToggle()
                        }
                        return true
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        if (!pipManager.isInPipMode()) {
                            if (isControllerFullyVisible) {
                                hideController()
                                isControllerVisible = false
                            } else {
                                showController()
                                isControllerVisible = true
                            }
                        }
                        return true
                    }
                })

                setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    true
                }

                // Player listener for error and state handling
                player?.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Logger.e("Error playing media: ${error.message}")
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY, Player.STATE_BUFFERING -> {
                                keepScreenOn = true
                            }
                            Player.STATE_ENDED, Player.STATE_IDLE -> {
                                keepScreenOn = false
                            }
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        // Ensure the surface is ready
                        Logger.d("First frame rendered")
                    }
                })
            }
        },
        update = { view ->
            // Update properties when state changes
            view.useController = !pipManager.isInPipMode()
            view.resizeMode = if (pipManager.isInPipMode()) {
                AspectRatioFrameLayout.RESIZE_MODE_FILL // Fill the PiP window
            } else if (isFullscreen) {
                AspectRatioFrameLayout.RESIZE_MODE_FILL
            } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (pipManager.isInPipMode()) ViewGroup.LayoutParams.MATCH_PARENT
                else ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val fullscreenButton = view.findViewById<ImageButton>(R.id.custom_fullscreen)
            fullscreenButton?.let { button ->
                updateFullscreenButtonAppearance(button, isFullscreen, context)
            }
            // Update PIP button visibility based on audio-only mode
            view.findViewById<ImageButton>(R.id.custom_pip)?.visibility = 
                if (isAudioOnlyMode || pipManager.isInPipMode()) View.GONE else View.VISIBLE
            // Ensure Chromecast button visibility
            view.findViewById<MediaRouteButton>(R.id.compose_cast_button)?.visibility =
                if (pipManager.isInPipMode()) View.GONE else View.INVISIBLE
        },
        modifier = Modifier.fillMaxSize()
    )

        // Black overlay for audio-only mode (hide when controls are visible)
        if (isAudioOnlyMode && !pipManager.isInPipMode()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable {
                        isAudioOnlyMode = false
                        playerManager.setAudioOnlyMode(false)
                    }
            )
        }
    }

    // Update fullscreen button when isFullscreen changes
    LaunchedEffect(isFullscreen, pipManager.isInPipMode()) {
        playerView?.let { view ->
            val fullscreenButton = view.findViewById<ImageButton>(R.id.custom_fullscreen)
            fullscreenButton?.let { button ->
                updateFullscreenButtonAppearance(button, isFullscreen, context)
            }
            // Ensure player is playing in PiP mode
            if (pipManager.isInPipMode() && playerManager.getPlayer()?.isPlaying != true) {
                playerManager.getPlayer()?.play()
            }
        }
    }
}

// Helper function to update button appearance
fun updateFullscreenButtonAppearance(
    button: ImageButton,
    isFullscreen: Boolean,
    context: android.content.Context
) {
    Logger.d("Updating fullscreen button appearance. isFullscreen: $isFullscreen")
    if (isFullscreen) {
        button.setImageResource(R.drawable.ic_fullscreen_shrink)
        button.contentDescription = context.getString(R.string.exit_fullscreen)
    } else {
        button.setImageResource(R.drawable.ic_fullscreen_expand)
        button.contentDescription = context.getString(R.string.fullscreen)
    }
}