package com.gbksoft.neighbourhood.data.models.request.email

import com.google.gson.annotations.SerializedName

class ConfirmEmailReq(
    @SerializedName("token")
    private val token: String,

    @SerializedName("deviceId")
    private val deviceId: String?
)