package com.gbksoft.neighbourhood.ui.fragments.static_pages.about

import com.gbksoft.neighbourhood.model.static_page.StaticPage
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageViewModel
import io.reactivex.Observable

class AboutViewModel : StaticPageViewModel() {

    override fun provideEndPoint(): Observable<StaticPage> {
        return staticPagesRepository.getAboutPage()
    }

}