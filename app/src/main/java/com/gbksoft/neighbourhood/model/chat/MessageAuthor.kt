package com.gbksoft.neighbourhood.model.chat

data class MessageAuthor(
    val id: Long,
    val avatar: String?,
    val fullName: String,
    val isBusiness: Boolean
)