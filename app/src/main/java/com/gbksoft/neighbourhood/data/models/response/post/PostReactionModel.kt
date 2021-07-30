package com.gbksoft.neighbourhood.data.models.response.post

import com.gbksoft.neighbourhood.data.models.response.my_posts.ProfileFeedModel
import com.google.gson.annotations.SerializedName

data class PostReactionModel(

    @SerializedName("postId")
    val postId: Long,

    @SerializedName("profileId")
    val profileId: Long,

    @SerializedName("reaction")
    val reaction: String,

    @SerializedName("profile")
    val profile: ProfileFeedModel
)