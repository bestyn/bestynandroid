package com.gbksoft.neighbourhood.ui.fragments.stories.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatCheckedTextView
import com.gbksoft.neighbourhood.R


class StrokeTextView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatCheckedTextView(context, attrs, defStyleAttr) {

    private val defaultStrokeWidth = 0F

    private var strokeColor: Int = 0
    private var strokeWidth: Float = 0.toFloat()

    private var outlineSpan: OutlineSpan? = null

    init {
        initResources(attrs)
        outlineSpan = OutlineSpan(strokeColor, strokeWidth)
        if (!text.isNullOrEmpty()) refresh()
    }

    private fun initResources(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context?.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
            strokeColor = a!!.getColor(R.styleable.StrokeTextView_stv_strokeColor,
                currentTextColor)
            strokeWidth = a.getDimension(R.styleable.StrokeTextView_stv_strokeWidth,
                defaultStrokeWidth)

            a.recycle()
        } else {
            strokeColor = currentTextColor
            strokeWidth = defaultStrokeWidth
        }
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
        refresh()
    }

    fun setStrokeWidth(width: Float) {
        setStrokeWidth(TypedValue.COMPLEX_UNIT_SP, width)
    }

    fun setStrokeWidth(unit: Int, width: Float) {
        strokeWidth = TypedValue.applyDimension(
            unit, width, context.resources.displayMetrics)
        refresh()
    }

    private fun refresh() {
        super.setText(text)
    }

    override fun setText(charSequence: CharSequence?, type: BufferType?) {
        outlineSpan?.let { outlineSpan ->
            val text = charSequence ?: ""
            outlineSpan.strokeColor = strokeColor
            outlineSpan.strokeWidth = strokeWidth
            val spannable = SpannableString(text)
            spannable.setSpan(outlineSpan, 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            super.setText(spannable, BufferType.SPANNABLE)
        } ?: run {
            super.setText(charSequence, type)
        }
    }

    inner class OutlineSpan(
        @ColorInt var strokeColor: Int,
        @Dimension var strokeWidth: Float
    ) : ReplacementSpan() {

        override fun getSize(
            paint: Paint,
            text: CharSequence,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
        ): Int {
            return paint.measureText(text.toString().substring(start until end)).toInt() +
                strokeWidth.toInt() * 2
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            val originTextColor = paint.color

            canvas.save()
            canvas.translate(strokeWidth, 0f)
            paint.apply {
                color = strokeColor
                style = Paint.Style.STROKE
                this.strokeWidth = this@OutlineSpan.strokeWidth
            }
            canvas.drawText(text, start, end, x, y.toFloat(), paint)

            paint.apply {
                color = originTextColor
                style = Paint.Style.FILL
            }
            canvas.drawText(text, start, end, x, y.toFloat(), paint)
            canvas.restore()
        }

    }
}
