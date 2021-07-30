package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodePlusCode(
    @SerializedName("compound_code") val compoundCode: String,
    @SerializedName("global_code") val globalCode: String
)