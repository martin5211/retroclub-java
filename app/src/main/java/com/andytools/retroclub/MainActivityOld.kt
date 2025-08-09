package com.andytools.retroclub

import android.annotation.SuppressLint
import android.app.Application
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import androidx.lifecycle.lifecycleScope

/*class MainActivityOld : AppCompatActivity() {

    companion object {
        private const val VIDEO_URL = "https://live20.bozztv.com/akamaissh101/ssh101/retroclub/playlist.m3u8"
        private const val MEDIA_CONTROLLER_TIMEOUT = 3000L // 3 seconds
        private const val API_BASE_URL = "https://api.martincaminoa.com.ar" // Replace with your API URL
        private const val TOKEN_ENDPOINT = "$API_BASE_URL/login"
        private const val MEDIA_ITEMS_ENDPOINT = "$API_BASE_URL/items"
        private const val REFRESH_INTERVAL_MINUTES = 60L // 15 minutes
        private const val REFRESH_INTERVAL_MS = REFRESH_INTERVAL_MINUTES * 60 * 1000L // Convert to milliseconds
        private const val WHATSAPP_GROUP_URL = "https://chat.whatsapp.com/DlTY8bciHttJeeI9wVGAx7?mode=ac_t"
    }

    private lateinit var mainLayout: ScrollView
    private lateinit var playerView: PlayerView
    private var playerControlView: LinearLayout? = null
    private lateinit var mediaRouteButton: MediaRouteButton
    private lateinit var headerImageView: ImageView
    private lateinit var mediaListComposeView: ComposeView
    private lateinit var whatsappButton: Button

    private var player: Player? = null
    private var isPipMode = false
    private var isFullscreen = false
    private var wasPlayingBeforePip = false // Track playback state before entering PiP
    private var isActivityStopped = false // Track if activity is stopped
    private var castContext: CastContext? = null
    private var castSession: CastSession? = null
    //private lateinit var themeManager: ThemeManagerDelete
    private var remoteMediaClient: RemoteMediaClient? = null

    // API related properties
    private var accessToken: String? = null
    private val httpClient = OkHttpClient()
    private var mediaItems = mutableListOf<MediaItem>()
    private var mediaItemsVersion = 0 // Add version counter to trigger recomposition

    // Refresh loop properties
    private var refreshJob: Job? = null
    private var isRefreshLoopActive = false

    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            castSession = session
            remoteMediaClient = session.remoteMediaClient
            loadMediaToCast()
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            castSession = null
            remoteMediaClient = null
            resumeLocalPlayback()
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
        override fun onSessionStartFailed(session: CastSession, error: Int) {}
        override fun onSessionResumeFailed(session: CastSession, error: Int) {}
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        mainLayout = findViewById(R.id.mainLayout)
        playerView = findViewById(R.id.playerView)
        mediaRouteButton = findViewById(R.id.media_route_button)
        headerImageView = findViewById(R.id.headerImageView)
        mediaListComposeView = findViewById(R.id.mediaListComposeView)
        whatsappButton = findViewById(R.id.whatsappButton) // Add this line

        // Initialize ThemeManager with context
        //themeManager = ThemeManagerDelete(this)

        // Set initial scroll state
        updateScrollState(resources.configuration.orientation)

        // Setup Cast button
        CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton)

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            castContext = CastContext.getSharedInstance(this)
            castContext?.sessionManager?.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        }

        // Setup video playback first to ensure playerControlView is available
        setupVideoPlayer()

        // Setup fullscreen and chromecast buttons after video player setup
        setupFullscreenButton()
        setupChromecastButton()

        // Setup WhatsApp button
        setupWhatsAppButton()

        // Apply theme
        applyTheme()

        // Initialize API and load media items
        initializeAPI()

        // Handle window insets
        setupWindowInsets()

        // Handle system bars for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            playerView.setPadding(0, 0, 0, 0) // Adjust only top padding
            mediaListComposeView.setPadding(0, 0, 0, systemBars.bottom) // Adjust bottom padding
            insets
        }

        /*themeManager.setOnThemeChangedListener {
            applyTheme()
            setupMediaList()
        }*/
    }

    private fun setupWhatsAppButton() {
        whatsappButton.setOnClickListener {
            openWhatsAppGroup()
        }
    }

    private fun openWhatsAppGroup() {
        try {
            // First try to open with WhatsApp app
            val whatsappIntent = Intent(Intent.ACTION_VIEW)
            whatsappIntent.data = Uri.parse(WHATSAPP_GROUP_URL)
            whatsappIntent.setPackage("com.whatsapp")

            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent)
            } else {
                // If WhatsApp is not installed, try WhatsApp Business
                whatsappIntent.setPackage("com.whatsapp.w4b")
                if (whatsappIntent.resolveActivity(packageManager) != null) {
                    startActivity(whatsappIntent)
                } else {
                    // If neither WhatsApp nor WhatsApp Business is installed, open in browser
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_GROUP_URL))
                    startActivity(browserIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("Retroclub", "Error opening WhatsApp: ${e.message}")
            Toast.makeText(this, "Error opening WhatsApp group", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateScrollState(orientation: Int) {
        val isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT

        if (isPortrait) {
            // Portrait: Enable NestedScrollView scrolling
            mainLayout.isNestedScrollingEnabled = true
            // Enable touch scrolling on NestedScrollView
            mainLayout.setOnTouchListener(null)
        } else {
            // Landscape: Enable NestedScrollView scrolling as well
            mainLayout.isNestedScrollingEnabled = true
            // Enable touch scrolling on NestedScrollView
            mainLayout.setOnTouchListener(null)
        }

        Log.d("Retroclub", "Scroll state updated - Portrait: $isPortrait, NestedScrolling: ${mainLayout.isNestedScrollingEnabled}")
    }

    // API Authentication Methods
    private fun initializeAPI() {
        lifecycleScope.launch {
            try {
                authenticateUser()
                loadMediaItemsFromAPI()
                startRefreshLoop() // Start the refresh loop after successful initialization
            } catch (e: Exception) {
                Log.e("Retroclub", "Failed to initialize API: ${e.message}")
                // Fallback to sample data
                setupMediaListWithSampleData()
                // Still start refresh loop to retry later
                startRefreshLoop()
            }
        }
    }

    private fun startRefreshLoop() {
        if (isRefreshLoopActive) {
            Log.d("Retroclub", "Refresh loop already active")
            return
        }

        isRefreshLoopActive = true
        refreshJob = lifecycleScope.launch {
            while (isActive && isRefreshLoopActive) {
                try {
                    // Wait for the refresh interval
                    delay(REFRESH_INTERVAL_MS)

                    Log.d("Retroclub", "Starting scheduled media items refresh")

                    // Check if we have a valid token, if not, re-authenticate
                    if (accessToken == null) {
                        Log.d("Retroclub", "No access token found, re-authenticating")
                        authenticateUser()
                    }

                    // Load media items from API
                    loadMediaItemsFromAPI()

                } catch (e: Exception) {
                    Log.e("Retroclub", "Error during scheduled refresh: ${e.message}")
                    // If authentication fails, try again
                    if (e.message?.contains("Authentication") == true || e.message?.contains("401") == true) {
                        Log.d("Retroclub", "Authentication error, will retry authentication on next cycle")
                        accessToken = null // Reset token to force re-authentication
                    }
                }
            }
        }

        Log.d("Retroclub", "Started media items refresh loop (${REFRESH_INTERVAL_MINUTES} minutes interval)")
    }

    private fun stopRefreshLoop() {
        isRefreshLoopActive = false
        refreshJob?.cancel()
        refreshJob = null
        Log.d("Retroclub", "Stopped media items refresh loop")
    }

    private suspend fun authenticateUser() {
        return withContext(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("username", "admin") // Replace with actual credentials
                put("password", "admin123") // Replace with actual credentials
                // For OAuth, you might use different fields like client_id, client_secret, grant_type
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(TOKEN_ENDPOINT)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "")
                accessToken = jsonResponse.getString("access_token") // or "token" depending on your API
                Log.d("Retroclub", "Authentication successful")
            } else {
                throw IOException("Authentication failed: ${response.code}")
            }
        }
    }

    private suspend fun loadMediaItemsFromAPI() {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(MEDIA_ITEMS_ENDPOINT)
                .addHeader("Authorization", "Bearer $accessToken") // For JWT
                // For OAuth, you might use: .addHeader("Authorization", "OAuth $accessToken")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonArray = JSONArray(responseBody ?: "[]")

                val newMediaItems = mutableListOf<MediaItem>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    newMediaItems.add(
                        MediaItem(
                            title = item.getString("title"), // Adjust field names according to your API
                            thumbnailUrl = item.getString("thumbnail_url") // Adjust field names according to your API
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    mediaItems.clear()
                    mediaItems.addAll(newMediaItems)
                    mediaItemsVersion++ // Increment version to trigger recomposition
                    setupMediaList()
                }
                Log.d("Retroclub", "Loaded ${newMediaItems.size} media items from API")
            } else {
                throw IOException("Failed to load media items: ${response.code}")
            }
        }
    }

    private fun setupMediaListWithSampleData() {
        // Fallback sample data
        mediaItems.clear()
        mediaItems.addAll(listOf(
            MediaItem("Sting", "https://www.theaudiodb.com/images/media/album/thumb/ysqysy1558955451.jpg/medium"),
            MediaItem("INXS", "https://www.theaudiodb.com/images/media/album/thumb/xttuts1341508831.jpg/medium"),
            MediaItem("INXS", "https://www.theaudiodb.com/images/media/album/thumb/wvwyvx1341508865.jpg/medium"),
            MediaItem("Pet Shop Boys", "https://www.theaudiodb.com/images/media/album/3dthumb/7ftghr1714258542.png/medium"),
            MediaItem("Duran Duran", "https://www.theaudiodb.com/images/media/album/3dthumb/472gzj1698842925.png/medium"),
            MediaItem("Ozzy", "https://www.theaudiodb.com/images/media/album/3dthumb/8z24021641748545.png/medium"),
            MediaItem("Queen", "https://www.theaudiodb.com/images/media/album/3dthumb/tcgizr1717518013.png/medium"),
            MediaItem("A-HA", "https://www.theaudiodb.com/images/media/album/3dthumb/q31aye1666349375.png/medium"),
            MediaItem("Genesis", "https://www.theaudiodb.com/images/media/album/3dthumb/r0gkpu1606295269.png/medium"),
            MediaItem("Rick Astley", "https://www.theaudiodb.com/images/media/album/3dthumb/7qfvi91697448450.png/medium"),
            MediaItem("John Lennon", "https://www.theaudiodb.com/images/media/album/thumb/menlove-ave-519210bf6c5ec.jpg/medium"),
            MediaItem("Michael Jackson", "https://www.theaudiodb.com/images/media/album/3dthumb/0otx0h1611038388.png/medium"),
            MediaItem("George Michael", "https://www.theaudiodb.com/images/media/album/3dthumb/f6gjc31606312563.png/medium"),
        ))
        mediaItemsVersion++ // Increment version to trigger recomposition
        setupMediaList()
    }

    private fun setupMediaList() {
        mediaListComposeView.setContent {
            /*(
                mediaItems = mediaItems.toList(), // Create a new list to trigger recomposition
                themeManager = null,
                version = mediaItemsVersion, // Pass version to force recomposition
                onItemClick = { item ->
                    Log.d("Retroclub", "Media item clicked: ${item.title}")
                    // Handle item click (e.g., play specific media)
                }
            )*/
        }
    }

    @UnstableApi
    private fun setupFullscreenButton() {
        // Use post to ensure the view is ready
        playerView.post {
            val fullscreenButton = playerView.findViewById<ImageButton>(R.id.custom_fullscreen)
            fullscreenButton?.setOnClickListener {
                toggleFullscreen()
            }
        }
    }

    private fun setupChromecastButton() {
        // Use post to ensure the view is ready
        playerView.post {
            val chromecastButton = playerView.findViewById<ImageButton>(R.id.custom_chromecast)
            chromecastButton?.setOnClickListener {
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                    // Trigger the same behavior as the mediaRouteButton
                    mediaRouteButton.performClick() // Simulate click on mediaRouteButton to show Cast dialog
                } else {
                    // Handle case where Google Play Services is not available
                    Log.e("Retroclub", "Google Play Services not available for Chromecast")
                }
            }
        }
    }

    private fun setupWindowInsets() {
        // Handle window insets for non-fullscreen mode
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            if (!isFullscreen) {
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                playerView.setPadding(0, 0, 0, 0) // No top padding needed
                mediaListComposeView.setPadding(0, 0, 0, systemBars.bottom)
                insets
            } else {
                // In fullscreen, consume all insets to draw edge-to-edge
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val window = window
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Update the custom player view's fullscreen state
        (playerView as? AspectRatioPlayerView)?.setFullscreenMode(isFullscreen)

        if (isFullscreen) {
            // Enter fullscreen: Hide system bars and remove insets
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            headerImageView.visibility = View.GONE
            mediaListComposeView.visibility = View.GONE // Hide media list in fullscreen
            whatsappButton.visibility = View.GONE
            playerView.setPadding(0, 0, 0, 0) // Ensure no padding in fullscreen
            mainLayout.setPadding(0, 0, 0, 0) // Remove any layout padding
            playerView.findViewById<ImageButton>(R.id.custom_fullscreen)?.setImageResource(R.drawable.ic_fullscreen_shrink)

            // Use FIT mode in fullscreen to prevent overflow and maintain aspect ratio
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

            // Force a layout pass to apply the new measurements
            playerView.requestLayout()

            // Ensure PlayerView fills the entire screen
            val layoutParams = playerView.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            playerView.layoutParams = layoutParams

            window.decorView.requestApplyInsets()
        } else {
            // Exit fullscreen: Restore system bars and insets
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())

            // Allow orientation changes when exiting fullscreen
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            headerImageView.visibility = View.VISIBLE
            mediaListComposeView.visibility = View.VISIBLE // Show media list
            whatsappButton.visibility = View.VISIBLE

            val systemBars = WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.systemBars())
            playerView.setPadding(0, 0, 0, 0) // No top padding
            mediaListComposeView.setPadding(0, 0, 0, systemBars.bottom)
            mainLayout.setPadding(0, 0, 0, 0) // Reset layout padding
            playerView.findViewById<ImageButton>(R.id.custom_fullscreen)?.setImageResource(R.drawable.ic_fullscreen_expand)

            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

            // Force a layout pass to apply the new measurements
            playerView.requestLayout()

            // Reset PlayerView layout parameters for normal mode
            val layoutParams = playerView.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            playerView.layoutParams = layoutParams

            window.decorView.requestApplyInsets()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyTheme()
        updateScrollState(newConfig.orientation)
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // Ensure insets are consumed in fullscreen
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, _ ->
                WindowInsetsCompat.CONSUMED
            }
        } else {
            // Restore insets handling
            setupWindowInsets()
        }

        // Refresh MediaList to handle orientation change
        setupMediaList()
    }

    private fun applyTheme() {
       val backgroundColor = if (themeManager.isLightTheme) Color.WHITE else Color.BLACK
        val headerBgColor = if (themeManager.isLightTheme) "#A1CEDC" else "#1D3D47"
        mediaRouteButton.setBackgroundColor(Color.TRANSPARENT)
        headerImageView.setBackgroundColor(headerBgColor.toColorInt())
        whatsappButton.setBackgroundColor(headerBgColor.toColorInt())
        window.decorView.setBackgroundColor(backgroundColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupVideoPlayer() {
        // Initialize ExoPlayer Player
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Set media source
        try {
            val mediaItem = ExoMediaItem.fromUri(VIDEO_URL)
            player?.setMediaItem(mediaItem)
        } catch (e: Exception) {
            Log.e("Retroclub", "Error starting playback: ${e.message}")
        }
        // Configure playback
        player?.repeatMode = Player.REPEAT_MODE_ALL // Loop playback
        player?.prepare()

        // Set controller timeout and initial visibility
        playerView.useController = true

        // Start playback
        player?.playWhenReady = true

        // Set up GestureDetector for double-tap
        playerControlView.let {
            it?.visibility = View.VISIBLE // Initial visibility
            val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!isPipMode) {
                        toggleFullscreen()
                    }
                    return true
                }

                // Handle single tap to show/hide controller (Unstable)
                @OptIn(UnstableApi::class)
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (!isPipMode) {
                        if (playerView.isControllerFullyVisible) {
                            playerView.hideController()
                        } else {
                            playerView.showController()
                        }
                    }
                    return true
                }
            })

            playerView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }

        // Error handling
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("Retroclub", "Error playing media: ${error.message}")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY, Player.STATE_BUFFERING -> {
                        // Keep screen on during playback or buffering
                        playerView.keepScreenOn = true
                    }
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        // Allow screen timeout when playback ends or is idle
                        playerView.keepScreenOn = false
                    }
                }
            }
        })
    }

    private fun loadMediaToCast() {
        remoteMediaClient?.let { client ->
            val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                putString(MediaMetadata.KEY_TITLE, "Retroclub")
            }
            val mediaInfo = MediaInfo.Builder(VIDEO_URL)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType("application/x-mpegURL")
                .setMetadata(metadata)
                .build()

            val requestData = MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build()

            client.load(requestData).setResultCallback { result ->
                if (result.status.isSuccess) {
                    player?.pause()
                    playerView.findViewById<ImageButton>(R.id.custom_chromecast)
                        ?.setImageResource(R.drawable.ic_chromecast_shrink)
                } else {
                    Log.e("RetroClub", "Failed to load stream to Chromecast: ${result.status.statusMessage}")
                }
            }
        }
    }

    private fun resumeLocalPlayback() {
        playerView.visibility = View.VISIBLE
        player?.playWhenReady = true
        playerView.findViewById<ImageButton>(R.id.custom_chromecast)?.setImageResource(R.drawable.ic_chromecast_expand)
    }

    private fun togglePipMode() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (!isPipMode) {
                // Enter PiP mode
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
                isPipMode = true
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            // Entering PiP mode
            wasPlayingBeforePip = player?.isPlaying == true

            // Hide UI elements in PiP mode
            headerImageView.visibility = View.GONE
            mediaListComposeView.visibility = View.GONE
            whatsappButton.visibility = View.GONE
            playerView.useController = false

            // Keep screen on in PiP mode (optional, as system may manage this)
            playerView.keepScreenOn = true
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // Restore UI when exiting PiP
            headerImageView.visibility = View.VISIBLE
            mediaListComposeView.visibility = View.VISIBLE
            whatsappButton.visibility = View.VISIBLE
            playerView.useController = true

            // IMPORTANT: Pause audio/video when PiP is closed
            player?.pause()
            Log.d("Retroclub", "PiP mode exited - pausing playback")

            (playerView as? AspectRatioPlayerView)?.setPipMode(false)

            // Handle playback based on activity state
            if (isActivityStopped) {
                // Activity was stopped (PiP window was closed) - stop playback
                player?.pause()
                Log.d("Retroclub", "PiP window closed - stopping playback")
            } else {
                // Activity is still active (PiP was maximized) - resume if was playing
                if (wasPlayingBeforePip) {
                    player?.play()
                    Log.d("Retroclub", "PiP maximized - resuming playback")
                }
            }

            // Update screen timeout based on playback state
            playerView.keepScreenOn = player?.isPlaying == true
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Enter PiP mode when user presses home or recent apps
        if (!isPipMode) {
            // Return back to portrait
            if (isFullscreen) toggleFullscreen()

            whatsappButton.visibility = View.GONE
            headerImageView.visibility = View.GONE
            mediaListComposeView.visibility = View.GONE
            playerView.useController = false
            (playerView as? AspectRatioPlayerView)?.setPipMode(true)
            togglePipMode()
        }
    }

    override fun onStop() {
        super.onStop()
        isActivityStopped = true
        // Only pause if not in PiP mode
        if (!isPipMode) {
            player?.pause()
        }
        // If in PiP mode, let playback continue
    }

    override fun onStart() {
        super.onStart()
        isActivityStopped = false
    }

    override fun onResume() {
        super.onResume()
        isActivityStopped = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release() // Clean up player
        player = null
        httpClient.dispatcher.executorService.shutdown()
    }


}*/