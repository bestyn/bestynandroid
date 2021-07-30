package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.request.email.ConfirmEmailReq
import com.gbksoft.neighbourhood.data.models.request.email.ResendEmailReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import com.gbksoft.neighbourhood.data.network.api.ApiEmail
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable

class EmailDataRepository(
    private val apiEmail: ApiEmail
) : BaseRepository() {
    fun resendEmail(email: String): Maybe<BaseResponse<Any?>> {
        val resendEmailReq = ResendEmailReq(email)
        return apiEmail
                .resendEmail(resendEmailReq)
    }

    fun confirmEmail(token: String, deviceId: String): Observable<TokenModel> {
        val confirmEmailReq = ConfirmEmailReq(token, deviceId)
        return apiEmail
                .confirmEmail(confirmEmailReq)
                .map { it.result }

    }

    fun confirmChangedEmail(token: String, deviceId: String): Completable {
        val confirmEmailReq = ConfirmEmailReq(token, deviceId)
        return apiEmail
                .confirmChangedEmail(confirmEmailReq)
    }
}