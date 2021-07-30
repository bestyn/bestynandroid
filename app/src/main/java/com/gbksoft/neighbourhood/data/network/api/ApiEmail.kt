package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.email.ChangeEmailReq
import com.gbksoft.neighbourhood.data.models.request.email.ConfirmEmailReq
import com.gbksoft.neighbourhood.data.models.request.email.ResendEmailReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiEmail {
    @POST("v1/emails/verify/token")
    fun resendEmail(@Body resendEmailRequestBody: ResendEmailReq): Maybe<BaseResponse<Any?>>

    @PUT("v1/emails/verify/confirm")
    fun confirmEmail(@Body confirmEmailRequestBody: ConfirmEmailReq): Observable<BaseResponse<TokenModel>>

    @POST("v1/emails/change/token")
    fun changeEmail(@Body changeEmailReq: ChangeEmailReq): Maybe<BaseResponse<Any?>>

    @PUT("v1/emails/change/confirm")
    fun confirmChangedEmail(@Body confirmEmailRequestBody: ConfirmEmailReq): Completable
}