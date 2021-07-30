package com.gbksoft.neighbourhood.model.story.creating

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class VideoSegment(
        val uri: Uri,
        val duration: Int,
        val isFromImage: Boolean,
        val originalFilePath: String? = null,
        var startTime: Int = 0,
        var endTime: Int = duration) : Parcelable {

    val needToCrop: Boolean
        get() = startTime != 0 || endTime != duration

    fun clone() = VideoSegment(uri, duration, isFromImage, originalFilePath, startTime, endTime)
}