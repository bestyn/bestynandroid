package com.gbksoft.neighbourhood.model.hashtag

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Hashtag(
    val id: Long,
    val name: String,
    var isSelected: Boolean = false): Parcelable {

    fun clone() = Hashtag(id, name, isSelected)
}