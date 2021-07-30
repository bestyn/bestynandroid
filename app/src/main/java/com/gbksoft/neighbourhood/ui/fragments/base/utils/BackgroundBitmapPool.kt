package com.gbksoft.neighbourhood.ui.fragments.base.utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator

class BackgroundBitmapPool(context: Context) {
    private var bitmapPool: BitmapPool

    init {
        val memorySizeCalculator = MemorySizeCalculator.Builder(context).build()
        val size = memorySizeCalculator.bitmapPoolSize
        bitmapPool = if (size > 0) {
            LruBitmapPool(size.toLong())
        } else {
            BitmapPoolAdapter()
        }
    }

    fun get(width: Float, height: Float, config: Bitmap.Config): Bitmap {
        return bitmapPool.get(width.toInt(), height.toInt(), config)
    }
}