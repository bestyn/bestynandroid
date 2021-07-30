package com.gbksoft.neighbourhood.data.models.response.search

data class AudioSearchModel (
	val code : Int,
	val status : String,
	val message : String,
	val result : List<AudioSearchResult>
)