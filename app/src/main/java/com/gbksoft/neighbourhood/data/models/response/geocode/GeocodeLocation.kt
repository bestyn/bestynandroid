package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodeLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)