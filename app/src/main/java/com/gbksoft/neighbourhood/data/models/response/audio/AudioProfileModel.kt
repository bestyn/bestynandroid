package com.gbksoft.neighbourhood.data.models.response.audio

import com.gbksoft.neighbourhood.data.models.response.chat.AvatarModel
import com.google.gson.annotations.SerializedName

data class AudioProfileModel(
    @SerializedName("id")
    val id: Long,

    @SerializedName("type")
    val type: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("avatar")
    val avatar: AvatarModel
)