package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

data class OnlineProfilesModel(

    @SerializedName("onlineProfileIds")
    val onlineProfileIds: List<Long>
)