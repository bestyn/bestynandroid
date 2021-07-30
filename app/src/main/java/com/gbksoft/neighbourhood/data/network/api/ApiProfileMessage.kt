package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.post_chat.PostMessageReq
import com.gbksoft.neighbourhood.data.models.request.private_chat.MessageIds
import com.gbksoft.neighbourhood.data.models.request.private_chat.PrivateMessageReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.chat.ChatMessageModel
import com.gbksoft.neighbourhood.data.models.response.chat.ConversationModel
import com.gbksoft.neighbourhood.data.models.response.chat.OnlineProfilesModel
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*

interface ApiProfileMessage {

    @GET("v1/profile-conversation")
    fun getConversationList(
        @Query("search") search: String?,
        @Query("expand") expand: String,
        @Query("perPage") perPage: Int,
        @Query("page") page: Int
    ): Observable<BaseResponse<List<ConversationModel>>>

    @PATCH("v1/profile-conversation/{id}/read")
    fun markConversationAsRead(@Path("id") chatId: Long): Completable

    @GET("v1/profile-conversation-by-collocutor")
    fun getConversationByOpponentId(
        @Query("collocutorId") opponentId: Long,
        @Query("expand") expand: String
    ): Observable<BaseResponse<ConversationModel>>

    @GET("v1/profile-conversation/{id}/message")
    fun getConversationMessagesByConversationId(
        @Path("id") chatId: Long,
        @Query("expand") expand: String,
        @Query("lastId") lastId: Long?,
        @Query("direction") direction: String,
        @Query("perPage") perPage: Int
    ): Observable<BaseResponse<List<ChatMessageModel>>>

    @POST("v1/profile-message")
    fun postMessage(
        @Query("expand") expand: String,
        @Body body: PrivateMessageReq
    ): Observable<BaseResponse<ChatMessageModel>>

    @PATCH("v1/profile-message/{id}")
    fun updateMessage(
        @Path("id") messageId: Long,
        @Query("expand") expand: String,
        @Body body: PostMessageReq
    ): Observable<BaseResponse<ChatMessageModel>>

    @DELETE("v1/profile-message/{id}")
    fun deleteMessage(@Path("id") messageId: Long): Completable

    @PATCH("v1/profile-message/read")
    fun markMessagesAsRead(@Body messageIds: MessageIds): Completable

    @PATCH("v1/profile-conversation/{id}/archive")
    fun archiveConversation(@Path("id") conversationId: Long): Completable

    @GET("v1/user/profile/online")
    fun checkIsProfilesOnline(@Query("ids[]") ids: List<Long>): Observable<BaseResponse<OnlineProfilesModel>>
}