package com.gbksoft.neighbourhood.ui.widgets.expandable_text

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView


class LinkHandler(private val longClickEnabled: Boolean = false) : LinkMovementMethod() {
    private val LONG_CLICK_TIME = 500
    private var startTime: Long = 0

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        if (longClickEnabled) {
            return super.onTouchEvent(widget, buffer, event)
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - startTime >= LONG_CLICK_TIME) return true
            }
        }

        return super.onTouchEvent(widget, buffer, event)
    }
}