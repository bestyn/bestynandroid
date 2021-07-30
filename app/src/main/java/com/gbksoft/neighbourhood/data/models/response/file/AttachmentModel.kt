package com.gbksoft.neighbourhood.data.models.response.file

import com.google.gson.annotations.SerializedName

class AttachmentModel(
    @SerializedName("id")
    var id: Long,

    //image, video, ...
    @SerializedName("type")
    var type: String?,

    @SerializedName("origin")
    var url: String,

    @SerializedName("preview")
    var preview: String?,

    @SerializedName("createdAt")
    var createdAt: String,

    @SerializedName("updatedAt")
    var updatedAt: String
)