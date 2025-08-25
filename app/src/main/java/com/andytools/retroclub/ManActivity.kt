package com.andytools.retroclub

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import com.andytools.retroclub.common.utils.Logger
import com.andytools.retroclub.ui.media.cast.CastManager
import com.andytools.retroclub.ui.media.player.ExoPlayerManager
import com.andytools.retroclub.ui.media.player.MediaPlayerScreen
import com.andytools.retroclub.ui.media.player.PipManager
import com.andytools.retroclub.ui.theme.ThemeManager
import com.andytools.retroclub.ui.home.HomeScreen
import com.andytools.retroclub.ui.home.openWhatsAppGroup
import com.andytools.retroclub.ui.media.player.MediaPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.media3.common.Player
import com.andytools.retroclub.ui.theme.ThemeManagerTheme

@UnstableApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var playerManager: ExoPlayerManager
    @Inject lateinit var castManager: CastManager
    @Inject lateinit var pipManager: PipManager
    @Inject lateinit var themeManager: ThemeManager
    private val viewModel: MediaPlayerViewModel by viewModels()

    //private var isFullscreen = false

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playerManager.initializePlayer()
        castManager.initialize()
        viewModel.initialize()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars()) // Hide status bar
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            var isFullscreen by remember { mutableStateOf(false) }
            var isRefreshing by remember { mutableStateOf(false) }

            ThemeManagerTheme(themeManager = themeManager) {
                if (isFullscreen) {
                    // Fullscreen mode - only show MediaPlayerScreen without Scaffold
                    MediaPlayerScreen(
                        playerManager = playerManager,
                        castManager = castManager,
                        pipManager = pipManager,
                        onFullscreenToggle = {
                            isFullscreen = !isFullscreen
                            toggleFullscreenMode(isFullscreen)
                        },
                        isFullscreen = isFullscreen,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        content = { paddingValues ->
                            val customPadding = PaddingValues(
                                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                                top = 0.dp,
                                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                                bottom = paddingValues.calculateBottomPadding()
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(customPadding)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (!pipManager.isInPipMode()) {
                                    Box {
                                        Image(
                                            painter = painterResource(id = R.drawable.retroclub_full_image),
                                            contentDescription = "Header Image - Tap to refresh",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .background(
                                                    Color(
                                                        themeManager.getHeaderBgColor().toColorInt()
                                                    )
                                                )
                                                .clickable {
                                                    Logger.d("Header clicked - triggering refresh")
                                                    if (!isRefreshing) {
                                                        isRefreshing = true
                                                        viewModel.refreshMediaItems()
                                                        playerManager.refreshStream()
                                                    }
                                                },
                                            contentScale = ContentScale.Inside
                                        )
                                        
                                        // Refresh indicator overlay
                                        if (isRefreshing) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .background(Color.Black.copy(alpha = 0.3f)),
                                                contentAlignment = androidx.compose.ui.Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(32.dp),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Handle refresh completion
                                    LaunchedEffect(isRefreshing) {
                                        if (isRefreshing) {
                                            // Reset after 2 seconds
                                            kotlinx.coroutines.delay(2000)
                                            isRefreshing = false
                                        }
                                    }
                                }
                                MediaPlayerScreen(
                                    playerManager = playerManager,
                                    castManager = castManager,
                                    pipManager = pipManager,
                                    onFullscreenToggle = {
                                        isFullscreen = !isFullscreen
                                        toggleFullscreenMode(isFullscreen)
                                    },
                                    isFullscreen = isFullscreen,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                                if (!pipManager.isInPipMode()) {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        themeManager = themeManager,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                    )
                                }
                            }
                        },
                        floatingActionButton = {
                            if (!pipManager.isInPipMode()) {
                                FloatingActionButton(
                                    onClick = { openWhatsAppGroup(this@MainActivity) },
                                    containerColor = Color(
                                        themeManager.getHeaderBgColor().toColorInt()
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                        contentDescription = "Join WhatsApp Group",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    @UnstableApi
    private fun toggleFullscreenMode(isFullscreen: Boolean) {
        if (isFullscreen) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            WindowCompat.getInsetsController(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            WindowCompat.getInsetsController(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.statusBars()) // Hide status bar only
                controller.show(WindowInsetsCompat.Type.navigationBars()) // Show navigation bar
            }
        }
    }

    @UnstableApi
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipManager.onPictureInPictureModeChanged(isInPictureInPictureMode, playerManager)
        if (isInPictureInPictureMode) {
            // Ensure player is prepared and playing
            if (playerManager.getPlayer().playbackState == Player.STATE_READY) {
                playerManager.play()
            }
        } else {
            // Video and sound are already stopped by PipManager
            Logger.d("Exited PIP mode - video and sound stopped")
        }
    }

    @UnstableApi
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!pipManager.isInPipMode() && playerManager.isPlaying() && !playerManager.isAudioOnlyMode()) {
            pipManager.togglePipMode(playerManager)
        }
    }

    @UnstableApi
    override fun onStop() {
        super.onStop()
        // Don't pause if in audio-only mode (allow background audio playback)
        if (!pipManager.isInPipMode() && !playerManager.isAudioOnlyMode()) {
            playerManager.pause()
        }
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()
        playerManager.releasePlayer()
        castManager.release()
        viewModel.stopRefreshLoop()
    }
}