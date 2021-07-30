package com.gbksoft.neighbourhood.data.network.api

import io.reactivex.Completable
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiFollowers {

    @POST("v1/profile/{id}/follow")
    fun followProfile(@Path("id") profileId: Long): Completable

    @DELETE("v1/profile/{id}/follow")
    fun unFollowProfile(@Path("id") profileId: Long): Completable

    @DELETE("v1/profile/{id}/follower")
    fun removeFollower(@Path("id") profileId: Long): Completable
}