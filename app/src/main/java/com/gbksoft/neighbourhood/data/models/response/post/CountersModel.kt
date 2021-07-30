package com.gbksoft.neighbourhood.data.models.response.post

import com.google.gson.annotations.SerializedName

data class CountersModel(

    @SerializedName("followers")
    val followers: Int,

    @SerializedName("messages")
    val messages: Int,

    @SerializedName("reactions")
    val reactions: Int,

    @SerializedName("like")
    val like: Int,

    @SerializedName("love")
    val love: Int,

    @SerializedName("laugh")
    val laugh: Int,

    @SerializedName("angry")
    val angry: Int,

    @SerializedName("sad")
    val sad: Int,

    @SerializedName("top")
    val top: Int,

    @SerializedName("trash")
    val trash: Int
)