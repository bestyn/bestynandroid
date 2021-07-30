package com.gbksoft.neighbourhood.ui.fragments.search.search_engine

import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class ProfilesSearchEngine(
    private val globalSearchRepository: GlobalSearchRepository
) : BaseSearchEngine<ProfileSearchItem>() {

    override fun getSearchEndpoint(query: String, page: Int): Observable<Paging<List<ProfileSearchItem>>> {
        return globalSearchRepository.findProfiles(query = query, page = page)
    }

    override fun searchTag(): String = "ProfilesSearch"

    override fun getSearchBuffer(): Int = Constants.PROFILE_SEARCH_LIST_PAGINATION_BUFFER

    override fun addToRecentSearch(query: String) {
        globalSearchRepository.addToRecentSearches(query)
    }
}