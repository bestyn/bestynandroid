package com.gbksoft.neighbourhood.data.models.request

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.repositories.isFile
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import timber.log.Timber
import java.io.File
import java.util.*


abstract class MultipartReq : HashMap<String, RequestBody>() {
    protected fun fieldNameWithFileName(fieldName: String, fileName: String): String {
        return "$fieldName\"; filename=\"$fileName\""
    }

    protected fun putField(fieldName: String, fieldValue: String, fieldType: String) {
        val contentType = fieldType.toMediaTypeOrNull() ?: return
        val requestBody: RequestBody = fieldValue.toRequestBody(contentType)
        put(fieldName, requestBody)
    }

    protected fun putField(fieldName: String, fieldValue: ByteArray, fieldType: String) {
        val contentType = fieldType.toMediaTypeOrNull() ?: return
        val requestBody: RequestBody = fieldValue.toRequestBody(contentType)
        put(fieldName, requestBody)
    }

    protected fun putField(fieldName: String, fieldValue: File, fieldType: String) {
        val contentType = fieldType.toMediaTypeOrNull() ?: return
        val requestBody: RequestBody = fieldValue.asRequestBody(contentType)
        put(fieldName, requestBody)
    }

    protected fun putField(fieldName: String, fieldValue: Uri, fieldType: String) {
        val contentType = fieldType.toMediaTypeOrNull() ?: return
        val requestBody: RequestBody = fieldValue.asRequestBody(contentType)
        put(fieldName, requestBody)
    }

    fun Uri.asRequestBody(contentType: MediaType? = null): RequestBody {
        val contentResolver = NApplication.context.contentResolver
        val uri = this
        return object : RequestBody() {
            private var fileLength: Long? = null
            override fun contentType() = contentType

            override fun contentLength(): Long {
                return fileLength ?: run {
                    var oldLength: Long = -1
                    var length = fetchFileLength()
                    //protection from parallel writing into a file (e.g. image/video from camera)
                    while (length != oldLength) {
                        oldLength = length
                        Thread.sleep(200)
                        length = fetchFileLength()
                    }
                    fileLength = length
                    return length
                }
            }

            private fun fetchFileName(cursor: Cursor): String {
                return contentResolver.query(uri, null, null, null, null)
                    ?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        return@use cursor.getString(nameIndex) ?: "null"
                    } ?: "null"
            }

            private fun fetchFileLength(): Long {
                return if (uri.isFile()) {
                    uri.toFile().length()
                } else {
                    contentResolver.query(uri, null, null, null, null)
                        ?.use { cursor ->
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            cursor.moveToFirst()
                            return cursor.getLong(sizeIndex)
                        } ?: 0L
                }
            }

            override fun writeTo(sink: BufferedSink) {
                /*contentResolver.openInputStream(uri)!!.source()
                        .use { source -> sink.writeAll(source) }*/
                val fileLength: Long = contentLength()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded: Long = 0

                contentResolver.openInputStream(uri)?.use { input ->
                    var read: Int
                    //val handler = Handler(Looper.getMainLooper())
                    while (input.read(buffer).also { bytes -> read = bytes } != -1) {

                        // update progress on UI thread
                        //handler.post(ProgressUpdater(uploaded, fileLength))
                        uploaded += read.toLong()
                        postProgress(uploaded, fileLength)
                        sink.write(buffer, 0, read)
                    }
                }
            }

            var lastProgress: Long = 0
            private fun postProgress(uploaded: Long, fileLength: Long) {
                //Timber.tag("VideoTag").d("uploaded: $uploaded")
                if (uploaded - lastProgress >= 50 * 1024 * 1024) {
                    lastProgress = uploaded
                    Timber.tag("FileUploadTag").d("uploaded: $uploaded")
                }
            }


        }
    }
}