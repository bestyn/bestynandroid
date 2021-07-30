package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.network.api.ApiPost
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.data.repositories.utils.RepositoryConstants
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.posts.FeedPostMapper
import com.gbksoft.neighbourhood.model.post.FeedPost
import io.reactivex.Observable

class HashtagSearchRepository(
    private val apiPost: ApiPost
) : BaseRepository() {
    private val postsPagingHelper = PagingHelper<FeedPostModel, FeedPost> {
        FeedPostMapper.toFeedPost(it)
    }

    fun findPostsByHashtag(hashtag: String,
                           page: Int = 1): Observable<Paging<List<FeedPost>>> {
        return apiPost
            .getPosts(
                hashtag = hashtag,
                types = RepositoryConstants.postFeedAllTypes,
                expand = RepositoryConstants.postFeedExpand,
                page = page,
                perPage = RepositoryConstants.postFeedPerPage)
            .map { postsPagingHelper.getPagingResult(it) }
    }
}