package com.gbksoft.neighbourhood.utils.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.util.Synthetic
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object TransformUtils {
    private val DEFAULT_PAINT = Paint(TransformationUtils.PAINT_FLAGS)

    // See #738.
    private val MODELS_REQUIRING_BITMAP_LOCK: Set<String> = HashSet(
        Arrays.asList( // Moto X gen 2
            "XT1085",
            "XT1092",
            "XT1093",
            "XT1094",
            "XT1095",
            "XT1096",
            "XT1097",
            "XT1098",  // Moto G gen 1
            "XT1031",
            "XT1028",
            "XT937C",
            "XT1032",
            "XT1008",
            "XT1033",
            "XT1035",
            "XT1034",
            "XT939G",
            "XT1039",
            "XT1040",
            "XT1042",
            "XT1045",  // Moto G gen 2
            "XT1063",
            "XT1064",
            "XT1068",
            "XT1069",
            "XT1072",
            "XT1077",
            "XT1078",
            "XT1079"))

    /**
     * https://github.com/bumptech/glide/issues/738 On some devices, bitmap drawing is not thread
     * safe. This lock only locks for these specific devices. For other types of devices the lock is
     * always available and therefore does not impact performance
     */
    private val BITMAP_DRAWABLE_LOCK =
        if (MODELS_REQUIRING_BITMAP_LOCK.contains(Build.MODEL)) ReentrantLock()
        else NoLock()

    private class NoLock @Synthetic internal constructor() : Lock {
        override fun lock() {
            // do nothing
        }

        @Throws(InterruptedException::class)
        override fun lockInterruptibly() {
            // do nothing
        }

        override fun tryLock(): Boolean {
            return true
        }

        @Throws(InterruptedException::class)
        override fun tryLock(time: Long, unit: TimeUnit): Boolean {
            return true
        }

        override fun unlock() {
            // do nothing
        }

        override fun newCondition(): Condition {
            throw UnsupportedOperationException("Should not be called")
        }
    }

    fun applyMatrix(inBitmap: Bitmap, targetBitmap: Bitmap, matrix: Matrix) {
        BITMAP_DRAWABLE_LOCK.lock()
        try {
            val canvas = Canvas(targetBitmap)
            canvas.drawBitmap(inBitmap, matrix, DEFAULT_PAINT)
            clear(canvas)
        } finally {
            BITMAP_DRAWABLE_LOCK.unlock()
        }
    }

    // Avoids warnings in M+.
    private fun clear(canvas: Canvas) {
        canvas.setBitmap(null)
    }

    fun getNonNullConfig(bitmap: Bitmap): Bitmap.Config {
        return if (bitmap.config != null) bitmap.config else Bitmap.Config.ARGB_8888
    }

    fun setAlpha(inBitmap: Bitmap, outBitmap: Bitmap) {
        outBitmap.setHasAlpha(inBitmap.hasAlpha())
    }
}