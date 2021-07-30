package com.gbksoft.neighbourhood.ui.fragments.static_pages.terms_and_conditions

import com.gbksoft.neighbourhood.model.static_page.StaticPage
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageViewModel
import io.reactivex.Observable

class TermsAndConditionsViewModel : StaticPageViewModel() {

    override fun provideEndPoint(): Observable<StaticPage> {
        return staticPagesRepository.getTermsAndConditions()
    }

}