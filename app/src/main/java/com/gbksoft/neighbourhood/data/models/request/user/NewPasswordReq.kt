package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class NewPasswordReq(
    @SerializedName("resetToken")
    private val resetToken: String,

    @SerializedName("newPassword")
    private val newPassword: String,

    @SerializedName("confirmNewPassword")
    private val confirmNewPassword: String
)