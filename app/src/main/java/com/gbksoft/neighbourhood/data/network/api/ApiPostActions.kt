package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.post.PostReactionReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiPostActions {
    @POST("v1/post/{id}/follow")
    fun followPost(@Path("id") postId: Long): Observable<BaseResponse<Any>>

    @DELETE("v1/post/{id}/unfollow")
    fun unfollowPost(@Path("id") postId: Long): Observable<BaseResponse<Any>>

    @POST("v1/post/{id}/reaction")
    fun addPostReaction(@Path("id") postId: Long,
                        @Body reaction: PostReactionReq): Completable

    @DELETE("v1/post/{id}/reaction")
    fun removePostReaction(@Path("id") postId: Long): Completable
}