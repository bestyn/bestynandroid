package com.gbksoft.neighbourhood.data.shared_prefs

import com.gbksoft.neighbourhood.data.models.response.chat.AvatarModel
import com.google.gson.annotations.SerializedName

class CurrentProfileModel(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String?,
    @SerializedName("avatar")
    val avatar: AvatarModel?,
    @SerializedName("isBusiness")
    val isBusiness: Boolean,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("isBusinessContentShown")
    val isBusinessContentShown: Boolean?,
    @SerializedName("isContainsInterests")
    val isContainsInterests: Boolean?,

    //deprecated
    @SerializedName("avatarUrl")
    var avatarUrl: String? = null
)