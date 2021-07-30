package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan

class BackgroundColorSpan(backgroundColor: Int,
                          private val padding: Int,
                          private val radius: Float) : LineBackgroundSpan {
    private val rect = RectF()
    private val paint = Paint()
    private val paintStroke = Paint()

    companion object {

        val ALIGN_CENTER = 0
        val ALIGN_START = 1
        val ALIGN_END = 2
    }

    init {
        paint.color = backgroundColor
        paintStroke.color = backgroundColor
    }

    private var align = ALIGN_CENTER

    fun setAlignment(alignment: Int) {
        align = alignment
    }

    override fun drawBackground(
            c: Canvas,
            p: Paint,
            left: Int,
            right: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            lnum: Int) {


        val txt = text.subSequence(start, end).trim();
        val newLen = txt.length
        val width = p.measureText(txt, 0, newLen) + 2f * padding
        val shiftLeft: Float
        val shiftRight: Float

        when (align) {
            ALIGN_START -> {
                shiftLeft = 0f - padding
                shiftRight = width + shiftLeft
            }

            ALIGN_END -> {
                shiftLeft = right - width + padding
                shiftRight = (right + padding).toFloat()
            }
            else -> {
                shiftLeft = (right - width) / 2
                shiftRight = right - shiftLeft
            }
        }

        rect.set(shiftLeft, top.toFloat(), shiftRight, bottom.toFloat())
        c.drawRoundRect(rect, radius, radius, paint)
    }
}