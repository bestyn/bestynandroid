package com.gbksoft.neighbourhood.data.models.response.views

import com.google.gson.annotations.SerializedName

data class MediaViewsResult(
        @SerializedName("field")
        val field : String? = null,
        @SerializedName("message")
        val message : String? = null
)