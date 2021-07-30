package com.gbksoft.neighbourhood.data.models.response.file

import com.google.gson.annotations.SerializedName

class MediaModel(
        @SerializedName("id")
        var id: Long,

        //image, video
        @SerializedName("type")
        var type: String?,

        @SerializedName("origin")
        var url: String,

        @SerializedName("createdAt")
        var createdAt: Long,

        @SerializedName("formatted")
        var formatted: Formatted?,

        @SerializedName("counters")
        var counters: MediaCounters?
)