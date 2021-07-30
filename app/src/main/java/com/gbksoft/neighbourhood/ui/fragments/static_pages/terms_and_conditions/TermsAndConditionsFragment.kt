package com.gbksoft.neighbourhood.ui.fragments.static_pages.terms_and_conditions

import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.mvvm.SimpleViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageFragment
import com.gbksoft.neighbourhood.ui.fragments.static_pages.StaticPageViewModel

class TermsAndConditionsFragment : StaticPageFragment() {
    override fun provideViewModel(): StaticPageViewModel {
        return ViewModelProvider(viewModelStore, SimpleViewModelFactory())
            .get(TermsAndConditionsViewModel::class.java)
    }

    override fun provideTitle(): Int = R.string.title_terms_and_conditions

}