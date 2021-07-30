package com.gbksoft.neighbourhood.data.models.response.search

import com.gbksoft.neighbourhood.data.models.response.hashtag.HashtagModel

data class AudioSearchResult (
	val id : Int,
	val description : String,
	val duration : Int,
	val popularity : Int,
	val profileId : Int,
	val url : String,
	val createdAt : Int,
	val profile : ProfileSearchModel,
	val hashtags : HashtagModel,
	val isFavorite : Boolean
)