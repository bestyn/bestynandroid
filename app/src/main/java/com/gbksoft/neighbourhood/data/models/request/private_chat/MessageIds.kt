package com.gbksoft.neighbourhood.data.models.request.private_chat

import com.google.gson.annotations.SerializedName

class MessageIds(
    @SerializedName("ids")
    val ids: LongArray
)