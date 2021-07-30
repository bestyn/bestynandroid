package com.gbksoft.neighbourhood.data.models.response.base

import com.google.gson.annotations.SerializedName

class BaseMeta(
    @SerializedName("pagination")
    val pagination: BasePagination? = null
)