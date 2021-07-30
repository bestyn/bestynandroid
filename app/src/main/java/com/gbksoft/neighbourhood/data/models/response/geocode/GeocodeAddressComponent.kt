package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodeAddressComponent(
    @SerializedName("long_name") val longName: String,
    @SerializedName("short_name") val shortName: String,
    @SerializedName("types") val types: MutableList<String>
)