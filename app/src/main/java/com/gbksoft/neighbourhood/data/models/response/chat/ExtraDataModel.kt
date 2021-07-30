package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

class ExtraDataModel(
    @SerializedName("hasUnreadMessages")
    var hasUnreadMessages: Boolean?
)