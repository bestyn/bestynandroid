package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentPublicBusinessProfileBinding
import com.gbksoft.neighbourhood.model.business_profile.PublicBusinessProfile
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.ui.data_binding.ProfileAdapters
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.adapter.PublicBusinessTabAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.tabs.InfoFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.ShowInAlbumHandler
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import com.gbksoft.neighbourhood.ui.widgets.actionbar.IOnOptionsMenuItemClick
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PublicBusinessProfileFragment : SystemBarsColorizeFragment(), IOnOptionsMenuItemClick, ShowInAlbumHandler {
    private val args by navArgs<PublicBusinessProfileFragmentArgs>()
    private val viewModel by viewModel<PublicBusinessProfileViewModel> {
        parametersOf(args.profileId)
    }
    private lateinit var layout: FragmentPublicBusinessProfileBinding
    private lateinit var tabAdapter: PublicBusinessTabAdapter
    private val infoFragment by lazy { InfoFragment() }
    private var businessProfile: PublicBusinessProfile? = null
    private var currentTabPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabAdapter = PublicBusinessTabAdapter(this)
        childFragmentManager.beginTransaction().add(R.id.infoFragment, infoFragment).commit()
    }

    override fun onDestroyView() {
        layout.viewPager.adapter = null
        super.onDestroyView()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_public_business_profile,
                container, false)

        viewModel.loadProfile()
        hideNavigateBar()

        setupView()
        setClickListeners()

        subscribeToViewModel()
        return layout.root
    }

    @SuppressLint("WrongConstant")
    private fun setupView() {
        layout.viewPager.adapter = tabAdapter
        layout.viewPager.offscreenPageLimit = PublicBusinessTabAdapter.TABS_COUNT
        layout.viewPager.isSaveEnabled = false
        TabLayoutMediator(layout.tabLayout, layout.viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            setTabTitle(tab, position)
        }).attach()
        layout.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
        currentTabPosition?.let {
            layout.viewPager.setCurrentItem(it, false)
        }
        layout.actionBar.setOptionsMenuClickListener(this)
    }

    private fun setTabTitle(tab: TabLayout.Tab, position: Int) {
        when (position) {
            PublicBusinessTabAdapter.INFO_TAB_POSITION -> tab.setText(R.string.business_profile_tab_info)
            PublicBusinessTabAdapter.IMAGES_TAB_POSITION -> tab.setText(R.string.business_profile_tab_images)
        }
    }

    private fun setClickListeners() {
        layout.btnSendMessage.setOnClickListener { openChatRoom() }
        layout.btnFollow.setOnClickListener {
            when (businessProfile?.followType) {
                FollowType.FOLLOW -> viewModel.followProfile()
                FollowType.FOLLOW_BACK -> viewModel.followProfile()
                FollowType.FOLLOWING -> viewModel.unfollowProfile()
            }
        }
    }

    private fun openChatRoom() {
        val businessProfile = businessProfile ?: return
        val chatRoomData = ChatRoomData(null, businessProfile.id,
                businessProfile.name, businessProfile.avatar?.getSmall(), true)
        val direction =
                PublicBusinessProfileFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(direction)
    }

    private fun subscribeToViewModel() {
        viewModel.getControlState().observe(viewLifecycleOwner, Observer { updateControlsState(it) })
        viewModel.getProfile().observe(viewLifecycleOwner, Observer { handleProfile(it.first, it.second) })
        viewModel.followTypeLiveEvent.observe(viewLifecycleOwner, Observer { handleFollowType(it) })
        viewModel.removeFollowerLiveEvent.observe(viewLifecycleOwner, Observer {
            if (it) {
                layout.actionBar.hideItem()
                viewModel.loadProfile()
            }
        })
    }

    private fun updateControlsState(stateMap: Map<Int, List<Boolean>>) {
        layout.btnSendMessage.isClickable = controlStateIsActive(R.id.btnSendMessage, stateMap)
    }

    private fun handleProfile(profile: PublicBusinessProfile, hasImages: Boolean) {
        this.businessProfile = profile
        layout.businessProfile = profile
        layout.chipGroup.setInterestList(profile.hashtags)

        if (hasImages) {
            tabAdapter.setProfile(profile)
            showTabs()
        } else {
            infoFragment.disableTopPadding()
            infoFragment.setProfile(profile)
            showBusinessInformation()
        }

        if (profile.isFollower.not()) {
            layout.actionBar.hideItem()
        }
    }

    private fun handleFollowType(followType: FollowType) {
        businessProfile?.followType = followType
        ProfileAdapters.setProfileFollowType(layout.btnFollow, followType)
    }

    private fun showBusinessInformation() {
        layout.infoFragment.visibility = View.VISIBLE
        layout.tvInfoTitle.visibility = View.VISIBLE
        layout.viewPager.visibility = View.GONE
        layout.tabLayout.visibility = View.GONE
    }

    private fun showTabs() {
        layout.infoFragment.visibility = View.GONE
        layout.tvInfoTitle.visibility = View.GONE
        layout.viewPager.visibility = View.VISIBLE
        layout.tabLayout.visibility = View.VISIBLE
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionReport -> {
                businessProfile?.let {
                    val direction =
                            PublicBusinessProfileFragmentDirections.toReportUserFragment(it)
                    findNavController().navigate(direction)
                }
                true
            }
            R.id.actionRemoveFollower -> {
                viewModel.removeFollower()
                true
            }
            else -> false
        }
    }

    override fun showInAlbum(picture: Media.Picture, position: Int) {
        val profile = businessProfile ?: return

        val publicProfile = PublicProfile(profile.id, true, profile.avatar, profile.name)
        val direction =
                PublicBusinessProfileFragmentDirections.toAlbumList(publicProfile)
        direction.imagePosition = position
        findNavController().navigate(direction)
    }
}