package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.LongSparseArray
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import timber.log.Timber

class SelectCoverView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), TimelineAreaProvider {
    companion object {
        const val NOTHING_SELECTED_TIME = -1
    }

    val timelineView by lazy { findViewById<FrameLineView>(R.id.select_cover_timeline) }
    private val thumb by lazy { findViewById<TimelineThumb>(R.id.select_cover_thumb) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.select_cover_progress_bar) }

    private val thumbPositionConverter = ThumbPositionConverter(this)
    private var selectedFrameTime = NOTHING_SELECTED_TIME


    var onTimeChangedListener: ((timeInMs: Int) -> Unit)? = null
    var onSizeChanged: ((Int, Int) -> Unit)? = null

    init {
        inflate(context, R.layout.layout_select_cover, this)
    }

    fun setBitmaps(bitmaps: LongSparseArray<Bitmap>) {
        timelineView.setBitmaps(bitmaps)
    }

    fun setDuration(duration: Int, currentCoverTimestamp: Int) {
        thumbPositionConverter.videoLengthInMs = duration
        val thumbPosition = thumbPositionConverter.getPosition(currentCoverTimestamp)
        post {
            thumb.setPosition(thumbPosition, true)
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
        thumb.onXPosChangedListener = ::onThumbPositionChanged
    }

    fun getSelectedFrameTime(): Int {
        return selectedFrameTime
    }

    override fun getThumbLeftBarrier(): Float {
        return timelineView.x
    }

    override fun getThumbRightBarrier(): Float {
        return timelineView.x + timelineView.width
    }

    private fun onThumbPositionChanged(xPos: Float) {
        selectedFrameTime = thumbPositionConverter.getTime(xPos)
        onTimeChangedListener?.invoke(selectedFrameTime)
    }
}