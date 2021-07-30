package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodeBounds(
    @SerializedName("northeast") val northeast: Northeast,
    @SerializedName("southwest") val southwest: Southwest
)