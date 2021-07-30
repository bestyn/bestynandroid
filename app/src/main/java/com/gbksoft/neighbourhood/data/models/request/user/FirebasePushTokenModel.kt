package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class FirebasePushTokenModel(
    @SerializedName("token")
    val token: String,

    @SerializedName("os")
    val os: String = "android"
)
