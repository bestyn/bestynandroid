package com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentTabBusinessInfoBinding
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.adapter.BusinessInfoAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.component.ProfileInfoItemClickHandler


class BusinessInfoFragment : BaseFragment() {
    private lateinit var layout: FragmentTabBusinessInfoBinding
    private lateinit var viewModel: BusinessInfoViewModel
    private lateinit var infoAdapter: BusinessInfoAdapter
    private val profileInfoItemClickHandler by lazy {
        ProfileInfoItemClickHandler(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ContextViewModelFactory(requireContext()))
            .get(BusinessInfoViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_business_info, container, false)
        setupView()
        setClickListeners()
        subscribeToViewModel()
        return layout.root
    }

    private fun setupView() {
        infoAdapter = BusinessInfoAdapter()
        layout.infoList.adapter = infoAdapter
    }

    private fun setClickListeners() {
        infoAdapter.onInfoItemClickListener = {
            profileInfoItemClickHandler.handle(it)
        }
    }

    private fun subscribeToViewModel() {
        viewModel.profileInfo().observe(viewLifecycleOwner, Observer {
            infoAdapter.setData(it)
        })
    }
}