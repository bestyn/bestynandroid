package com.gbksoft.neighbourhood.data.models.response.news

import com.google.gson.annotations.SerializedName

class NewsModel(
    @SerializedName("id")
    val id: Long,

    @SerializedName("url")
    val url: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("description")
    val description: String
)