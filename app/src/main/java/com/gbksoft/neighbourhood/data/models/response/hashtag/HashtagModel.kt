package com.gbksoft.neighbourhood.data.models.response.hashtag

import com.google.gson.annotations.SerializedName

data class HashtagModel(

    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("featured")
    val featured: Boolean,

    @SerializedName("popularity")
    val popularity: Int
)