package com.gbksoft.neighbourhood.data.models.response.user

import com.gbksoft.neighbourhood.data.models.response.file.MediaModel
import com.gbksoft.neighbourhood.data.models.response.hashtag.HashtagModel
import com.google.gson.annotations.SerializedName

class BusinessProfileModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("avatar")
    var avatar: MediaModel?,

    @SerializedName("fullName")
    var name: String,

    @SerializedName("description")
    var description: String,

    @SerializedName("address")
    var address: String,

    @SerializedName("radius")
    var radius: Int,

    @SerializedName("site")
    var site: String?,

    @SerializedName("email")
    var email: String?,

    @SerializedName("phone")
    var phone: String?,

    @SerializedName("longitude")
    var longitude: Double,

    @SerializedName("latitude")
    var latitude: Double,

    @SerializedName("hashtags")
    var hashtags: List<HashtagModel>,

    @SerializedName("images")
    var images: List<MediaModel>?,

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