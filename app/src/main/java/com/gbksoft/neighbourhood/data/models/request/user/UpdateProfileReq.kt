package com.gbksoft.neighbourhood.data.models.request.user

import android.graphics.Bitmap.CompressFormat
import com.gbksoft.neighbourhood.data.models.request.MultipartReq
import com.gbksoft.neighbourhood.data.models.request.RequestUtils
import java.io.File

class UpdateProfileReq : MultipartReq() {
    fun setAvatar(image: File?, format: CompressFormat?) {
        val type: String = when (format) {
            CompressFormat.JPEG -> "image/jpeg"
            CompressFormat.PNG -> "image/png"
            CompressFormat.WEBP -> "image/webp"
            else -> "image/jpeg"
        }
        if (image == null) {
            putField("image", "undefined", "text/plain")
        } else {
            val fieldName = fieldNameWithFileName("image", "avatar_photo")
            putField(fieldName, image, type)
        }
    }

    fun setFullName(fullName: String) {
        putField("fullName", fullName, "text/plain")
    }

    fun setPlaceId(placeId: String) {
        putField("placeId", placeId, "text/plain")
    }

    //1577836800 - in seconds
    fun setBirthday(birthday: Long) {
        putField("birthday", birthday.toString(), "text/plain")
    }

    //1 - show, 0 - don't show
    fun setSeeBusinessPosts(showBusinessPosts: Int) {
        putField("seeBusinessPosts", showBusinessPosts.toString(), "text/plain")
    }

    //Male, Female, Other
    fun setGender(gender: String) {
        putField("gender", gender, "text/plain")
    }

    //List with Hashtags id. Empty to clear Hashtags
    fun setHashtags(ids: List<Long>) {
        val hashtagsValue = RequestUtils.formatHashtagIds(ids)
        putField("hashtagIds", hashtagsValue, "text/plain")
    }

}