package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class RefreshTokenReq(
    @SerializedName("refreshToken")
    private val refreshToken: String
)