package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class ChangePasswordReq(
    @SerializedName("password")
    private val currentPassword: String,

    @SerializedName("newPassword")
    private val newPassword: String,

    @SerializedName("confirmPassword")
    private val confirmNewPassword: String = newPassword
)