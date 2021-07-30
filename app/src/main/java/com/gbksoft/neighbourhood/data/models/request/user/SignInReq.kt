package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class SignInReq(
    @SerializedName("email")
    private val email: String,

    @SerializedName("password")
    private val password: String,

    @SerializedName("deviceId")
    private val deviceId: String?
)