package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

open class BaseMessageEventModel<T : Any> {

    @SerializedName("action")
    var action: String? = null

    @SerializedName("data")
    lateinit var message: T

    @SerializedName("extraData")
    var extraData: ExtraDataModel? = null
}