package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class RecoveryPasswordReq(
    @SerializedName("email")
    private val email: String
)