package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiMyPosts {
    @GET("v1/my-posts")
    fun getMyPosts(@Query("types[]") types: List<String>,
                   @Query("search") searchQuery: CharSequence?,
                   @Query("authorIsMe") authorIsMe: Int,
                   @Query("page") page: Int,
                   @Query("perPage") perPage: Int,
                   @Query("expand") expand: String): Observable<BaseResponse<List<FeedPostModel>>>
}