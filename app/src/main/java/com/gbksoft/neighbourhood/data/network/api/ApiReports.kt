package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.report.ReportModel
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiReports {

    @POST("v1/report")
    fun report(@Body reportModel: ReportModel): Observable<BaseResponse<Any>>
}