package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import timber.log.Timber

class TimelineThumb
@JvmOverloads
constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    var timelineAreaProvider: TimelineAreaProvider? = null
    var onXPosChangedListener: ((x: Float) -> Unit)? = null
    var onRightLimitReached: ((x: Float) -> Unit)? = null
    private var prevTouchX: Float = 0f
    val widthWithoutPadding: Int
        get() {
            return width - paddingRight - paddingLeft
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        prevTouchX = event.rawX
    }

    private fun onActionMove(event: MotionEvent) {
        val thumbAreaProvider = timelineAreaProvider ?: return

        val currentTouchX = event.rawX
        val deltaX = currentTouchX - prevTouchX
        val leftX = x + deltaX

        prevTouchX = currentTouchX

        val rightX = leftX + width
        val leftPosition = validatePosition(
                leftX,
                rightX,
                thumbAreaProvider.getThumbLeftBarrier() - paddingLeft,
                thumbAreaProvider.getThumbRightBarrier() + paddingRight
        )
        move(this, leftPosition, toThumbPosition(leftPosition), true)
    }

    fun setPosition(thumbPosition: Float, invokeXPosChangedListener: Boolean) {
        val viewPosition = toViewPosition(thumbPosition)
        move(this, viewPosition, thumbPosition, invokeXPosChangedListener)
    }


    private fun move(view: View, viewPosition: Float, thumbPosition: Float, invokeXPosChangedListener: Boolean) {
        view.animate()
                .x(viewPosition)
                .setDuration(0)
                .start()
        view.invalidate()
        if (invokeXPosChangedListener) {
            onXPosChangedListener?.invoke(thumbPosition)
        }
    }

    private fun toViewPosition(thumbPosition: Float): Float {
        return thumbPosition - paddingLeft
    }

    private fun toThumbPosition(viewPosition: Float): Float {
        return viewPosition + paddingLeft
    }

    private fun validatePosition(leftX: Float, rightX: Float, minX: Float, maxX: Float): Float {
        return when {
            leftX < minX -> minX
            rightX > maxX -> leftX - (rightX - maxX).also { onRightLimitReached?.invoke(it) }
            else -> leftX
        }
    }
}

