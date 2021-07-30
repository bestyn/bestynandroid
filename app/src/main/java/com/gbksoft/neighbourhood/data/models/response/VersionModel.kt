package com.gbksoft.neighbourhood.data.models.response

import com.google.gson.annotations.SerializedName

class VersionModel(
    @SerializedName("major")
    val major: Int,

    @SerializedName("minor")
    val minor: Int,

    @SerializedName("patch")
    val patch: Int,

    @SerializedName("commit")
    val commit: String
)

