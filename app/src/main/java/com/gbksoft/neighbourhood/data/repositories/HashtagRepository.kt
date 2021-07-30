package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.hashtag.HashtagModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import io.reactivex.Observable

class HashtagRepository : BaseRepository() {

    private val sort = "-featured,-popularity"
    private val perPage = 50

    private val pagingHelper = PagingHelper<HashtagModel, Hashtag> {
        Hashtag(it.id, it.name)
    }

    fun loadHashtags(paging: Paging<List<Hashtag>>?, search: String?): Observable<Paging<List<Hashtag>>> {
        paging?.let {
            return loadHashtags(it.currentPage, it.itemsPerPage, search)
        } ?: run {
            return loadHashtags(1, perPage, search)
        }
    }

    private fun loadHashtags(page: Int, perPage: Int, search: String?): Observable<Paging<List<Hashtag>>> {
        return ApiFactory.apiHashtag.getHashtags(page, perPage, search, sort)
                .map { pagingHelper.getPagingResult(it) }
    }
}