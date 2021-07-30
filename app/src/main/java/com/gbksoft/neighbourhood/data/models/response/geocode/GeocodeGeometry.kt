package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodeGeometry(
    @SerializedName("bounds") val bounds: GeocodeBounds,
    @SerializedName("location") val location: GeocodeLocation,
    @SerializedName("location_type") val locationType: String,
    @SerializedName("viewport") val viewport: GeocodeViewport
)