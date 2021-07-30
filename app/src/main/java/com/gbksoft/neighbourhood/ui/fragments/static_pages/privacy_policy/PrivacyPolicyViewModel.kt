package com.gbksoft.neighbourhood.ui.fragments.static_pages.privacy_policy

import com.gbksoft.neighbourhood.model.static_page.StaticPage
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageViewModel
import io.reactivex.Observable

class PrivacyPolicyViewModel : StaticPageViewModel() {

    override fun provideEndPoint(): Observable<StaticPage> {
        return staticPagesRepository.getPrivacyPolicy()
    }

}