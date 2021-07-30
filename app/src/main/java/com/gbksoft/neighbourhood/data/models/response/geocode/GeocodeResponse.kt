package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodeResponse(
    @SerializedName("plus_code") val plusCode: GeocodePlusCode,
    @SerializedName("results") val results: List<GeocodeResult>,
    @SerializedName("status") val status: String
)