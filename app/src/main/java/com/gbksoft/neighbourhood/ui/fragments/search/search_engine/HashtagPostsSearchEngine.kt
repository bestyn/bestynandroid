package com.gbksoft.neighbourhood.ui.fragments.search.search_engine

import com.gbksoft.neighbourhood.data.repositories.HashtagSearchRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class HashtagPostsSearchEngine(
    private val hashtagSearchRepository: HashtagSearchRepository
) : BaseSearchEngine<FeedPost>() {

    override fun getSearchEndpoint(query: String, page: Int): Observable<Paging<List<FeedPost>>> {
        return hashtagSearchRepository.findPostsByHashtag(query, page)
    }

    override fun searchTag(): String = "HashtagPostsSearch"

    override fun getSearchBuffer(): Int = Constants.POST_LIST_PAGINATION_BUFFER

    override fun addToRecentSearch(query: String) {}
}