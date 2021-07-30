package com.gbksoft.neighbourhood.data.models.response.chat

import com.google.gson.annotations.SerializedName

class PostMessageEventModel(
    //create, delete, update
    @SerializedName("action")
    val action: String,

    @SerializedName("data")
    val message: PostMessageModel
)