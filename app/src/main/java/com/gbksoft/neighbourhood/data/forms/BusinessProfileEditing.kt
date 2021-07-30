package com.gbksoft.neighbourhood.data.forms

import android.graphics.Bitmap
import java.io.File

class BusinessProfileEditing(
    val id: Long
) {
    var name: String? = null

    var description: String? = null
    var radius: Int? = null

    var image: File? = null
        private set
    var imageFormat: Bitmap.CompressFormat? = null
        private set

    var addressPlaceId: String? = null
        private set

    var hashtagIds: List<Long>? = null

    var webSite: String? = null
    var email: String? = null
    var phone: String? = null

    fun setImage(image: File, imageFormat: Bitmap.CompressFormat) {
        this.image = image
        this.imageFormat = imageFormat
    }

    fun setAddressPlaceId(addressPlaceId: String) {
        this.addressPlaceId = addressPlaceId
    }
}