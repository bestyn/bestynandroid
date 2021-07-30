package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

class LastMessageAttachmentModel(
    @SerializedName("id")
    var id: Long,

    //image, video, voice, other
    @SerializedName("type")
    var type: String,

    @SerializedName("origin")
    var origin: String,

    @SerializedName("originName")
    var title: String?,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("formatted")
    var preview: FormattedPreviewModel?

)