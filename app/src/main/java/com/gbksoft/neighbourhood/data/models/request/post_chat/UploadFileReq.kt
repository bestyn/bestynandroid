package com.gbksoft.neighbourhood.data.models.request.post_chat

import com.gbksoft.neighbourhood.data.models.request.MultipartReq
import com.gbksoft.neighbourhood.model.LocalFile
import java.net.URLEncoder

//typeName: image, video, other, voice
class UploadFileReq(localFile: LocalFile<*>? = null, typeName: String? = null) : MultipartReq() {

    init {
        localFile?.let {
            val fileName = URLEncoder.encode(localFile.name, "utf-8")
            val fieldName = fieldNameWithFileName("file", fileName)
            putField(fieldName, localFile.uri, localFile.mime)
            typeName?.let {
                putField("typeName", typeName, "text/plain")
            }
        }
    }

    constructor() : this(null, null)

    fun putFile(localFile: LocalFile<*>, typeName: String? = null) {
        val fileName = URLEncoder.encode(localFile.name, "utf-8")
        val fieldName = fieldNameWithFileName("file", fileName)
        putField(fieldName, localFile.uri, localFile.mime)
        typeName?.let {
            putField("typeName", typeName, "text/plain")
        }
    }

}