package com.gbksoft.neighbourhood.data.models.response.post

import com.google.gson.annotations.SerializedName

class PostModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("type")
    var type: String,

    @SerializedName("description")
    var description: String,

    @SerializedName("userId")
    var userId: Long,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long
)