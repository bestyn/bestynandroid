package com.gbksoft.neighbourhood.model.chat

data class Attachment(
    val id: Long,
    val type: Int,
    val title: String,
    val originUrl: String,
    val previewUrl: String,
    val created: Long = System.currentTimeMillis(),
    val updated: Long? = null
) {

    companion object {
        const val TYPE_PICTURE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_FILE = 3
        const val TYPE_AUDIO = 4
    }
}