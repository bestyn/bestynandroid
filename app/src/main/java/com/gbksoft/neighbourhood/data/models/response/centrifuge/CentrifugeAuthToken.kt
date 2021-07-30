package com.gbksoft.neighbourhood.data.models.response.centrifuge

import com.google.gson.annotations.SerializedName

class CentrifugeAuthToken(
    @SerializedName("token")
    val token: String
)