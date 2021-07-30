package com.gbksoft.neighbourhood.ui.widgets.stories.timeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.LongSparseArray
import android.util.TypedValue
import android.view.View
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class FrameLineView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var heightView = 48F.pxValue()
    private var bitmapList: LongSparseArray<Bitmap>? = null

    var onFrameLineBuildFinished: (() -> Unit)? = null
    var onSizeChanged: ((Int, Int) -> Unit)? = null

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.TimelineView, 0, 0)
        try {
            heightView = a.getDimension(R.styleable.TimelineView_videoFrameHeight, heightView)
        } finally {
            a.recycle()
        }
        checkInEditMode()
    }

    fun setBitmaps(bitmaps: LongSparseArray<Bitmap>) {
        bitmapList = bitmaps
        invalidate()
        onFrameLineBuildFinished?.invoke()
    }

    private fun checkInEditMode() {
        if (isInEditMode.not()) return
        bitmapList = LongSparseArray<Bitmap>().apply {
            for (i in 0..10) {
                val bitmap = Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round),
                        heightView.toInt(),
                        heightView.toInt(),
                        false)
                put(i.toLong(), bitmap)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val minH = paddingBottom + paddingTop + heightView
        val h = resolveSizeAndState(minH.toInt(), heightMeasureSpec, 1)
        setMeasuredDimension(w, h)
    }


    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w != oldW) {
            Timber.tag("FrameLineView").d("onSizeChanged")
            onSizeChanged?.invoke(w, h)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmapList = bitmapList ?: return
        canvas.save()
        var x = 0f
        for (i in 0 until bitmapList.size()) {
            val bitmap = bitmapList.get(i.toLong())
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, x, 0f, null)
                x += bitmap.width
            }
        }
    }

    fun Float.pxValue(unit: Int = TypedValue.COMPLEX_UNIT_DIP): Float {
        return TypedValue.applyDimension(unit, this, resources.displayMetrics)
    }
}