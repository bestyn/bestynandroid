package com.gbksoft.neighbourhood.ui.fragments.profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentTabInterestsBinding
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment

class InterestsFragment : BaseFragment() {
    private lateinit var layout: FragmentTabInterestsBinding
    private lateinit var viewModel: InterestsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
            .get(InterestsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_interests,
            container, false)
        setClickListeners()
        subscribeToViewModel()
        return layout.root
    }

    private fun setClickListeners() {
        layout.ivEditInterests.setOnClickListener { v: View? -> openEditInterests() }
    }

    private fun openEditInterests() {
        findNavController().navigate(R.id.editInterestsFragment)
    }

    private fun subscribeToViewModel() {
        viewModel.interest.observe(viewLifecycleOwner, Observer { handleInterests(it) })
    }

    private fun handleInterests(interests: List<Hashtag>) {
        layout.chipGroup.setInterestList(interests)
    }
}