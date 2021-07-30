package com.gbksoft.neighbourhood.ui.widgets.stories.record_time

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.story.creating.StoryTime
import kotlinx.android.synthetic.main.layout_record_time_switch.view.*

class RecordTimeSwitch @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var timeSwitchListener: ((storyTime: StoryTime) -> Unit)? = null

    var currentTime = StoryTime.SEC_60
        set(value) {
            field = value
            onTimeChanged()
        }

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.layout_record_time_switch, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setClickListeners()
    }

    private fun setClickListeners() {
        iv15s.setOnClickListener { switchTime(StoryTime.SEC_15) }
        iv30s.setOnClickListener { switchTime(StoryTime.SEC_30) }
        iv45s.setOnClickListener { switchTime(StoryTime.SEC_45) }
        iv60s.setOnClickListener { switchTime(StoryTime.SEC_60) }
    }

    private fun switchTime(time: StoryTime) {
        currentTime = time
    }

    private fun onTimeChanged() {
        checkCurrentTimeButton()
        timeSwitchListener?.invoke(currentTime)
    }

    private fun checkCurrentTimeButton() {
        when (currentTime) {
            StoryTime.SEC_15 -> {
                check(iv15s)
                uncheck(iv30s, iv45s, iv60s)
            }
            StoryTime.SEC_30 -> {
                check(iv30s)
                uncheck(iv15s, iv45s, iv60s)
            }
            StoryTime.SEC_45 -> {
                check(iv45s)
                uncheck(iv15s, iv30s, iv60s)
            }
            StoryTime.SEC_60 -> {
                check(iv60s)
                uncheck(iv15s, iv30s, iv45s)
            }
        }
    }

    private fun check(imageView: ImageView) {
        imageView.setBackgroundResource(R.drawable.ic_duration_active)
    }

    private fun uncheck(vararg imageViews: ImageView) {
        for (imageView in imageViews) {
            imageView.setBackgroundResource(R.drawable.ic_duration_inactive)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        iv15s.isEnabled = enabled
        iv30s.isEnabled = enabled
        iv45s.isEnabled = enabled
        iv60s.isEnabled = enabled
    }
}