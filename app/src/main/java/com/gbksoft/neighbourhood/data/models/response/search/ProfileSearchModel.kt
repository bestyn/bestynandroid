package com.gbksoft.neighbourhood.data.models.response.search

import com.gbksoft.neighbourhood.data.models.response.chat.AvatarModel
import com.google.gson.annotations.SerializedName

data class ProfileSearchModel(
        @SerializedName("id")
        val id: Long,

        @SerializedName("type")
        val type: String, //business or basic

        @SerializedName("fullName")
        val fullName: String,

        @SerializedName("rating")
        val rating: Any?,

        @SerializedName("avatar")
        val avatar: AvatarModel,

        @SerializedName("isFollowed")
        val isFollowed: Boolean,

        @SerializedName("isFollower")
        val isFollower: Boolean
)