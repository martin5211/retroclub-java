package com.andytools.retroclub

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.andytools.retroclub.common.extensions.applyInsets
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
                                    Image(
                                        painter = painterResource(id = R.drawable.retroclub_full_image),
                                        contentDescription = "Header Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .background(
                                                Color(
                                                    themeManager.getHeaderBgColor().toColorInt()
                                                )
                                            ),
                                        contentScale = ContentScale.Inside
                                    )
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

        themeManager.setOnThemeChangedListener { applyTheme() }
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

    private fun applyTheme() {
        //window.decorView.setBackgroundColor(themeManager.getBackgroundColor().toColorInt())
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipManager.onPictureInPictureModeChanged(isInPictureInPictureMode, playerManager)
        if (isInPictureInPictureMode) {
            // Ensure player is prepared and playing
            if (playerManager.getPlayer().playbackState == Player.STATE_READY) {
                playerManager.play()
            }
        }
    }

    @UnstableApi
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!pipManager.isInPipMode() && playerManager.isPlaying()) {
            pipManager.togglePipMode(playerManager)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!pipManager.isInPipMode()) {
            playerManager.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.releasePlayer()
        castManager.release()
        viewModel.stopRefreshLoop()
    }
}