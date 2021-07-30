package com.gbksoft.neighbourhood.data.models.request.post

import android.graphics.Rect
import android.net.Uri
import com.gbksoft.neighbourhood.data.models.request.MultipartReq
import java.io.File

class UploadMediaReq : MultipartReq {

    constructor(data: ByteArray, mimeType: String) : super() {
        val fieldName = fieldNameWithFileName("file", "${System.currentTimeMillis()}")
        putField(fieldName, data, mimeType)
    }

    constructor(uri: Uri, mimeType: String) : super() {
        val fieldName = fieldNameWithFileName("file", "${System.currentTimeMillis()}")
        putField(fieldName, uri, mimeType)
    }

    constructor(file: File, mimeType: String) : super() {
        val fieldName = fieldNameWithFileName("file", "${System.currentTimeMillis()}")
        putField(fieldName, file, mimeType)
    }

    fun setCropRect(rect: Rect) {
        putField("x", rect.left.toString(), "text/plain")
        putField("y", rect.top.toString(), "text/plain")
        putField("width", rect.width().toString(), "text/plain")
        putField("height", rect.height().toString(), "text/plain")
    }
}