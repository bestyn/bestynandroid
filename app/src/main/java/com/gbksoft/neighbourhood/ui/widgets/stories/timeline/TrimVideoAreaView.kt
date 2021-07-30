package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gbksoft.neighbourhood.R
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

private const val UNDEFINED_THUMB_POS = -1f
private const val MIN_AREA_TIME_MS = 1000
private const val MAX_AREA_TIME_MS = 60 * 1000

class TrimVideoAreaView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val trimContainer by lazy { findViewById<FrameLayout>(R.id.trim_container) }
    private val trimArea by lazy { findViewById<View>(R.id.trim_area) }
    private val leftThumb by lazy { findViewById<TimelineThumb>(R.id.trim_area_left_thumb) }
    private val rightThumb by lazy { findViewById<TimelineThumb>(R.id.trim_area_right_thumb) }

    var timelineAreaProvider: TimelineAreaProvider? = null

    private var inflated = false
    private var leftThumbX = UNDEFINED_THUMB_POS
    private var rightThumbX = UNDEFINED_THUMB_POS
    private var areaMinWidth: Float = 0f
    private var areaMaxWidth: Float = 0f

    init {
        inflate(context, R.layout.layout_trim_video_area, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflated = true

        leftThumb.onXPosChangedListener = ::onLeftThumbPositionChanged
        rightThumb.onXPosChangedListener = ::onRightThumbPositionChanged
        leftThumb.timelineAreaProvider = LeftThumbAreaProvider()
        rightThumb.timelineAreaProvider = RightThumbAreaProvider()
    }


    private fun onLeftThumbPositionChanged(x: Float) {
        leftThumbX = x
        Timber.tag("TrimAreaView").d("leftThumbX = $x")
        post { updateTrimArea() }
    }

    private fun onRightThumbPositionChanged(x: Float) {
        rightThumbX = x
        Timber.tag("TrimAreaView").d("rightThumbX = $x")
        post { updateTrimArea() }
    }

    private fun updateTrimArea() {
        if (leftThumbX == UNDEFINED_THUMB_POS || rightThumbX == UNDEFINED_THUMB_POS) return

        val x = leftThumbX + leftThumb.widthWithoutPadding - 1
        val width = (rightThumbX - x).toInt() + 2
        trimArea.x = x
        trimArea.layoutParams.width = width
        trimArea.requestLayout()
    }

    fun setup(thumbPositionConverter: ThumbPositionConverter, leftTrimPositionMills: Int = -1, rightTrimPositionMills: Int = -1) {
        areaMinWidth = thumbPositionConverter.timeToWidth(MIN_AREA_TIME_MS)
        areaMaxWidth = thumbPositionConverter.timeToWidth(MAX_AREA_TIME_MS)
        val leftThumbPosition = if (leftTrimPositionMills == -1) {
            0f
        } else {
            thumbPositionConverter.getPosition(leftTrimPositionMills)
        }
        val rightThumbPosition = if (rightTrimPositionMills == -1) {
            min(x + width, leftThumbX + leftThumb.widthWithoutPadding + areaMaxWidth.toInt() + rightThumb.widthWithoutPadding + 1)
        } else {
            thumbPositionConverter.getPosition(rightTrimPositionMills)
        }

        leftThumb.setPosition(leftThumbPosition, true)
        rightThumb.setPosition(rightThumbPosition, true)
        leftThumb.visibility = View.VISIBLE
        rightThumb.visibility = View.VISIBLE
        trimArea.visibility = View.VISIBLE
    }

    fun getLeftTrimPosition() = leftThumbX
    fun getRightTrimPosition() = rightThumbX - leftThumb.widthWithoutPadding

    override fun addView(child: View?) {
        if (isOwnView(child)) super.addView(child)
        else trimContainer.addView(child)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        if (isOwnView(child)) super.addView(child, params)
        else trimContainer.addView(child, params)
    }

    override fun addView(child: View?, index: Int) {
        if (isOwnView(child)) super.addView(child, index)
        else trimContainer.addView(child, index)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (isOwnView(child)) super.addView(child, index, params)
        else trimContainer.addView(child, index, params)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        if (isOwnView(child)) super.addView(child, width, height)
        else trimContainer.addView(child, width, height)
    }

    private fun isOwnView(child: View?): Boolean {
        val id = child?.id ?: return false
        return id == R.id.trim_container ||
                id == R.id.trim_area ||
                id == R.id.trim_area_left_thumb ||
                id == R.id.trim_area_right_thumb
    }

    inner class LeftThumbAreaProvider : TimelineAreaProvider {
        override fun getThumbLeftBarrier(): Float {
            return max(x, rightThumbX - areaMaxWidth)
        }

        override fun getThumbRightBarrier(): Float {
            return rightThumbX - areaMinWidth
        }
    }

    inner class RightThumbAreaProvider : TimelineAreaProvider {
        override fun getThumbLeftBarrier(): Float {
            return leftThumbX + leftThumb.widthWithoutPadding + areaMinWidth
        }

        override fun getThumbRightBarrier(): Float {
            return  min(x + width, leftThumbX + leftThumb.widthWithoutPadding + areaMaxWidth.toInt() + rightThumb.widthWithoutPadding + 1)
        }
    }
}