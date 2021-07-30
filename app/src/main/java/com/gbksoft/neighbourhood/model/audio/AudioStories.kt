package com.gbksoft.neighbourhood.model.audio

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioStories(
        val id: Int?,
        val description: String?,
        val duration: Int?,
        val popularity: Int?,
        val profileId: String?,
        val profileFullName: String?,
        val url: String?,
        val createdAt: Int?) : Parcelable