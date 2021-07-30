package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.post_feed.PostFilter

class PostFeedSettings {
    private var postFilter: PostFilter? = null
    private var postsTypeList = mutableListOf<PostType>()

    init {
        setPostFilter(null)
    }

    fun setPostFilter(filter: PostFilter?) {
        postFilter = filter
        when (filter) {
            null -> setAllTypes()
            PostFilter.GENERAL -> setType(PostType.GENERAL)
            PostFilter.NEWS -> setType(PostType.NEWS)
            PostFilter.CRIME -> setType(PostType.CRIME)
            PostFilter.OFFER -> setType(PostType.OFFER)
            PostFilter.EVENT -> setType(PostType.EVENT)
            PostFilter.MEDIA -> setType(PostType.MEDIA)
            PostFilter.STORY -> setType(PostType.STORY)
            PostFilter.BUSINESS -> setAllTypes()
            PostFilter.RECOMMENDED -> setAllTypes()
            PostFilter.CREATED -> setAllTypes()
            PostFilter.FOLLOWED -> setAllTypes()
        }
    }

    private fun setType(type: PostType) {
        postsTypeList.clear()
        postsTypeList.add(type)
    }

    private fun setAllTypes() {
        postsTypeList.clear()
        postsTypeList.addAll(
            arrayListOf(
                PostType.GENERAL,
                PostType.NEWS,
                PostType.CRIME,
                PostType.OFFER,
                PostType.EVENT,
                PostType.MEDIA,
                PostType.STORY
            )
        )
    }

    fun getPostsTypeList(): List<PostType> = postsTypeList
    fun isShowBusinessContent(): Boolean = postFilter == PostFilter.BUSINESS
    fun isShowRecommended(): Boolean = postFilter == PostFilter.RECOMMENDED
    fun isShowCreated(): Boolean = postFilter == PostFilter.CREATED
    fun isShowFollowed(): Boolean = postFilter == PostFilter.FOLLOWED
}