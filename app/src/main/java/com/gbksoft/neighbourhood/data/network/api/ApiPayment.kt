package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.payment.PaymentReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.payment.SubscriptionModel
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiPayment {

    @GET("v1/in-app-payment")
    fun getLastPayment(): Single<BaseResponse<SubscriptionModel>>

    @POST("v1/in-app-payment")
    fun setPayment(@Body body: PaymentReq): Completable

    @DELETE("v1/in-app-payment")
    fun deletePayment(): Completable

}