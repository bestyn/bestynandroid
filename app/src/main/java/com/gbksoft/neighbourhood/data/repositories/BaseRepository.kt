package com.gbksoft.neighbourhood.data.repositories

import android.net.Uri
import androidx.core.net.toFile
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.models.request.post.UploadMediaReq
import com.gbksoft.neighbourhood.model.media.Media
import retrofit2.HttpException
import retrofit2.Response


abstract class BaseRepository {
    companion object {

        @JvmStatic
        fun <T> Response<T>.isNotSuccessful(): Boolean = !isSuccessful
    }

    @Throws(HttpException::class)
    protected fun checkResponse(resp: Response<*>) {
        if (resp.isNotSuccessful()) {
            throw HttpException(resp)
        }
    }

    @Throws(IllegalStateException::class)
    protected fun createMediaReq(media: Media): UploadMediaReq {
        when (media) {
            is Media.Picture -> {
                val mimeType = "image/*"
                if (media.origin.isFile()) {
                    return UploadMediaReq(media.origin.toFile(), mimeType)
                } else {
                    val data = extractUri(media.origin)
                    data?.let { return UploadMediaReq(it, mimeType) }
                }
            }
        }
        throw IllegalStateException("Wrong file path: ${media.origin}")
    }

    private fun extractUri(uri: Uri): ByteArray? {
        val cr = NApplication.context.contentResolver
        cr.openInputStream(uri).use {
            return it?.readBytes()
        }
    }

}

fun Uri.isFile() = scheme == "file"
