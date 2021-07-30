package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.response.ConfigModel
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.static_page.StaticPageModel
import io.reactivex.Observable
import retrofit2.http.GET

interface ApiAnyBody {
    @GET(UrlsWithoutToken.CONFIG)
    fun getConfig(): Observable<BaseResponse<ConfigModel>>

    @GET(UrlsWithoutToken.PAGE_ABOUT)
    fun getPageAbout(): Observable<BaseResponse<StaticPageModel>>

    @GET(UrlsWithoutToken.PAGE_PRIVACY)
    fun getPagePrivacyPolicy(): Observable<BaseResponse<StaticPageModel>>

    @GET(UrlsWithoutToken.PAGE_TERMS)
    fun getPageTermsAndConditions(): Observable<BaseResponse<StaticPageModel>>
}