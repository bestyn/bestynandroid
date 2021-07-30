package com.gbksoft.neighbourhood.data.models.response

import com.google.gson.annotations.SerializedName

class ConfigModel(
    @SerializedName("version")
    val version: VersionModel,

    @SerializedName("parameters")
    val parameters: Map<String, Any>,

    @SerializedName("errors")
    val errors: Map<String, String>
)