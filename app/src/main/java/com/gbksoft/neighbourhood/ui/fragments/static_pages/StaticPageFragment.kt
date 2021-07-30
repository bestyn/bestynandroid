package com.gbksoft.neighbourhood.ui.fragments.static_pages

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentStaticPageBinding
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment

abstract class StaticPageFragment : SystemBarsColorizeFragment() {

    private lateinit var layout: FragmentStaticPageBinding
    private lateinit var viewModel: StaticPageViewModel

    override fun getStatusBarColor() = R.color.static_page_bg
    override fun getNavigationBarColor() = R.color.static_page_bg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = provideViewModel()
    }

    abstract fun provideViewModel(): StaticPageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_static_page, container, false)

        hideNavigateBar()

        layout.actionBar.setTitle(provideTitle())
        viewModel.staticPageContent.observe(viewLifecycleOwner, Observer { setContent(it) })

        return layout.root
    }

    @StringRes
    abstract fun provideTitle(): Int

    private fun setContent(content: String) {
        layout.tvContent.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(content)
        }
    }

}