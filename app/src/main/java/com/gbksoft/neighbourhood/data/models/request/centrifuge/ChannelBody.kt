package com.gbksoft.neighbourhood.data.models.request.centrifuge

import com.google.gson.annotations.SerializedName

class ChannelBody(
    @SerializedName("client")
    private var client: String,

    @SerializedName("channel")
    private var channel: String
)