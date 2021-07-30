package com.gbksoft.neighbourhood.data.models.response.base

import com.google.gson.annotations.SerializedName

class ErrorResponse(
    @SerializedName("field")
    val field: String?,

    @SerializedName("message")
    val message: String,

    @SerializedName("code")
    val code: Int,

    @SerializedName("params")
    val params: List<ErrorParams>? = null
)