package com.gbksoft.neighbourhood.model.audio

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Audio(
        val id: Long,
        val url: String,
        val description: String,
        var duration: String,
        var isFavorite: Boolean,
        val addedBy: String?,
        var startTime: Int = 0,
        var fileUri: Uri? = null,
        var fileDuration: Int? = null) : Parcelable