package com.gbksoft.neighbourhood.ui.fragments.static_pages.about

import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.mvvm.SimpleViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageFragment
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageViewModel

class AboutFragment : StaticPageFragment() {
    override fun provideViewModel(): StaticPageViewModel {
        return ViewModelProvider(viewModelStore, SimpleViewModelFactory())
            .get(AboutViewModel::class.java)
    }

    override fun provideTitle(): Int = R.string.title_about

}