package com.gbksoft.neighbourhood.data.models.response.chat

import com.gbksoft.neighbourhood.data.models.response.file.Formatted
import com.google.gson.annotations.SerializedName

class AvatarModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("origin")
    var origin: String,

    @SerializedName("formatted")
    var formatted: Formatted?,

    @SerializedName("createdAt")
    var createdAt: Long
)