package com.gbksoft.neighbourhood.mappers.posts

import com.gbksoft.neighbourhood.model.post.PostType

object PostTypeMapper {

    fun toServerPostTypeList(types: List<PostType>): MutableList<String> {
        val postTypes = mutableListOf<String>()
        for (type in types) {
            postTypes.add(toServerPostType(type))
        }
        return postTypes
    }

    fun toAppPostType(type: String) = when (type) {
        "general" -> PostType.GENERAL
        "news" -> PostType.NEWS
        "crime" -> PostType.CRIME
        "offer" -> PostType.OFFER
        "event" -> PostType.EVENT
        "media" -> PostType.MEDIA
        "story" -> PostType.STORY
        else -> null
    }

    fun toServerPostType(type: PostType) = when (type) {
        PostType.GENERAL -> "general"
        PostType.NEWS -> "news"
        PostType.CRIME -> "crime"
        PostType.OFFER -> "offer"
        PostType.EVENT -> "event"
        PostType.MEDIA -> "media"
        PostType.STORY -> "story"
    }
}