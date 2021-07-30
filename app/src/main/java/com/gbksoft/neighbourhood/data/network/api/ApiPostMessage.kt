package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.post_chat.PostMessageReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.chat.PostMessageModel
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*

interface ApiPostMessage {
    @POST("v1/post/{id}/message")
    fun createPostMessage(@Path("id") postId: Long,
                          @Query("expand") expand: String,
                          @Body body: PostMessageReq): Observable<BaseResponse<PostMessageModel>>

    @GET("v1/post/{id}/message")
    fun getPostMessageList(@Path("id") postId: Long,
                           @Query("expand") expand: String,
                           @Query("lastId") lastId: Long?,
                           @Query("direction") direction: String,
                           @Query("perPage") perPage: Int
    ): Observable<BaseResponse<List<PostMessageModel>>>

    @PATCH("v1/post/message/{id}")
    fun updatePostMessage(@Path("id") messageId: Long,
                          @Query("expand") expand: String,
                          @Body body: PostMessageReq): Observable<BaseResponse<PostMessageModel>>

    @DELETE("v1/post/message/{id}")
    fun deleteMessage(@Path("id") messageId: Long): Completable
}