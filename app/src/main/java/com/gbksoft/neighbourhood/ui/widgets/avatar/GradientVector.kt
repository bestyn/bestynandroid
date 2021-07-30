package com.gbksoft.neighbourhood.ui.widgets.avatar

data class GradientVector(
    val x0: Int,
    val y0: Int,
    val x1: Int,
    val y1: Int
) {
    val x0f = x0.toFloat()
    val y0f = y0.toFloat()
    val x1f = x1.toFloat()
    val y1f = y1.toFloat()
}