package com.gbksoft.neighbourhood.data.models.request.user

import android.graphics.Bitmap.CompressFormat
import com.gbksoft.neighbourhood.data.models.request.MultipartReq
import com.gbksoft.neighbourhood.data.models.request.RequestUtils
import java.io.File

class UpdateBusinessProfileReq : MultipartReq() {
    //required
    fun setAvatar(image: File, format: CompressFormat) {
        val type: String = when (format) {
            CompressFormat.JPEG -> "image/jpeg"
            CompressFormat.PNG -> "image/png"
            CompressFormat.WEBP -> "image/webp"
            else -> "image/jpeg"
        }

        val fieldName = fieldNameWithFileName("image", "avatar_photo")
        putField(fieldName, image, type)
    }

    //required
    fun setName(fullName: String) {
        putField("fullName", fullName, "text/plain")
    }

    fun setPlaceId(placeId: String) {
        putField("placeId", placeId, "text/plain")
    }

    //required
    fun setDescription(description: String) {
        putField("description", description, "text/plain")
    }

    //required
    fun setRadius(radius: Int) {
        putField("radius", radius.toString(), "text/plain")
    }

    //List with categories id. Empty to clear categories
    fun setCategories(ids: List<Long>) {
        val interestsValue = RequestUtils.formatHashtagIds(ids)
        putField("hashtagIds", interestsValue, "text/plain")
    }

    fun setSite(site: String) {
        putField("site", site, "text/plain")
    }

    fun setEmail(email: String) {
        putField("email", email, "text/plain")
    }

    fun setPhone(phone: String) {
        putField("phone", phone, "text/plain")
    }
}