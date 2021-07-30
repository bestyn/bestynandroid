package com.gbksoft.neighbourhood.utils.glide

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.security.MessageDigest

class RectCenterCrop(private val rect: Rect) : BitmapTransformation() {
    private val ID = "com.gbksoft.neighbourhood.utils.glide.RectCrop"
    private val ID_BYTES = ID.toByteArray(Key.CHARSET)

    override fun equals(other: Any?): Boolean {
        return if (other is RectCenterCrop) {
            other.rect == rect
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(), rect.hashCode())
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)

        val radiusData = ByteBuffer.allocate(4 * 4)
            .putInt(rect.left)
            .putInt(rect.top)
            .putInt(rect.right)
            .putInt(rect.bottom)
            .array()
        messageDigest.update(radiusData)
    }

    override fun transform(pool: BitmapPool, bitmap: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        if (bitmap.width == rect.width() && bitmap.height == rect.height()) {
            return bitmap
        }

        val rectBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
        val scale: Float
        val dx: Float
        val dy: Float
        val m = Matrix()
        if (rectBitmap.width * outHeight > outWidth * rectBitmap.height) {
            scale = outHeight.toFloat() / rectBitmap.height.toFloat()
            dx = (outWidth - rectBitmap.width * scale) * 0.5f
            dy = 0f
        } else {
            scale = outWidth.toFloat() / rectBitmap.width.toFloat()
            dx = 0f
            dy = (outHeight - rectBitmap.height * scale) * 0.5f
        }

        m.setScale(scale, scale)
        m.postTranslate((dx + 0.5f).toInt().toFloat(), (dy + 0.5f).toInt().toFloat())

        val result = pool.get(outWidth, outHeight, TransformUtils.getNonNullConfig(bitmap))
        TransformUtils.setAlpha(rectBitmap, result)
        TransformUtils.applyMatrix(rectBitmap, result, m)
        return result
    }


}