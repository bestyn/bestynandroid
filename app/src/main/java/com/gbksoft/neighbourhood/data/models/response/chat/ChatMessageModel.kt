package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

open class ChatMessageModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("text")
    var text: String?,

    @SerializedName("isRead")
    var isRead: Boolean?,

    @SerializedName("attachment")
    var attachment: AttachmentModel?,

    @SerializedName("senderProfileId")
    var senderProfileId: Long,

    @SerializedName("recipientProfileId")
    var recipientProfileId: Long,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long,

    @SerializedName("senderProfile")
    var senderProfile: AuthorModel,

    @SerializedName("recipientProfile")
    var recipientProfile: AuthorModel

)