package com.gbksoft.neighbourhood.data.models.response.chat

import com.gbksoft.neighbourhood.data.models.response.file.Formatted
import com.google.gson.annotations.SerializedName

class AttachmentModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("origin")
    var origin: String,

    @SerializedName("originName")
    var title: String?,

    //image, video, voice, other
    @SerializedName("type")
    var type: String,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long?,

    @SerializedName("formatted")
    var formatted: Formatted?
)