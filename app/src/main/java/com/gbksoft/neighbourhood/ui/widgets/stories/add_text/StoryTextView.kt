package com.gbksoft.neighbourhood.ui.widgets.stories.add_text

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class StoryTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatTextView(context, attrs, defStyleAttr), View.OnTouchListener {

    private val CLICK_ACTION_DURATION = 100
    private val LONG_CLICK_ACTION_DURATION = 250L

    var lastEvent: FloatArray? = null
    var d = 0f
    var newRot = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    private val start: PointF = PointF()
    private val mid: PointF = PointF()
    var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f

    private var startX = 0f
    private var startY = 0f

    private var curX = 0f
    private var curY = 0f

    var enableMoving = true

    private val longClickRunnable = Runnable {
        if (abs(startX - curX) < 10 && abs(startY - curY) < 10) {
            performLongClick()
        }
    }

    var curWidth = 0
    var curHeight = 0

    init {
        setOnTouchListener(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        curHeight = (height * scaleY).toInt()
        curWidth = (width * scaleX).toInt()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        viewTransformation(event)
        return true
    }

    private fun viewTransformation(event: MotionEvent) {
        if (!enableMoving) {
            return
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY

                xCoOrdinate = x - event.rawX
                yCoOrdinate = y - event.rawY
                start[event.rawX] = event.rawY
                isOutSide = false
                mode = DRAG
                lastEvent = null

                handler.postDelayed(longClickRunnable, LONG_CLICK_ACTION_DURATION)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                handler.removeCallbacks(longClickRunnable)
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)

            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longClickRunnable)
                val duration = event.eventTime - event.downTime
                if (duration < CLICK_ACTION_DURATION) {
                    performClick()
                }
                isZoomAndRotate = false
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                curX = event.rawX
                curY = event.rawY
                if (mode == DRAG) {
                    isZoomAndRotate = false
                    animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate).setDuration(0).start()
                }
                if (mode == ZOOM && event.pointerCount == 2) {
                    val newDist1 = spacing(event)
                    if (newDist1 > 10f) {
                        val scale = newDist1 / oldDist * scaleX
                        scaleX = scale
                        scaleY = scale

                        curHeight = (height * scaleY).toInt()
                        curWidth = (width * scaleX).toInt()

                    }
                    if (lastEvent != null) {
                        newRot = rotation(event)
                        rotation = rotation + newRot - d
                    }
                }
            }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x: Double = event.getX(0) - event.getX(1).toDouble()
        val delta_y: Double = event.getY(0) - event.getY(1).toDouble()
        val radians = atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = (event.getX(0) - event.getX(1)).toDouble()
        val y = (event.getY(0) - event.getY(1)).toDouble()
        return sqrt(x * x + y * y).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x: Float = event.getX(0) + event.getX(1)
        val y: Float = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }
}
