package com.gbksoft.neighbourhood.data.models.request.private_chat

import com.google.gson.annotations.SerializedName

class PrivateMessageReq(
    @SerializedName("recipientProfileId")
    private var opponentId: Long,
    @SerializedName("text")
    private var text: String? = null,
    @SerializedName("attachmentId")
    private var attachmentId: Long? = null
)