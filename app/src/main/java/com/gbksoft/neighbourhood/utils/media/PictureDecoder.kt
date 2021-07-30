package com.gbksoft.neighbourhood.utils.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gbksoft.neighbourhood.utils.Constants
import java.io.File
import java.util.concurrent.Future

class PictureDecoder internal constructor(context: Context) {
    private val requestManager: RequestManager = Glide.with(context)
    private var currentFuture: Future<Bitmap>? = null

    @Throws(Throwable::class)
    fun load(uri: Uri?): Bitmap? {
        val future = requestManager
            .asBitmap()
            .load(uri)
            .override(Constants.PICTURE_PIXELS_MAX_SIZE)
            .submit()
        currentFuture = future
        return if (future.isCancelled.not()) {
            future.get()
        } else {
            null
        }
    }

    @Throws(Throwable::class)
    fun load(file: File?): Bitmap? {
        val future = requestManager
            .asBitmap()
            .load(file)
            .override(Constants.PICTURE_PIXELS_MAX_SIZE)
            .submit()
        currentFuture = future
        return if (future.isCancelled.not()) {
            future.get()
        } else {
            null
        }
    }

    fun cancel() {
        currentFuture?.cancel(true)
    }

}