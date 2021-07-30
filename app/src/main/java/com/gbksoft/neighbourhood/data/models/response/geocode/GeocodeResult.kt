package com.gbksoft.neighbourhood.data.models.response.geocode

import com.google.gson.annotations.SerializedName

data class GeocodeResult(
    @SerializedName("address_components") val addressComponents: List<GeocodeAddressComponent>,
    @SerializedName("formatted_address") val formattedAddress: String,
    @SerializedName("geometry") val geometry: GeocodeGeometry,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("types") val types: List<String>
)