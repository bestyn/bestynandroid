package com.gbksoft.neighbourhood.data.models.request.report

import com.google.gson.annotations.SerializedName

class ReportModel private constructor(
    @SerializedName("reason")
    val reason: String,

    @SerializedName("targetEntityId")
    val targetEntityId: Long,

    @SerializedName("targetEntityType")
    val targetEntityType: String,

    @SerializedName("comment")
    val comment: String?
) {
    companion object {
        fun aboutPost(reason: String, postId: Long, comment: String? = null) =
            ReportModel(reason, postId, "post", comment)

        fun aboutUser(reason: String, profileId: Long, comment: String? = null) =
            ReportModel(reason, profileId, "profile", comment)

        fun aboutAudio(reason: String, audioId: Long, comment: String? = null) =
            ReportModel(reason, audioId, "audio", comment)
    }
}