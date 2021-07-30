package com.gbksoft.neighbourhood.data.models.response.post

import com.google.gson.annotations.SerializedName

data class MyReactionModel(

    @SerializedName("postId")
    val postId: Long,

    @SerializedName("profileId")
    val profileId: Long,

    @SerializedName("reaction")
    val reaction: String
)