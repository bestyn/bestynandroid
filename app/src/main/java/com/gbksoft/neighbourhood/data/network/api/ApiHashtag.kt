package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.hashtag.HashtagModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiHashtag {

    @GET("v1/hashtag")
    fun getHashtags(@Query("page") page: Int,
                    @Query("perPage") perPage: Int,
                    @Query("name") search: String?,
                    @Query("sort") sort: String): Observable<BaseResponse<List<HashtagModel>>>
}