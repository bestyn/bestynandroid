package com.gbksoft.neighbourhood.data.models.response.base

import com.google.gson.annotations.SerializedName
import java.util.*

class BaseResponse<ResponseType>(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("result")
    val result: ResponseType?,

    @SerializedName("_meta")
    val meta: BaseMeta? = null
) {
    override fun toString(): String {
        return String.format(Locale.getDefault(), "<%s> status:%d(%s) , msg:%s", this.javaClass.simpleName, code, status, message)
    }

    fun requireResult() = result!!
}