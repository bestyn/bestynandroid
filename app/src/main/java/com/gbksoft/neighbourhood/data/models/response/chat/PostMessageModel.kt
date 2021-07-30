package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

open class PostMessageModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("text")
    var text: String?,

    @SerializedName("attachment")
    var attachment: AttachmentModel?,

    @SerializedName("profile")
    var authorModel: AuthorModel,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long
)