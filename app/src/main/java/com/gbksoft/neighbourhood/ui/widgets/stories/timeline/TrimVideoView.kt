package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.LongSparseArray
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.gbksoft.neighbourhood.R

class TrimVideoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), TimelineAreaProvider {

    val timelineView by lazy { findViewById<FrameLineView>(R.id.trim_video_timeline) }
    private val trimArea by lazy { findViewById<TrimVideoAreaView>(R.id.trim_area_view) }
    private val thumb by lazy { findViewById<TimelineThumb>(R.id.trim_video_thumb) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.trim_video_progress_bar) }
    private val thumbPositionConverter = ThumbPositionConverter(this)

    var onTimeChangedListener: ((timeInMs: Int) -> Unit)? = null
    var onSizeChanged: ((Int, Int) -> Unit)? = null

    init {
        inflate(context, R.layout.layout_trim_video, this)
    }

    fun setBitmaps(bitmaps: LongSparseArray<Bitmap>) {
        timelineView.setBitmaps(bitmaps)
    }

    fun setDuration(duration: Int, startTime: Int = -1, endTime: Int = -1) {
        thumbPositionConverter.videoLengthInMs = duration
        post {
            trimArea.setup(thumbPositionConverter, startTime, endTime)
        }
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            timelineView.visibility = View.GONE
            thumb.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            timelineView.visibility = View.VISIBLE
            thumb.visibility = View.VISIBLE
        }
        invalidate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        timelineView.onSizeChanged = { w, h ->
            onSizeChanged?.invoke(w, h)
        }
        timelineView.onFrameLineBuildFinished = {
            thumb.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
        thumb.timelineAreaProvider = this
        trimArea.timelineAreaProvider = this
        thumb.onXPosChangedListener = ::onThumbPositionChanged
    }

    fun getLeftTrim(): Int {
        val leftPosition = trimArea.getLeftTrimPosition()
        return thumbPositionConverter.getTime(leftPosition)
    }

    fun getRightTrim(): Int {
        val rightPosition = trimArea.getRightTrimPosition()
        return thumbPositionConverter.getTime(rightPosition)
    }

    fun getLeftTrimPos(): Float {
        return trimArea.getLeftTrimPosition()
    }

    fun getRightTrimPos(): Float {
        return trimArea.getRightTrimPosition()
    }

    fun setThumbTime(timeMillis: Int) {
        val thumbPosition = thumbPositionConverter.getPosition(timeMillis)
        thumb.setPosition(thumbPosition, false)
    }

    fun getTimeLineThumb(): TimelineThumb {
        return thumb
    }

    override fun getThumbLeftBarrier(): Float {
        return timelineView.x
    }

    override fun getThumbRightBarrier(): Float {
        return timelineView.x + timelineView.width
    }

    private fun onThumbPositionChanged(xPos: Float) {
        val selectedFrameTime = thumbPositionConverter.getTime(xPos)
        onTimeChangedListener?.invoke(selectedFrameTime)
    }
}