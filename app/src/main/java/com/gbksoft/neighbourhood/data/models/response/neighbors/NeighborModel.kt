package com.gbksoft.neighbourhood.data.models.response.neighbors

import com.google.gson.annotations.SerializedName

class NeighborModel(
    @SerializedName("id")
    val id: Long,

    @SerializedName("type")
    val type: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("avatar")
    val avatar: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)
