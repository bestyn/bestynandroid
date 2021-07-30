package com.gbksoft.neighbourhood.ui.fragments.followers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentFollowersBinding
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener

abstract class ProfileListFragment : SystemBarsColorizeFragment() {

    protected abstract val viewModel: ProfileListViewModel
    protected lateinit var layout: FragmentFollowersBinding
    private lateinit var adapter: ProfileListAdapter
    private var selectedProfileTab: String? = null

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_followers, container, false)
        setupAdapter()
        setClickListeners()
        subscribeViewModel()
        hideNavigateBar()
        return layout.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFollowerInfo()
    }

    private fun setupAdapter() {
        adapter = ProfileListAdapter(resources)
        layout.rvFollowers.adapter = adapter
        layout.rvFollowers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        layout.rvFollowers.addOnScrollListener(lastVisiblePositionListener)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_profiles_search_result)?.let { divider ->
            val dividerDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            dividerDecoration.setDrawable(divider)
            layout.rvFollowers.addItemDecoration(dividerDecoration)
        }
    }

    private fun setClickListeners() {
        layout.tabBasic.setOnClickListener { handleTabBasicClick() }
        layout.tabBusiness.setOnClickListener { handleTabBusinessClick() }
        layout.searchView.onSearchClickListener = { handleOnSearchButtonClicked() }
        adapter.onProfileClickListener = { handleOnProfileClicked(it) }
        adapter.onOptionsButtonClickListener = { showBottomSheetDialog(it) }
        adapter.onFollowButtonClickListener = { handleOnFollowButtonClick(it) }
    }

    private fun subscribeViewModel() {
        viewModel.followerProfiles.observe(viewLifecycleOwner, Observer(this::onFollowersLoaded))
        viewModel.profilesCount.observe(viewLifecycleOwner, Observer(this::onProfilesCountLoaded))
    }

    private fun onFollowersLoaded(followers: List<ProfileSearchItem>) {
        showHideEmptyState(followers.isEmpty())
        adapter.setData(followers)
    }

    protected abstract fun showBottomSheetDialog(profile: ProfileSearchItem)

    private fun handleTabBasicClick() {
        selectedProfileTab =
                if (selectedProfileTab == ProfileListViewModel.PROFILE_TYPE_BASIC) null
                else ProfileListViewModel.PROFILE_TYPE_BASIC
        viewModel.setProfileType(selectedProfileTab)
    }

    private fun handleTabBusinessClick() {
        selectedProfileTab =
                if (selectedProfileTab == ProfileListViewModel.PROFILE_TYPE_BUSINESS) null
                else ProfileListViewModel.PROFILE_TYPE_BUSINESS
        viewModel.setProfileType(selectedProfileTab)
    }

    private fun handleOnSearchButtonClicked() {
        val query = layout.searchView.getCurrentQuery()
        viewModel.setSearchQuery(query)
        hideKeyboard()
    }

    private fun handleOnFollowButtonClick(profile: ProfileSearchItem) {
        if (profile.followType == FollowType.FOLLOWING) {
            viewModel.unfollowProfile(profile.id)
        } else {
            viewModel.followProfile(profile.id)
        }
    }

    private fun handleOnProfileClicked(profile: ProfileSearchItem) {
        if (profile.isBusiness) {
            navigateToPublicBusinessProfile(profile.id)
        } else {
            navigateToPublicProfile(profile.id)
        }
    }

    private fun hideKeyboard() {
        layout.searchView.clearFocus()
        KeyboardUtils.hideKeyboard(layout.searchView)
    }

    private fun showHideEmptyState(showEmptyState: Boolean) {
        layout.ivEmptyState.isVisible = showEmptyState
        layout.tvEmptyState.isVisible = showEmptyState
        layout.rvFollowers.isVisible = !showEmptyState
    }

    abstract fun onProfilesCountLoaded(profilesCount: Int)

    protected abstract fun navigateToReportUser(profileSearchItem: ProfileSearchItem)

    protected abstract fun navigateToPublicProfile(profileId: Long)

    protected abstract fun navigateToPublicBusinessProfile(profileId: Long)
}