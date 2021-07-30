package com.gbksoft.neighbourhood.data.models.request.user

import com.google.gson.annotations.SerializedName

class SignUpReq(
    //From Google Maps
    @SerializedName("placeId")
    val addressPlaceId: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)