package com.gbksoft.neighbourhood.data.models.request.post

import com.google.gson.annotations.SerializedName

class CreatePostReq {
    @SerializedName("name")
    private var name: String? = null

    @SerializedName("description")
    private var description: String

    @SerializedName("placeId")
    private var placeId: String? = null

    @SerializedName("price")
    private var price: Double? = null

    @SerializedName("startDatetime")
    private var startDatetimeInSec: Long? = null

    @SerializedName("endDatetime")
    private var endDatetimeInSec: Long? = null

    constructor(description: String) {
        this.description = description
    }

    constructor(description: String, placeId: String?) {
        this.description = description
        this.placeId = placeId
    }

    constructor(description: String, price: Double?) {
        this.description = description
        this.price = price
    }

    constructor(description: String, placeId: String?,
                name: String?, startDatetimeInSec: Long?, endDatetimeInSec: Long?) {
        this.description = description
        this.placeId = placeId
        this.name = name
        this.startDatetimeInSec = startDatetimeInSec
        this.endDatetimeInSec = endDatetimeInSec
    }
}