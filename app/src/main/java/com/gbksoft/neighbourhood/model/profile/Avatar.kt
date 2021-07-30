package com.gbksoft.neighbourhood.model.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Avatar(
    val origin: String,
    private val medium: String? = null,
    private val small: String? = null
) : Parcelable {
    fun getSmall(): String {
        return small ?: getMedium()
    }

    fun getMedium(): String {
        return medium ?: origin
    }
}