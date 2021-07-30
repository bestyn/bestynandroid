package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentTabBusinessInfoBinding
import com.gbksoft.neighbourhood.model.business_profile.PublicBusinessProfile
import com.gbksoft.neighbourhood.mvvm.SimpleViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.adapter.BusinessInfoAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.component.ProfileInfoItemClickHandler

class InfoFragment : BaseFragment() {
    private lateinit var layout: FragmentTabBusinessInfoBinding
    private lateinit var infoAdapter: BusinessInfoAdapter
    private var viewModel: InfoViewModel? = null
    private var savedTopPadding: Int? = null
    private var isTopPaddingDisabled = false
    private val profileInfoItemClickHandler by lazy {
        ProfileInfoItemClickHandler(requireActivity())
    }

    fun setProfile(profile: PublicBusinessProfile) {
        if (arguments == null) {
            arguments = Bundle()
        }
        requireArguments().putParcelable("profile", profile)
        viewModel?.init(profile)
    }

    fun disableTopPadding() {
        isTopPaddingDisabled = true
        if (this::layout.isInitialized) {
            checkTopPadding()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, SimpleViewModelFactory())
            .get(InfoViewModel::class.java)
        arguments?.let {
            viewModel!!.init(it.getParcelable("profile"))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_business_info, container, false)
        setupView()
        checkTopPadding()
        setClickListeners()
        subscribeToViewModel(viewModel!!)
        return layout.root
    }

    private fun setupView() {
        infoAdapter = BusinessInfoAdapter()
        layout.infoList.adapter = infoAdapter
    }

    private fun checkTopPadding() {
        if (savedTopPadding == null) {
            savedTopPadding = layout.scrollView.paddingTop
        }
        if (isTopPaddingDisabled) {
            layout.scrollView.setPadding(
                layout.scrollView.paddingLeft,
                0,
                layout.scrollView.paddingRight,
                layout.scrollView.paddingBottom
            )
        } else {
            layout.scrollView.setPadding(
                layout.scrollView.paddingLeft,
                savedTopPadding ?: 0,
                layout.scrollView.paddingRight,
                layout.scrollView.paddingBottom
            )
        }
    }

    private fun setClickListeners() {
        infoAdapter.onInfoItemClickListener = {
            profileInfoItemClickHandler.handle(it)
        }
    }

    private fun subscribeToViewModel(viewModel: InfoViewModel) {
        viewModel.profileInfo().observe(viewLifecycleOwner, Observer {
            infoAdapter.setData(it)
        })
    }
}