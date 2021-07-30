package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

class LastMessageModel(

    @SerializedName("id")
    val id: Long,

    @SerializedName("text")
    val text: String,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("attachment")
    val attachment: LastMessageAttachmentModel?,

    @SerializedName("senderProfileId")
    val senderProfileId: Long,

    @SerializedName("recipientProfileId")
    val recipientProfileId: Long,

    @SerializedName("createdAt")
    val createdAt: Long,

    @SerializedName("updatedAt")
    val updatedAt: Long

)