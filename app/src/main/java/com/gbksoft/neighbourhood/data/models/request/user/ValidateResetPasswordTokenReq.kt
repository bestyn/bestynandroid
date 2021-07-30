package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class ValidateResetPasswordTokenReq(
    @SerializedName("resetToken")
    private val resetToken: String
)