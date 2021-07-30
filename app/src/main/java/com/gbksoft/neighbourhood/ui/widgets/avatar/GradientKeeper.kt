package com.gbksoft.neighbourhood.ui.widgets.avatar

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.annotation.ColorInt

class GradientKeeper {
    private var gradientShader: Shader? = null
    private var lastGradientVector: GradientVector? = null
    private var gradientUpdated = false

    @ColorInt
    var startColor: Int? = null
        set(value) {
            gradientUpdated = true
            field = value
        }

    @ColorInt
    var endColor: Int? = null
        set(value) {
            gradientUpdated = true
            field = value
        }

    @ColorInt
    var centerColors: List<Int>? = null
        set(value) {
            gradientUpdated = true
            field = value
        }

    fun containsGradient(): Boolean {
        return startColor != null && endColor != null
    }

    fun getShader(gradientVector: GradientVector): Shader {
        val start = startColor
        val end = endColor
        val center = centerColors

        if (start == null || end == null) {
            throw IllegalStateException("gradientStart and gradientEnd cannot be null")
        }

        if (gradientVector != lastGradientVector) gradientUpdated = true
        lastGradientVector = gradientVector

        gradientShader?.let {
            if (!gradientUpdated) return it
        }

        val shader = createShader(start, center, end, gradientVector)
        gradientShader = shader
        return shader
    }

    private fun createShader(start: Int, center: List<Int>?, end: Int, vector: GradientVector): Shader {
        return if (center != null) {
            val colors = mutableListOf<Int>()
            colors.add(start)
            colors.addAll(center)
            colors.add(end)
            LinearGradient(vector.x0f, vector.y0f, vector.x1f, vector.y1f,
                colors.toIntArray(), null, Shader.TileMode.CLAMP)
        } else {
            LinearGradient(vector.x0f, vector.y0f, vector.x1f, vector.y1f,
                start, end, Shader.TileMode.CLAMP)
        }
    }
}