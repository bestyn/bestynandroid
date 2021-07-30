package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.marginLeft

class TrimVideoThumbTime
@JvmOverloads
constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    fun setPosition(thumbPosition: Float) {
        val viewPosition = toViewPosition(thumbPosition)
        move(this, viewPosition)
    }

    private fun move(view: View, viewPosition: Float) {
        view.animate()
                .x(viewPosition)
                .setDuration(0)
                .start()
        view.invalidate()
    }

    private fun toViewPosition(thumbPosition: Float): Float {
        return thumbPosition + marginLeft
    }
}

