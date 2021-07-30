package com.gbksoft.neighbourhood.data.repositories.utils

import com.gbksoft.neighbourhood.mappers.posts.PostTypeMapper
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.utils.Constants

object RepositoryConstants {
    val postFeedAllTypes = PostTypeMapper.toServerPostTypeList(
        listOf(PostType.GENERAL, PostType.NEWS, PostType.CRIME, PostType.OFFER, PostType.EVENT, PostType.MEDIA, PostType.STORY)
    )
    val postFeedStory = PostTypeMapper.toServerPostTypeList(
        listOf(PostType.STORY)
    )
    const val postFeedExpand = "categories,profile.avatar.formatted,media.formatted,totalMessages,myReaction,counters,iFollow,audio, audio.profile"
    const val postFeedPerPage = Constants.PER_PAGE

}