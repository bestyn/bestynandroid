package com.gbksoft.neighbourhood.ui.widgets.stories.add_text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar


class VerticalSeekBar : AppCompatSeekBar {

    var onProgressChangeListener: ((Int) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(height, width, oldHeight, oldWidth)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(ROTATION_ANGLE.toFloat())
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> setProgressInternally(max - (max * event.y / height).toInt())
            MotionEvent.ACTION_MOVE -> setProgressInternally(max - (max * event.y / height).toInt())
            MotionEvent.ACTION_UP -> setProgressInternally(max - (max * event.y / height).toInt())
        }
        return true
    }

    fun setProgressInternally(progress: Int) {
        if (progress != getProgress()) {
            super.setProgress(progress)
            onProgressChangeListener?.invoke(progress)
        }
        onSizeChanged(width, height, 0, 0)
    }

    companion object {
        private const val ROTATION_ANGLE = -90
    }
}
