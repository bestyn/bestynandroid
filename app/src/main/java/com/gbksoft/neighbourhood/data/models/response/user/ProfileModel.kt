package com.gbksoft.neighbourhood.data.models.response.user

import com.gbksoft.neighbourhood.data.models.response.file.MediaModel
import com.gbksoft.neighbourhood.data.models.response.hashtag.HashtagModel
import com.google.gson.annotations.SerializedName

class ProfileModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("avatar")
    var avatar: MediaModel?,

    @SerializedName("fullName")
    var fullName: String,

    @SerializedName("address")
    var address: String,

    @SerializedName("birthday")
    var birthday: Long?,

    @SerializedName("gender")
    var gender: String?,

    //1 or 0
    @SerializedName("seeBusinessPosts")
    var seeBusinessPosts: Int,

    @SerializedName("latitude")
    var latitude: Double,

    @SerializedName("longitude")
    var longitude: Double,

    @JvmField
    @SerializedName("hashtags")
    var hashtags: List<HashtagModel>,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long,

    @SerializedName("hasUnreadMessages")
    var hasUnreadMessages: Boolean,

    @SerializedName("isFollower")
    var isFollower: Boolean,

    @SerializedName("isFollowed")
    var isFollowed: Boolean
)