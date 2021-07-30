package com.gbksoft.neighbourhood.data.models.request.story

import android.net.Uri
import com.gbksoft.neighbourhood.data.models.request.MultipartReq

class CreateStoryReq : MultipartReq() {
    //required
    fun setVideoFile(video: Uri) {
        val fieldName = fieldNameWithFileName("file", "story_video.mp4")
        putField(fieldName, video, "video/mp4")
    }

    //required (time in seconds)
    fun setPosterTimestamp(timeInSec: Long) {
        putField("posterTime", timeInSec.toString(), "text/plain")
    }

    //required
    fun setDescription(description: String) {
        putField("description", description, "text/plain")
    }

    //required ()
    fun setAllowedComment(isAllowedComment: Boolean) {
        putField("allowedComment", if (isAllowedComment) "1" else "0", "text/plain")
    }

    //required ()
    fun setAllowedDuet(isAllowedDuet: Boolean) {
        putField("allowedDuet", if (isAllowedDuet) "1" else "0", "text/plain")
    }

    fun setLocation(placeId: String) {
        putField("placeId", placeId, "text/plain")
    }

    fun setAudioId(audioId: String) {
        putField("audioId", audioId, "text/plain")
    }
}