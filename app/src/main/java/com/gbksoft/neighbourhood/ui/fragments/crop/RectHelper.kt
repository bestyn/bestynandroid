package com.gbksoft.neighbourhood.ui.fragments.crop

import android.graphics.Matrix
import android.graphics.Rect
import androidx.core.graphics.toRectF
import timber.log.Timber


fun Rect.flipHorizontally(hCenter: Float) {
    val tempLeft = left
    left = (hCenter * 2 - right).toInt()
    right = (hCenter * 2 - tempLeft).toInt()
}

fun Rect.flipVertically(vCenter: Float) {
    val tempTop = top
    top = (vCenter * 2 - bottom).toInt()
    bottom = (vCenter * 2 - tempTop).toInt()
}

fun Rect.rotate(degrees: Float, px: Float = exactCenterX(), py: Float = exactCenterY()) {
    val hDiff = (px - py).toInt()
    val vDiff = (py - px).toInt()
    val matrix = Matrix()
    matrix.setRotate(degrees, px, py)
    matrix.mapRect(this)
    left -= hDiff
    right -= hDiff
    top -= vDiff
    bottom -= vDiff
}

fun Matrix.mapRect(rect: Rect) {
    val rectF = rect.toRectF()
    Timber.tag("CropTag").d("rectF: $rectF")
    mapRect(rectF)
    Timber.tag("CropTag").d("rectF after: $rectF")
    rect.set(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
}