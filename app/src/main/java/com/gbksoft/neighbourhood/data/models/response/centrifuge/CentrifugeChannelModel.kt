package com.gbksoft.neighbourhood.data.models.response.centrifuge

import com.google.gson.annotations.SerializedName

class CentrifugeChannelModel(
    @SerializedName("channel")
    val channelId: String,

    @SerializedName("token")
    val channelToken: String
)