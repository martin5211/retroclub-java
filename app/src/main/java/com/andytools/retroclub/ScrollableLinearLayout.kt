package com.andytools.retroclub

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.LinearLayout
import android.widget.OverScroller
import kotlin.math.abs

class ScrollableLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val scroller = OverScroller(context)
    private var lastY = 0f
    private var isScrolling = false
    private var isScrollEnabled = true
    private var maxScrollY = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Calculate maximum scroll distance after measurement
        post {
            maxScrollY = computeVerticalScrollRange() - height
            if (maxScrollY < 0) maxScrollY = 0
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isScrollEnabled) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                isScrolling = true
                // Stop any ongoing fling
                if (!scroller.isFinished) scroller.abortAnimation()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = lastY - event.y
                lastY = event.y
                scrollBy(0, deltaY.toInt())
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScrolling = false
                parent.requestDisallowInterceptTouchEvent(false)
                // Start fling
                val velocityTracker = VelocityTracker.obtain()
                velocityTracker.addMovement(event)
                velocityTracker.computeCurrentVelocity(1000)
                val yVelocity = velocityTracker.getYVelocity().toInt()
                velocityTracker.recycle()
                if (abs(yVelocity) > 200) {
                    scroller.fling(
                        scrollX, scrollY,
                        0, -yVelocity,
                        0, 0,
                        0, maxScrollY
                    )
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        // Clamp scroll position to valid bounds
        val clampedY = y.coerceIn(0, maxScrollY)
        super.scrollTo(x, clampedY)
    }

    fun setScrollEnabled(enabled: Boolean) {
        isScrollEnabled = enabled
        if (!enabled) {
            // Reset scroll position when disabled
            scrollTo(0, 0)
            scroller.abortAnimation()
        }
    }

    override fun performClick(): Boolean {
        // Required for accessibility
        return super.performClick()
    }
}