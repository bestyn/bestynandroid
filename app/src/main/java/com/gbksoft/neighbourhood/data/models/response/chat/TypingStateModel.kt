package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

data class TypingStateModel(

    @SerializedName("isTyping")
    val isTyping: Boolean,

    @SerializedName("profileId")
    val profileId: Long
)
