package com.gbksoft.neighbourhood.data.models.response.my_posts

import com.gbksoft.neighbourhood.data.models.response.file.MediaModel
import com.google.gson.annotations.SerializedName

class ProfileFeedModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("type")
    var type: String,

    @SerializedName("avatar")
    var avatar: MediaModel?,

    @SerializedName("fullName")
    var fullName: String
)