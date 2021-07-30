package com.gbksoft.neighbourhood.data.models.response.file

import com.google.gson.annotations.SerializedName

class Formatted(
    //for image attachment
    @SerializedName("small")
    var small: String?,

    //for image attachment
    @SerializedName("medium")
    var medium: String?,

    //for video attachment, preview picture
    @SerializedName("thumbnail")
    var thumbnail: String?,

    //for video attachment, preview video
    @SerializedName("preview")
    var preview: String?,

    //origin link to media file
    @SerializedName("origin")
    var origin: String
)