package com.gbksoft.neighbourhood.ui.fragments.search.search_engine

import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class PostsSearchEngine(
    private val globalSearchRepository: GlobalSearchRepository
) : BaseSearchEngine<FeedPost>() {

    override fun getSearchEndpoint(query: String, page: Int): Observable<Paging<List<FeedPost>>> {
        return globalSearchRepository.findPosts(query, page)
    }

    override fun searchTag(): String = "PostsSearch"

    override fun getSearchBuffer(): Int = Constants.POST_LIST_PAGINATION_BUFFER

    override fun addToRecentSearch(query: String) {
        globalSearchRepository.addToRecentSearches(query)
    }
}