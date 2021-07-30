package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.mappers.static_page.StaticPageMapper
import com.gbksoft.neighbourhood.model.static_page.StaticPage
import io.reactivex.Observable

class StaticPagesRepository : BaseRepository() {

    fun getAboutPage(): Observable<StaticPage> {
        return ApiFactory
            .anyBody
            .getPageAbout()
            .map { StaticPageMapper.toStaticPage(it.requireResult()) }
    }

    fun getPrivacyPolicy(): Observable<StaticPage> {
        return ApiFactory
            .anyBody
            .getPagePrivacyPolicy()
            .map { StaticPageMapper.toStaticPage(it.requireResult()) }
    }

    fun getTermsAndConditions(): Observable<StaticPage> {
        return ApiFactory
            .anyBody
            .getPageTermsAndConditions()
            .map { StaticPageMapper.toStaticPage(it.requireResult()) }
    }
}