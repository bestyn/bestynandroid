package com.gbksoft.neighbourhood.ui.widgets.stories.progress_bar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.gbksoft.neighbourhood.R
import kotlin.reflect.KProperty

class RecordProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val minProgressViewHeight by OnValidateProp(5F.pxValue())

    //list should be sorted in increasing order as per markers progress
    private val pauseMarkers: MutableList<RecordPause> by OnLayoutProp(mutableListOf())
    fun addPause(timeInMillis: Int) {
        pauseMarkers.add(RecordPause(timeInMillis))
    }

    fun setPauseMarkers(pauseMarkers: List<RecordPause>?) {
        this.pauseMarkers.clear()
        if (pauseMarkers != null) {
            this.pauseMarkers.addAll(pauseMarkers)
        }
    }

    fun removePause(timeInMillis: Int) {
        pauseMarkers.find { it.progressValue == timeInMillis }?.let { marker ->
            pauseMarkers.remove(marker)
        }
    }

    fun deleteLastSegment() {
        if (pauseMarkers.size < 2) {
            currentProgress = 0
            pauseMarkers.clear()
        } else {
            val newProgress = pauseMarkers[pauseMarkers.size - 2].progressValue
            currentProgress = newProgress
            pauseMarkers.removeAt(pauseMarkers.size - 1)
        }
    }

    fun clearPauseMarkers() {
        pauseMarkers.clear()
    }

    fun pauseMarkers(): List<RecordPause> {
        return pauseMarkers
    }

    var totalProgress: Int by OnValidateProp(184)
    var currentProgress: Int by OnValidateProp(0)
    var pauseMarkerWidth: Float by OnValidateProp(3F.pxValue())
    var rectRadius: Float by OnValidateProp(0F.pxValue())
    var pauseMarkerColor: Int by OnValidateProp(Color.WHITE) {
        paintPauseMarkers.color = pauseMarkerColor
    }
    var progressColor: Int by OnValidateProp(Color.GREEN) {
        paintProgress.color = progressColor
    }
    var progressBackgroundColor: Int by OnValidateProp(Color.GRAY) {
        paintBackground.color = progressBackgroundColor
    }
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = progressBackgroundColor
    }
    private val paintPauseMarkers = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = pauseMarkerColor
    }
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = progressColor
    }

    private val progressBarRect = RectF()

    private val progressTrackPath = Path()

    private var propsInitialisedOnce = false

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.RecordProgressView, 0, 0)

        try {

            currentProgress = a.getInt(R.styleable.RecordProgressView_currentProgress, currentProgress)
            totalProgress = a.getInt(R.styleable.RecordProgressView_totalProgress, totalProgress)

            pauseMarkerWidth = a.getDimension(R.styleable.RecordProgressView_recordPauseMarkerWidth, pauseMarkerWidth)

            progressBackgroundColor = a.getColor(R.styleable.RecordProgressView_progressTrackBackgroundColor,
                    progressBackgroundColor)
            pauseMarkerColor = a.getColor(R.styleable.RecordProgressView_recordPauseMarkerColor, pauseMarkerColor)
            progressColor = a.getColor(R.styleable.RecordProgressView_progressColor, progressColor)
            rectRadius = a.getDimension(R.styleable.RecordProgressView_progressTrackCorners, rectRadius)
        } finally {
            a.recycle()
        }

        propsInitialisedOnce = true
        checkInEditMode()
    }

    private fun checkInEditMode() {
        if (isInEditMode.not()) return
        totalProgress = 100
        currentProgress = 40
        pauseMarkers.add(RecordPause(25))
    }

    override fun onLayout(changed: Boolean, leftP: Int, topP: Int, rightP: Int, bottomP: Int) {
        super.onLayout(changed, leftP, topP, rightP, bottomP)

        progressBarRect.apply {
            left = 0f
            top = 0f
            right = (rightP - leftP).toFloat()
            bottom = (bottomP - topP).toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(minProgressViewHeight.toInt()),
                    MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawProgressTrack(canvas, paintBackground)

        if (currentProgress > 0) {
            calcPauseMarkersPos()
            drawProgress(canvas)
            drawPauseMarkers(canvas)
        }

        canvas.restore()
    }

    private fun calcPauseMarkersPos() {
        val trackWidth = progressBarRect.right - progressBarRect.left
        for (marker in pauseMarkers) {
            val left = (marker.progressValue / totalProgress.toFloat()) * (trackWidth) - pauseMarkerWidth / 2
            val top = progressBarRect.top
            val right = left + pauseMarkerWidth
            val bottom = progressBarRect.bottom
            marker.setPosition(left, top, right, bottom)
        }
    }

    private fun drawProgress(canvas: Canvas) {
        val progressX = (currentProgress / totalProgress.toFloat()) * (progressBarRect.right - progressBarRect.left)
        val lastMarkerIndex = if (pauseMarkers.size > 0) findProgressLastMarker() else -1

        if (lastMarkerIndex == -1) {
            canvas.drawRect(progressBarRect.left, progressBarRect.top, progressX, progressBarRect.bottom, paintProgress)
            return
        }

        var left = progressBarRect.left
        for (i in 0..lastMarkerIndex) {
            val marker = pauseMarkers[i]
            canvas.drawRect(left, progressBarRect.top, marker.left, progressBarRect.bottom, paintProgress)
            left = marker.right
        }
        if (pauseMarkers[lastMarkerIndex].progressValue < currentProgress) {
            canvas.drawRect(left, progressBarRect.top, progressX, progressBarRect.bottom, paintProgress)
        }
    }

    private fun findProgressLastMarker(): Int {
        var index = -1
        for (marker in pauseMarkers) {
            if (currentProgress >= marker.progressValue) index++
            else break
        }
        return index
    }

    private fun drawProgressTrack(canvas: Canvas, paint: Paint) {
        progressTrackPath.reset()
        progressTrackPath.addRoundRect(progressBarRect, rectRadius, rectRadius, Path.Direction.CW)

        canvas.drawPath(progressTrackPath, paint)
        canvas.save()
        canvas.clipPath(progressTrackPath)
    }

    private fun drawPauseMarkers(canvas: Canvas) {
        if (pauseMarkers.size == 0) return
        for (marker in pauseMarkers) {
            if (marker.progressValue in 1..totalProgress) {
                canvas.drawRect(marker.left, marker.top, marker.right, marker.bottom, paintPauseMarkers)
            }
        }
    }


    private fun Float.pxValue(unit: Int = TypedValue.COMPLEX_UNIT_DIP): Float {
        return TypedValue.applyDimension(unit, this, resources.displayMetrics)
    }

    /**
     * Delegate property used to requestLayout on value set after executing a custom function
     */
    inner class OnLayoutProp<T>(private var field: T, private inline var func: () -> Unit = {}) {
        operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T) {
            field = v
            func()
            if (propsInitialisedOnce) {
                requestLayout()

            }

        }

        operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
            return field
        }

    }

    /**
     * Delegate Property used to invalidate on value set after executing a custom function
     */
    inner class OnValidateProp<T>(private var field: T, private inline var func: () -> Unit = {}) {
        operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T) {
            field = v
            func()
            if (propsInitialisedOnce) {
                invalidate()
            }
        }

        operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
            return field
        }
    }

    inner class RecordPause(
        val progressValue: Int
    ) {
        var left: Float = 0f
        var top: Float = 0f
        var right: Float = 0f
        var bottom: Float = 0f

        fun setPosition(left: Float, top: Float, right: Float, bottom: Float) {
            this.left = left
            this.top = top
            this.right = right
            this.bottom = bottom
        }
    }
}