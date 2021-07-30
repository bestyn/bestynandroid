package com.gbksoft.neighbourhood.data.models.response.report

import com.google.gson.annotations.SerializedName

class ReportUserResultModel(
    @SerializedName("id")
    var id: Long,

    @SerializedName("profileId")
    var profileId: Long,

    @SerializedName("status")
    var status: Int,

    @SerializedName("authorProfileId")
    var authorProfileId: Long,

    @SerializedName("reason")
    var reason: String,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long
)