package com.gbksoft.neighbourhood.ui.widgets.avatar

import kotlin.math.abs

class GradientAngle(
    userAngle: Int
) {

    private val step = 45
    private val halfStep = 22.5f
    var angle: Int
        private set
    private var gradientVector: GradientVector? = null

    init {
        angle = calcSuitableAngle(userAngle)
    }

    fun setUserAngle(userAngle: Int) {
        angle = calcSuitableAngle(userAngle)
    }

    private fun calcSuitableAngle(userAngle: Int): Int {
        val positiveUserAngle = if (userAngle < 0) makePositiveAngle(userAngle) else userAngle
        val factor = positiveUserAngle / 360
        val angle = positiveUserAngle - factor * 360
        return round(angle)
    }

    private fun makePositiveAngle(angle: Int): Int {
        if (angle >= 0) return angle

        val factor = abs(angle / 360)
        return 360 + 360 * factor + angle
    }

    private fun round(input: Int): Int {
        println("step: $step   halfStep: $halfStep")
        val stepCount = input / step
        val halfStepCount = ((input - stepCount * step) / halfStep).toInt()
        println("input: $input   stepCount: $stepCount   halfStepCount: $halfStepCount")
        var result = stepCount * step + halfStepCount * step
        if (result == 360) result = 0
        return result
    }

    fun toGradientVector(spaceWidth: Int, spaceHeight: Int): GradientVector {
        return gradientVector ?: run {
            val vector = calcGradientVector(spaceWidth, spaceHeight)
            gradientVector = vector
            vector
        }
    }

    private fun calcGradientVector(width: Int, height: Int): GradientVector {
        return when (angle) {
            0, 360 -> GradientVector(0, height / 2, width, height / 2)
            45 -> GradientVector(0, height, width, 0)
            90 -> GradientVector(width / 2, height, width / 2, 0)
            135 -> GradientVector(width, height, 0, 0)
            180 -> GradientVector(width, height / 2, 0, height / 2)
            225 -> GradientVector(width, 0, 0, height)
            270 -> GradientVector(width / 2, 0, width / 2, height)
            315 -> GradientVector(0, 0, width, height)
            else -> GradientVector(0, height / 2, width, height / 2)
        }
    }
}