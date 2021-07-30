package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.news.NewsModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.news.NewsMapper
import com.gbksoft.neighbourhood.model.news.News
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Observable

class NewsRepository : BaseRepository() {
    private val pagingHelper = PagingHelper<NewsModel, News> {
        NewsMapper.toNews(it)
    }

    fun loadNews(page: Int): Observable<Paging<List<News>>> {
        return ApiFactory
            .apiNews
            .getNews(page, Constants.NEWS_PER_PAGE)
            .map {
                pagingHelper.getPagingResult(it)
            }
    }

}