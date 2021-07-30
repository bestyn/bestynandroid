package com.gbksoft.neighbourhood.data.models.request.story

import com.gbksoft.neighbourhood.data.models.request.MultipartReq

class UpdateStoryReq : MultipartReq() {

    // time in seconds
    fun setPosterTimestamp(timeInSec: Long) {
        putField("posterTime", timeInSec.toString(), "text/plain")
    }

    fun setDescription(description: String) {
        putField("description", description, "text/plain")
    }

    fun setLocation(placeId: String) {
        putField("placeId", placeId, "text/plain")
    }

    fun setAllowedComment(isAllowedComment: Boolean) {
        putField("allowedComment", if (isAllowedComment) "1" else "0", "text/plain")
    }

    fun setAllowedDuet(isAllowedDuet: Boolean) {
        putField("allowedDuet", if (isAllowedDuet) "1" else "0", "text/plain")
    }
}