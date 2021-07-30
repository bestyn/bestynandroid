package com.gbksoft.neighbourhood.data.models.response.email

import com.google.gson.annotations.SerializedName

class TokenModel(
    @SerializedName("token")
    var accessToken: String,

    @SerializedName("expiredAt")
    var expiredAt: Long,

    @SerializedName("refreshToken")
    var refreshToken: String
)