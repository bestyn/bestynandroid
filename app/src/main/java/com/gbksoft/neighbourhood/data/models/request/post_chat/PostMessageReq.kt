package com.gbksoft.neighbourhood.data.models.request.post_chat

import com.google.gson.annotations.SerializedName

class PostMessageReq(
    @SerializedName("text")
    private var text: String? = null,
    @SerializedName("attachmentId")
    private var attachmentId: Long? = null
)