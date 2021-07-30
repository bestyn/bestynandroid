package com.gbksoft.neighbourhood.data.models.request.audio

import android.net.Uri
import com.gbksoft.neighbourhood.data.models.request.MultipartReq

class CreateAudioReq: MultipartReq() {

    fun setAudioFile(audio: Uri) {
        val fieldName = fieldNameWithFileName("file", "story_audio.mp3")
        putField(fieldName, audio, "audio/mpeg")
    }

    fun setDescription(description: String) {
        putField("description", description, "text/plain")
    }

    fun setStartTime(startTime: Int) {
        putField("trimStart", startTime.toString(), "text/plain")
    }

    fun setDuration(duration: Int) {
        putField("trimDuration", duration.toString(), "text/plain")
    }
}