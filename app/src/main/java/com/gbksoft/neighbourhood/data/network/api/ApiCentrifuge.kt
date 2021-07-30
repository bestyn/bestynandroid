package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.centrifuge.ChannelBody
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.centrifuge.CentrifugeAuthToken
import com.gbksoft.neighbourhood.data.models.response.centrifuge.CentrifugeChannelModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCentrifuge {

    @POST("v1/centrifugo/sign")
    fun sign(): Call<BaseResponse<CentrifugeAuthToken>>

    @POST("v1/centrifugo/auth")
    fun channelAuth(@Body channelBody: ChannelBody): Call<BaseResponse<CentrifugeChannelModel>>

}