package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.search.ProfileSearchModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiProfilesSearch {
    @GET("v1/profiles")
    fun findProfiles(@Query("fullName") fullName: String?,
                     @Query("isFollowed") isFollowed: Boolean?,
                     @Query("isFollower") isFollower: Boolean?,
                     @Query("type") type: String?,
                     @Query("sort") sort: String?,
                     @Query("page") page: Int,
                     @Query("perPage") perPage: Int,
                     @Query("expand") expand: String): Observable<BaseResponse<List<ProfileSearchModel>>>
}