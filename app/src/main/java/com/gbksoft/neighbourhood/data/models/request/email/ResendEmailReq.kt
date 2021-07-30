package com.gbksoft.neighbourhood.data.models.request.email

import com.google.gson.annotations.SerializedName

class ResendEmailReq(
    @SerializedName("email")
    private val email: String
)