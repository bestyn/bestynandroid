package com.gbksoft.neighbourhood.data.models.response.user

import com.google.gson.annotations.SerializedName

class UserModel(
    @SerializedName("id")
    var userId: Long,

    @SerializedName("email")
    var email: String,

    @JvmField
    @SerializedName("profile")
    var profile: ProfileModel,

    @SerializedName("businessProfiles")
    var businessProfiles: List<BusinessProfileModel>
)