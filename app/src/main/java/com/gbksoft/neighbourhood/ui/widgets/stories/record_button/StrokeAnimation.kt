package com.gbksoft.neighbourhood.ui.widgets.stories.record_button

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources

class StrokeAnimation(
    private val view: View,
    @ColorRes
    strokeColor: Int,
    @DimenRes
    fromValue: Int,
    @DimenRes
    toValue: Int
) {
    private val color = AppCompatResources.getColorStateList(view.context, strokeColor)
    private val from = view.resources.getDimensionPixelSize(fromValue)
    private val to = view.resources.getDimensionPixelSize(toValue)

    private val gradientDrawable: GradientDrawable? = view.background as? GradientDrawable
    private val valueAnimator: ValueAnimator?

    init {
        valueAnimator = if (gradientDrawable != null) {
            ValueAnimator.ofInt(from, to).apply {
                addUpdateListener {
                    gradientDrawable.setStroke(it.animatedValue as Int, color)
                    view.invalidate()
                }
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
            }
        } else {
            null
        }
    }

    fun setDuration(duration: Long): StrokeAnimation {
        valueAnimator?.duration = duration
        return this
    }

    fun start() {
        valueAnimator?.start()
    }

    fun stop() {
        valueAnimator?.cancel()
        gradientDrawable?.setStroke(from, color)
        view.invalidate()
    }
}