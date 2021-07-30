package com.gbksoft.neighbourhood.model.news

data class News(
    val id: Long,
    val link: String,
    val imageUrl: String?,
    val description: String?
)