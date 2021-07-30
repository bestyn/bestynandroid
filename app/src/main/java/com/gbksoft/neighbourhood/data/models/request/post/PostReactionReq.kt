package com.gbksoft.neighbourhood.data.models.request.post

import com.google.gson.annotations.SerializedName

data class PostReactionReq(@SerializedName("reaction") val reaction: String)