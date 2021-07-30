package com.gbksoft.neighbourhood.mvvm

import com.google.gson.annotations.SerializedName

class ErrorJson {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("code")
    var code: Int? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("type")
    var type: String? = null
}