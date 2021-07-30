package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.audio.CreateAudioReq
import com.gbksoft.neighbourhood.data.models.response.audio.AudioModel
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.views.MediaViewsResult
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*

interface ApiAudio {

    @GET("v1/audio")
    fun getAudio(
        @Query("description") searchByDescription: String? = null,
        @Query("profileId") searchByProfileId: Long? = null,
        @Query("isFavorite") isFavorite: Boolean? = null,
        @Query("sort") sort: String? = null,
        @Query("expand") expand: String? = null,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Observable<BaseResponse<List<AudioModel>>>

    @POST("v1/audio/{id}/favorite")
    fun addAudioToFavorites(@Path("id") id: Long): Completable

    @DELETE("v1/audio/{id}/favorite")
    fun removeAudioFromFavorites(@Path("id") id: Long): Completable

    @Multipart
    @POST("v1/audio")
    fun createAudio(@PartMap createAudioReq: CreateAudioReq): Completable

    @POST("v1/posts/media/{mediaId}/view")
    fun addAudioCounter(@Path("mediaId") mediaId: Int): Observable<BaseResponse<List<MediaViewsResult>>?>
}