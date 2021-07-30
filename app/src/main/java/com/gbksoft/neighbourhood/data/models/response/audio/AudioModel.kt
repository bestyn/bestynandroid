package com.gbksoft.neighbourhood.data.models.response.audio

import com.google.gson.annotations.SerializedName

data class AudioModel(

	@SerializedName("id")
	val id: Long,

	@SerializedName("profileId")
	val profileId: Long?,

	@SerializedName("url")
	val url: String,

	@SerializedName("description")
	val description: String,

	@SerializedName("duration")
	val duration: Int,

	@SerializedName("popularity")
	val popularity: Int,

	@SerializedName("createdAt")
	val createdAt: Long,

	@SerializedName("isFavorite")
	val isFavorite: Boolean,

	@SerializedName("profile")
	val profile: AudioProfileModel?)
