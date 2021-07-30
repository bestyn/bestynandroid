package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.story.CreateStoryReq
import com.gbksoft.neighbourhood.data.models.request.story.UpdateStoryReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import io.reactivex.Single
import retrofit2.http.*

interface ApiPostStory {
    @Multipart
    @POST("v1/posts/story")
    fun createStory(@PartMap req: CreateStoryReq,
                    @Query("expand") expand: String): Single<BaseResponse<Any>>

    @Multipart
    @PATCH("v1/posts/story/{id}")
    fun updateStory(@Path("id") id: Long,
                    @PartMap req: UpdateStoryReq,
                    @Query("expand") expand: String): Single<BaseResponse<FeedPostModel>>
}