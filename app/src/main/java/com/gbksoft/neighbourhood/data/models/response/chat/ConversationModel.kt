package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

class ConversationModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("lastMessageId")
    var lastMessageId: Long,

    @SerializedName("unreadTotal")
    var unreadTotal: Int,

    @SerializedName("profile")
    var profile: AuthorModel,

    @SerializedName("lastMessage")
    var lastMessage: LastMessageModel,

    @SerializedName("firstUnreadMessageId")
    var firstUnreadMessageId: Long?

)