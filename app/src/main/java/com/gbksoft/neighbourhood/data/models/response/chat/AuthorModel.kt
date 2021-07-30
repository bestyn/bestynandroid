package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

class AuthorModel(
    @SerializedName("id")
    var id: Long,

    //basic, business
    @SerializedName("type")
    var type: String,

    @SerializedName("fullName")
    var fullName: String,

    @SerializedName("avatar")
    var avatar: AvatarModel?,

    @SerializedName("isOnline")
    var isOnline: Boolean?
)