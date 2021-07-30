package com.gbksoft.neighbourhood.ui.fragments.search.search_engine

import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class AudioSearchEngine(
        private val globalSearchRepository: GlobalSearchRepository
) : BaseSearchEngine<Audio>() {

    override fun getSearchEndpoint(query: String, page: Int): Observable<Paging<List<Audio>>> {
        return globalSearchRepository.findAudios(query, page)
    }

    override fun searchTag(): String = "AudiosSearch"

    override fun getSearchBuffer(): Int = Constants.PROFILE_SEARCH_LIST_PAGINATION_BUFFER

    override fun addToRecentSearch(query: String) {
        globalSearchRepository.addToRecentSearches(query)
    }
}