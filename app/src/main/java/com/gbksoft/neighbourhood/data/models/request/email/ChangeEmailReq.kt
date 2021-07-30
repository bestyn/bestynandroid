package com.gbksoft.neighbourhood.data.models.request.email

import com.google.gson.annotations.SerializedName

class ChangeEmailReq(
    @SerializedName("newEmail")
    private val newEmail: String
)