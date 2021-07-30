package com.gbksoft.neighbourhood.data.models.response.base

import com.google.gson.annotations.SerializedName

class BasePagination(
    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("pageCount")
    val pageCount: Int,

    @SerializedName("currentPage")
    val currentPage: Int,

    @SerializedName("perPage")
    val perPage: Int
)