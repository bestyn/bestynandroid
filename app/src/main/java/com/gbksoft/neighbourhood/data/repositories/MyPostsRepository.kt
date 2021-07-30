package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.models.response.views.MediaViewsResult
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.posts.FeedPostMapper
import com.gbksoft.neighbourhood.mappers.posts.PostTypeMapper
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Completable
import io.reactivex.Observable

class MyPostsRepository : BaseRepository() {
    private val expand = "categories,profile.avatar.formatted,media.formatted,totalMessages,counters,myReaction,iFollow"
    private val pagingHelper = PagingHelper<FeedPostModel, FeedPost> {
        FeedPostMapper.toFeedPost(it)
    }

    fun loadFollowed(paging: Paging<List<FeedPost>>?,
                     postTypes: List<PostType>): Observable<Paging<List<FeedPost>>> {
        val types = PostTypeMapper.toServerPostTypeList(postTypes)
        paging?.let {
            return loadFollowed(types, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadFollowed(types, 1, Constants.PER_PAGE)
        }
    }

    private fun loadFollowed(types: List<String>,
                             page: Int,
                             perPage: Int): Observable<Paging<List<FeedPost>>> {
        return ApiFactory
            .apiMyPosts.getMyPosts(types, null, 0, page, perPage, expand)
            .map { pagingHelper.getPagingResult(it) }
    }

    fun loadCreated(paging: Paging<List<FeedPost>>?,
                    postTypes: List<PostType>): Observable<Paging<List<FeedPost>>> {
        val types = PostTypeMapper.toServerPostTypeList(postTypes)
        paging?.let {
            return loadCreated(types, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadCreated(types, 1, Constants.PER_PAGE)
        }
    }

    private fun loadCreated(types: List<String>,
                            page: Int,
                            perPage: Int): Observable<Paging<List<FeedPost>>> {

        return ApiFactory
            .apiMyPosts.getMyPosts(types, null, 1, page, perPage, expand)
            .map { pagingHelper.getPagingResult(it) }
    }

    fun deletePost(postId: Long): Completable {
        return Completable.create { emitter ->
            try {
                val api = ApiFactory.apiPost
                checkResponse(api.deletePost(postId).execute())
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    fun addAudioCounter(audioId: Int): Observable<BaseResponse<List<MediaViewsResult>>?> {
        return ApiFactory.apiAudio.addAudioCounter(audioId)
    }
}