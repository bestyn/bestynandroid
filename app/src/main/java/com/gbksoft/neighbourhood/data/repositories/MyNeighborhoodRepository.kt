package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.domain.utils.asInt
import com.gbksoft.neighbourhood.mappers.posts.FeedPostMapper
import com.gbksoft.neighbourhood.mappers.posts.PostTypeMapper
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class MyNeighborhoodRepository : BaseRepository() {
    companion object {
        const val expand = "categories,profile.avatar.formatted,media.formatted,totalMessages,myReaction,counters,iFollow,allowedComment, media, profile, audio, media.counters"
    }

    private val pagingHelper = PagingHelper<FeedPostModel, FeedPost> {
        FeedPostMapper.toFeedPost(it)
    }

    fun loadAll(paging: Paging<List<FeedPost>>?,
                postTypes: List<PostType>,
                isBusinessContent: Boolean): Observable<Paging<List<FeedPost>>> {
        val types = PostTypeMapper.toServerPostTypeList(postTypes)
        paging?.let {
            return loadAll(types, it.currentPage + 1, paging.itemsPerPage, isBusinessContent)
        } ?: run {
            return loadAll(types, 1, Constants.PER_PAGE, isBusinessContent)
        }
    }

    private fun loadAll(types: List<String>,
                        page: Int,
                        perPage: Int,
                        isBusinessContent: Boolean): Observable<Paging<List<FeedPost>>> {
        return ApiFactory
            .apiAllPosts.getPosts(types, null, 0, page, perPage, isBusinessContent.asInt(), expand)
            .map {
                pagingHelper.getPagingResult(it)
            }
    }

    fun loadRecommended(paging: Paging<List<FeedPost>>?,
                        postTypes: List<PostType>,
                        isBusinessContent: Boolean): Observable<Paging<List<FeedPost>>> {
        val types = PostTypeMapper.toServerPostTypeList(postTypes)
        paging?.let {
            return loadRecommended(types, it.currentPage + 1, paging.itemsPerPage, isBusinessContent)
        } ?: run {
            return loadRecommended(types, 1, Constants.PER_PAGE, isBusinessContent)
        }
    }

    private fun loadRecommended(types: List<String>,
                                page: Int,
                                perPage: Int,
                                isBusinessContent: Boolean): Observable<Paging<List<FeedPost>>> {
        return ApiFactory
            .apiAllPosts.getPosts(types, null, 1, page, perPage, isBusinessContent.asInt(), expand)
            .map { pagingHelper.getPagingResult(it) }
    }
}