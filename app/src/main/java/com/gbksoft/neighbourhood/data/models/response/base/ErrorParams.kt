package com.gbksoft.neighbourhood.data.models.response.base

import com.google.gson.annotations.SerializedName

class ErrorParams(
    @SerializedName("name")
    val name: String,

    @SerializedName("value")
    val value: String
)