package com.gbksoft.neighbourhood.ui.fragments.profile.public_view

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
import com.gbksoft.neighbourhood.databinding.FragmentPublicProfileBinding
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.PublicBasicProfile
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.ui.data_binding.ProfileAdapters
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.ShowInAlbumHandler
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import com.gbksoft.neighbourhood.ui.fragments.profile.public_view.tabs.PublicInterestsFragment
import com.gbksoft.neighbourhood.ui.fragments.profile.public_view.tabs.PublicTabAdapter
import com.gbksoft.neighbourhood.ui.widgets.actionbar.IOnOptionsMenuItemClick
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PublicProfileFragment : SystemBarsColorizeFragment(), IOnOptionsMenuItemClick, ShowInAlbumHandler {
    private val args by navArgs<PublicProfileFragmentArgs>()
    private val viewModel by viewModel<PublicProfileViewModel> {
        parametersOf(args.profileId)
    }
    private lateinit var layout: FragmentPublicProfileBinding
    private lateinit var tabAdapter: PublicTabAdapter

    private val interestsFragment = PublicInterestsFragment()
    private var profile: PublicBasicProfile? = null
    private var currentTabPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabAdapter = PublicTabAdapter(this)
        childFragmentManager.beginTransaction().add(R.id.interestsFragment, interestsFragment).commit()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_public_profile, container, false)
        viewModel.loadProfile()
        hideNavigateBar()

        setupView()
        setClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    override fun onDestroyView() {
        layout.viewPager.adapter = null
        super.onDestroyView()
    }

    private fun setupView() {
        layout.actionBar.setOptionsMenuClickListener(this)
        layout.viewPager.adapter = tabAdapter
        layout.viewPager.offscreenPageLimit = PublicTabAdapter.TABS_COUNT
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
    }

    private fun setTabTitle(tab: TabLayout.Tab, position: Int) {
        when (position) {
            PublicTabAdapter.INFO_TAB_POSITION -> tab.setText(R.string.public_profile_tab_info)
            PublicTabAdapter.IMAGES_TAB_POSITION -> tab.setText(R.string.public_profile_tab_images)
        }
    }

    private fun setClickListeners() {
        layout.btnSendMessage.setOnClickListener { openChatRoom() }
        layout.btnFollow.setOnClickListener {
            when (profile?.followType) {
                FollowType.FOLLOW -> viewModel.followProfile()
                FollowType.FOLLOW_BACK -> viewModel.followProfile()
                FollowType.FOLLOWING -> viewModel.unfollowProfile()
            }
        }
    }

    private fun openChatRoom() {
        val profile = profile ?: return
        val chatRoomData = ChatRoomData(null, profile.id,
                profile.name, profile.avatar?.getSmall(), false)
        val direction =
                PublicProfileFragmentDirections.toChatRoom(chatRoomData)
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

    private fun handleProfile(profile: PublicBasicProfile, hasImages: Boolean) {
        this.profile = profile
        layout.publicProfile = profile
        tabAdapter.setProfile(profile)
        interestsFragment.setProfile(profile)

        if (hasImages) {
            showTabs()
        } else {
            showInterests()
        }

        if (profile.isFollower.not()) {
            layout.actionBar.hideItem()
        }
    }

    private fun handleFollowType(followType: FollowType) {
        profile?.followType = followType
        ProfileAdapters.setProfileFollowType(layout.btnFollow, followType)
    }

    private fun showInterests() {
        layout.interestsFragment.visibility = View.VISIBLE
        layout.tvInterestsTitle.visibility = View.VISIBLE
        layout.viewPager.visibility = View.GONE
        layout.tabLayout.visibility = View.GONE
    }

    private fun showTabs() {
        layout.interestsFragment.visibility = View.GONE
        layout.tvInterestsTitle.visibility = View.GONE
        layout.viewPager.visibility = View.VISIBLE
        layout.tabLayout.visibility = View.VISIBLE
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionReport -> {
                profile?.let {
                    val direction = PublicProfileFragmentDirections.toReportUserFragment(it)
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
        val profile = profile ?: return

        val publicProfile = PublicProfile(profile.id, false, profile.avatar, profile.name)
        val direction =
                PublicProfileFragmentDirections.toAlbumList(publicProfile)
        direction.imagePosition = position
        findNavController().navigate(direction)
    }
}