package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.post.PostReactionModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.posts.PostReactionMapper
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class ReactionRepository : BaseRepository() {

    private val expand = "profile.avatar"
    private val pagingHelper = PagingHelper<PostReactionModel, PostReaction> {
        PostReactionMapper.toPostReaction(it)
    }

    fun getPostReactions(paging: Paging<List<PostReaction>>?, postId: Long, reaction: String?): Observable<Paging<List<PostReaction>>> {
        paging?.let {
            return getPostReactions(postId, reaction, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return getPostReactions(postId, reaction, 1, Constants.PER_PAGE)
        }
    }

    private fun getPostReactions(postId: Long,
                                 reaction: String?,
                                 page: Int,
                                 perPage: Int): Observable<Paging<List<PostReaction>>> {
        return ApiFactory
            .apiPost.getPostReactions(postId, page, perPage, reaction, expand)
            .map { pagingHelper.getPagingResult(it) }
    }
}