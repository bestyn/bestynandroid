package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.post_chat.UploadFileReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.file.AttachmentModel
import io.reactivex.Observable
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Streaming

interface ApiPostMessageAttachment {

    @Streaming
    @Multipart
    @POST("v1/post/message/attachment")
    fun uploadPostMedia(@PartMap uploadFileReq: UploadFileReq): Observable<BaseResponse<AttachmentModel>>

}