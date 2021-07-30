package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.post.CreatePostReq
import com.gbksoft.neighbourhood.data.models.request.post.UploadMediaReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.models.response.post.PostModel
import com.gbksoft.neighbourhood.data.models.response.post.PostReactionModel
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

interface ApiPost {

    @POST("v1/posts/general")
    fun createPostGeneral(@Query("expand") expand: String,
                          @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @POST("v1/posts/news")
    fun createPostNews(@Query("expand") expand: String,
                       @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @POST("v1/posts/crime")
    fun createPostCrime(@Query("expand") expand: String,
                        @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @POST("v1/posts/offer")
    fun createPostOffer(@Query("expand") expand: String,
                        @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @POST("v1/posts/event")
    fun createPostEvent(@Query("expand") expand: String,
                        @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @Streaming
    @Multipart
    @POST("v1/posts/media")
    fun createPostMedia(@Query("expand") expand: String,
                        @PartMap uploadMediaReq: UploadMediaReq): Observable<BaseResponse<FeedPostModel>>

    @PATCH("v1/posts/general/{id}")
    fun updatePostGeneral(@Path("id") postId: Long,
                          @Query("expand") expand: String,
                          @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @PATCH("v1/posts/news/{id}")
    fun updatePostNews(@Path("id") postId: Long,
                       @Query("expand") expand: String,
                       @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @PATCH("v1/posts/crime/{id}")
    fun updatePostCrime(@Path("id") postId: Long,
                        @Query("expand") expand: String,
                        @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @PATCH("v1/posts/offer/{id}")
    fun updatePostOffer(@Path("id") postId: Long,
                        @Query("expand") expand: String,
                        @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @PATCH("v1/posts/event/{id}")
    fun updatePostEvent(@Path("id") postId: Long,
                        @Query("expand") expand: String,
                        @Body body: CreatePostReq): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/general/{id}")
    fun getPostGeneral(@Path("id") postId: Long,
                       @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/news/{id}")
    fun getPostNews(@Path("id") postId: Long,
                    @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/crime/{id}")
    fun getPostCrime(@Path("id") postId: Long,
                     @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/offer/{id}")
    fun getPostOffer(@Path("id") postId: Long,
                     @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/event/{id}")
    fun getPostEvent(@Path("id") postId: Long,
                     @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/media/{id}")
    fun getPostMedia(@Path("id") postId: Long,
                     @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/story/{id}")
    fun getPostStory(@Path("id") postId: Long,
                     @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>

    @GET("v1/posts/{id}")
    fun getPost(@Path("id") postId: Long,
                @Query("expand") expand: String): Observable<BaseResponse<FeedPostModel>>


    @Streaming
    @Multipart
    @POST("v1/posts/{postId}/media")
    fun uploadPostMedia(@Path("postId") postId: Long,
                        @PartMap uploadMediaReq: UploadMediaReq): Call<BaseResponse<PostModel>>

    @DELETE("v1/posts/media/{mediaId}")
    fun deletePostMedia(@Path("mediaId") mediaId: Long): Call<BaseResponse<PostModel>>

    @DELETE("v1/posts/{postId}")
    fun deletePost(@Path("postId") postId: Long): Call<BaseResponse<Any>>

    @GET("v1/post/{id}/reaction")
    fun getPostReactions(@Path("id") postId: Long,
                         @Query("page") page: Int,
                         @Query("perPage") perPage: Int,
                         @Query("reaction") reaction: String?,
                         @Query("expand") expand: String): Observable<BaseResponse<List<PostReactionModel>>>

    @GET("v1/posts")
    fun getPosts(
            @Query("profileId") profileId: Long? = null,
            @Query("description") descriptionSearch: String? = null,
            @Query("hashtag") hashtag: String? = null,
            @Query("audioId") audioId: Long? = null,
            @Query("idAfter") idAfter: Long? = null,
            @Query("idBefore") idBefore: Long? = null,
            @Query("search") search: String? = null,
            @Query("types[]") types: List<String>? = null,
            @Query("categoriesIds[]") categories: List<String>? = null,
            @Query("fields") fields: String? = null,
            @Query("expand") expand: String? = null,
            @Query("sort") sort: String? = null,
            @Query("page") page: Int,
            @Query("perPage") perPage: Int): Observable<BaseResponse<List<FeedPostModel>>>
}