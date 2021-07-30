package com.gbksoft.neighbourhood.ui.widgets.stories.record_button

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.gbksoft.neighbourhood.R
import kotlinx.android.synthetic.main.layout_video_record_button.view.*


class VideoRecordButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val size = resources.getDimensionPixelSize(R.dimen.create_story_record_btn_size)
    private val activeAnimDrawable: AnimatedVectorDrawable?
    private val inactiveAnimDrawable: AnimatedVectorDrawable?
    private var strokeAnimation: StrokeAnimation? = null
    private var onClickListener: OnClickListener? = null
    private var prevState: Boolean? = null
    var isRecordingState = false
        private set


    init {
        inflate(context, R.layout.layout_video_record_button, this)
        activeAnimDrawable = AppCompatResources.getDrawable(context,
            R.drawable.anim_vector_record_btn_active)
            as? AnimatedVectorDrawable
        inactiveAnimDrawable = AppCompatResources.getDrawable(context,
            R.drawable.anim_vector_record_btn_inactive)
            as? AnimatedVectorDrawable
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        isClickable = true
        super.setOnClickListener(this)
        setupView()
    }

    private fun setupView() {
        activeAnimDrawable?.let {
            vrb_recordDot.background = it
        }
        strokeAnimation = StrokeAnimation(
            vrb_recordRing,
            R.color.create_story_record_btn_stroke_color,
            R.dimen.create_story_record_btn_stroke_width_from,
            R.dimen.create_story_record_btn_stroke_width_to
        ).setDuration(800)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        val height = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(width, height)
    }

    override fun onClick(v: View?) {
        onClickListener?.onClick(this)
    }

    fun setState(isRecording: Boolean) {
        isRecordingState = isRecording
        updateUI()
    }

    private fun updateUI() {
        if (prevState == isRecordingState) return
        prevState = isRecordingState

        if (isRecordingState) {
            activeAnimDrawable?.let {
                vrb_recordDot.background = it
                it.start()
            }
            strokeAnimation?.start()
        } else {
            inactiveAnimDrawable?.let {
                vrb_recordDot.background = it
                it.start()
            }
            strokeAnimation?.stop()
        }
    }

}