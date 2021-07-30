package com.gbksoft.neighbourhood.data.models.response.audio

import com.gbksoft.neighbourhood.data.models.response.user.ProfileModel
import com.google.gson.annotations.SerializedName

data class AudioStoriesModel (
		@SerializedName("id") val id : Int,
		@SerializedName("description") val description : String,
		@SerializedName("duration") val duration : Int,
		@SerializedName("popularity") val popularity : Int,
		@SerializedName("profileId") val profileId : String,
		@SerializedName("profile") val profile : ProfileModel,
		@SerializedName("url") val url : String,
		@SerializedName("createdAt") val createdAt : Int
)