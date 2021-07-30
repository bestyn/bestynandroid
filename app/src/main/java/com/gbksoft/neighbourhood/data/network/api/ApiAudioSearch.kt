package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.search.AudioSearchResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiAudioSearch {
    @GET("v1/audio")
    fun findAudios(@Query("description") description: String,
                   @Query("page") page: Int,
                   @Query("perPage") perPage: Int,
                   @Query("sort") sort: String,
                   @Query("expand") expand: String): Observable<BaseResponse<List<AudioSearchResult>>>
}