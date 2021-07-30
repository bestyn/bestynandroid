package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.news.NewsModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiNews {

    @GET("v1/news-feed")
    fun getNews(@Query("page") page: Int,
                @Query("perPage") perPage: Int): Observable<BaseResponse<List<NewsModel>>>
}