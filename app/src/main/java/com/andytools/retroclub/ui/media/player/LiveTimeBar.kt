package com.andytools.retroclub.ui.media.player

import android.content.Context
import android.util.AttributeSet
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import com.andytools.retroclub.common.utils.Logger

@UnstableApi
class LiveTimeBar @UnstableApi
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DefaultTimeBar(context, attrs, defStyleAttr) {
    
    private val liveWindowDurationMs = 30 * 60 * 1000L // 30 minutes
    private var virtualStartTime = System.currentTimeMillis()
    private var lastRealPosition = 0L
    private var virtualPosition = 0L
    private var realDuration = 0L
    private var player: Player? = null
    
    override fun setDuration(durationMs: Long) {
        realDuration = durationMs
        // Always set duration to our 30-minute window
        super.setDuration(liveWindowDurationMs)
    }

    @UnstableApi
    override fun setPosition(positionMs: Long) {
        val currentTime = System.currentTimeMillis()
        
        // If the real position went backwards (HLS restart), maintain virtual position
        if (positionMs < lastRealPosition && lastRealPosition > 10000) { // 10 second threshold
            Logger.d("HLS restart detected: real position $positionMs < last $lastRealPosition")
            // Continue virtual position without resetting
            val timeDiff = currentTime - virtualStartTime
            virtualPosition = minOf(timeDiff, liveWindowDurationMs)
        } else {
            // Normal progression - update virtual position based on real time
            val timeDiff = currentTime - virtualStartTime
            virtualPosition = minOf(timeDiff, liveWindowDurationMs)
            
            // If we've reached the 30-minute window, start sliding
            if (virtualPosition >= liveWindowDurationMs) {
                virtualStartTime = currentTime - liveWindowDurationMs
                virtualPosition = liveWindowDurationMs
            }
        }
        
        lastRealPosition = positionMs
        super.setPosition(virtualPosition)
    }
    
    override fun setBufferedPosition(bufferedPositionMs: Long) {
        // Map buffered position to our virtual timeline
        val bufferedInWindow = minOf(virtualPosition + (bufferedPositionMs - lastRealPosition), liveWindowDurationMs)
        super.setBufferedPosition(maxOf(bufferedInWindow, 0L))
    }
    
    fun setPlayer(player: Player) {
        this.player = player
    }
    
    private fun mapVirtualToRealPosition(virtualPos: Long): Long {
        if (player == null) return 0L
        
        val currentRealPos = player!!.currentPosition
        val bufferedPosition = player!!.bufferedPosition
        val duration = player!!.duration
        
        Logger.d("Seeking debug: virtual=$virtualPos, virtualPosition=$virtualPosition, current=$currentRealPos, buffered=$bufferedPosition")
        
        // For live streams, use a simpler approach
        if (duration == C.TIME_UNSET) {
            // Calculate how far back from current position we want to seek
            val virtualSeekBack = virtualPosition - virtualPos
            val maxSeekBack = bufferedPosition - currentRealPos
            
            Logger.d("Live stream seek: virtualSeekBack=$virtualSeekBack, maxSeekBack=$maxSeekBack")
            
            if (virtualSeekBack <= 0) {
                // Seeking to current or future, return current position
                return currentRealPos
            }
            
            // Limit the seek back to what's actually buffered, but make it more aggressive
            val actualSeekBack = minOf(virtualSeekBack, maxOf(0L, (maxSeekBack * 0.8f).toLong()))
            val targetPosition = maxOf(0L, currentRealPos - actualSeekBack)
            
            Logger.d("Mapped to: target=$targetPosition, seekBack=$actualSeekBack")
            return targetPosition
        } else {
            // VOD - can seek anywhere
            val virtualPercent = virtualPos.toFloat() / liveWindowDurationMs.toFloat()
            val targetPosition = (duration * virtualPercent).toLong()
            Logger.d("VOD seek: ${String.format("%.1f", virtualPercent * 100)}% = $targetPosition")
            return maxOf(0L, minOf(duration, targetPosition))
        }
    }

    init {
        // Set a custom scrub listener to handle seeking
        addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                val realPosition = mapVirtualToRealPosition(position)
                Logger.d("Seeking start: virtual=$position, mapped real=$realPosition")
                player?.seekTo(realPosition)
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                val realPosition = mapVirtualToRealPosition(position)
                player?.seekTo(realPosition)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                if (!canceled) {
                    val realPosition = mapVirtualToRealPosition(position)
                    Logger.d("Seek completed: virtual=$position, final real=$realPosition")
                    player?.seekTo(realPosition)
                }
            }
        })
    }
    
    fun resetVirtualTimer() {
        virtualStartTime = System.currentTimeMillis()
        virtualPosition = 0L
        lastRealPosition = 0L
    }
}