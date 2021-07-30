package com.gbksoft.neighbourhood.ui.widgets.stories.audio

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutTrimAudioBinding
import com.gbksoft.neighbourhood.ui.widgets.stories.timeline.ThumbPositionConverter
import com.gbksoft.neighbourhood.ui.widgets.stories.timeline.TimelineAreaProvider
import com.gbksoft.neighbourhood.utils.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val MIN_DURATION = 5_000

class TrimAudioView @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes), TimelineAreaProvider {

    private val layout: LayoutTrimAudioBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.layout_trim_audio, this, true)
    private val thumbPositionConverter = ThumbPositionConverter(this)
    private var totalDurationMs: Int? = null

    private var audioDuration: Int? = null

    var startPos = 0f

    var onTimeChangedListener: ((timeInMs: Int) -> Unit)? = null

    init {
        startPos = layout.trimVideoThumb.x
    }


    fun setAudioDuration(audioDurationMs: Int) {
        Log.d("sdvsvsvs", "audioDuration $audioDuration")
        when (audioDurationMs) {
            0 -> {
                layout.tvDuration.text = getDurationStr(1)
            }
            -1 -> {
                layout.tvDuration.text = getDurationStr(audioDuration ?: 1)
            }
            else -> {
                setDuration(audioDurationMs)
                audioDuration = audioDurationMs
            }
        }
    }

    fun setAudioUri(audioUri: Uri) {
        val audioMetaDataRetriever = MediaMetadataRetriever().apply { setDataSource(context, audioUri) }
        val audioDurationMs = audioMetaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
        setDuration(audioDurationMs)
    }

    fun resetTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            layout.tvCurTime.text = getDurationStr(0)
            layout.tvCurTime.setPosition(0f)
            setPlayingProgress(0)
            layout.trimVideoThumb.setPosition(thumbPositionConverter.getPosition(0), false)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupView()
    }

    private fun setupView() {
        layout.trimVideoThumb.timelineAreaProvider = this
        layout.trimVideoThumb.onXPosChangedListener = ::onThumbPositionChanged
        layout.trimVideoThumb.onRightLimitReached = { onRightLimitReached() }
        layout.tvCurTime.setPosition(getThumbLeftBarrier() - layout.tvCurTime.width / 2)
    }

    fun setPlayingProgress(currentMs: Long) {
        val totalDurationMs = this.totalDurationMs ?: return
        (layout.ivPlayedLevels.drawable as? ClipDrawable)?.let { clipDrawable ->
            val currentProgress = (10000.toFloat() / totalDurationMs * currentMs).toInt()
            ObjectAnimator.ofInt(clipDrawable, "level", currentProgress).apply {
                duration = 16
                start()
            }
        }
    }

    fun skipFirstLevels(durationMs: Int) {
        val totalDurationMs = this.totalDurationMs ?: return
        (layout.ivSkipLevels.drawable as? ClipDrawable)?.let { clipDrawable ->
            val skipLevel = (10000.toFloat() / totalDurationMs * durationMs).toInt()
            clipDrawable.setLevel(skipLevel)
        }
    }

    private fun setDuration(durationMs: Int) {
        this.totalDurationMs = durationMs
        this.thumbPositionConverter.videoLengthInMs = durationMs

        val thumbPosition = thumbPositionConverter.getPosition(0)
        CoroutineScope(Dispatchers.Main).launch {
            //layout.trimVideoThumb.setPosition(thumbPosition, false)
            layout.tvDuration.text = getDurationStr(durationMs)
        }
    }

    private fun getDurationStr(durationMs: Int): String {
        val durationsSec = durationMs / 1000
        val minutes = durationsSec / 60
        val seconds = durationsSec % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun onThumbPositionChanged(xPos: Float) {
        var selectedTime = thumbPositionConverter.getTime(xPos)
        totalDurationMs?.let {
            if (it <= MIN_DURATION) {
                selectedTime = 0
                layout.trimVideoThumb.setPosition(thumbPositionConverter.getPosition(selectedTime), false)
            } else if (totalDurationMs!! - selectedTime < MIN_DURATION) {
                selectedTime = (totalDurationMs!! - MIN_DURATION)
                layout.trimVideoThumb.setPosition(thumbPositionConverter.getPosition(selectedTime), false)
            }

            layout.tvCurTime.text = getDurationStr(selectedTime)
            layout.tvCurTime.setPosition(xPos - layout.tvCurTime.width / 2)
            onTimeChangedListener?.invoke(selectedTime)
        }
    }

    fun hideTimer() {
        layout.tvCurTime.visibility = View.GONE
    }

    private fun onRightLimitReached() {
        ToastUtils.showToastMessage("Your Audio Track must be 5s or more.")
    }

    override fun getThumbLeftBarrier(): Float {
        return layout.ivPlayedLevels.x
    }

    override fun getThumbRightBarrier(): Float {
        return layout.ivPlayedLevels.x + layout.ivPlayedLevels.width
    }
}