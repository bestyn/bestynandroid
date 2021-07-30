package com.gbksoft.neighbourhood.ui.widgets.chat.input_field.audio

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

class AudioRecordButton
@JvmOverloads
constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    //return is record started
    var onActionDownListener: (() -> Boolean)? = null
    var onActionUpListener: (() -> Unit)? = null
    var onActionCancelListener: (() -> Unit)? = null
    var onPositionXChangedListener: ((x: Float) -> Unit)? = null
    var cancelBarrierXProvider: (() -> Float)? = null

    private var isMovementAvailable = false
    private var originRecordButtonX = 0f
    private var downX = 0f
    private var cancelRecordBarrierX = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMovementAvailable = onActionDownListener?.invoke() ?: false
                originRecordButtonX = x
                downX = event.x
                cancelRecordBarrierX = cancelBarrierXProvider?.invoke() ?: 0f
                true
            }
            MotionEvent.ACTION_UP -> {
                onActionUpListener?.invoke()
                moveRecordButton(this, originRecordButtonX)
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                onActionCancel()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isMovementAvailable) return false

                val leftX = event.rawX - (downX)
                if (leftX <= cancelRecordBarrierX) {
                    onActionCancel()
                    return false
                }
                if (leftX < originRecordButtonX) {
                    moveRecordButton(this, leftX)
                }
                true
            }
            else -> false
        }
    }

    private fun onActionCancel() {
        onActionCancelListener?.invoke()
        moveRecordButton(this, originRecordButtonX)
    }

    private fun moveRecordButton(view: View, x: Float) {
        view.animate()
            .x(x)
            .setDuration(0)
            .start()
        view.invalidate()
        onPositionXChangedListener?.invoke(x)
    }

}