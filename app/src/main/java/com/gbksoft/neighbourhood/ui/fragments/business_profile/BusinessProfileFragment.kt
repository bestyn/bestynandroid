package com.gbksoft.neighbourhood.ui.fragments.business_profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentBusinessProfileBinding
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.crop.CropOptions
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.BusinessTabAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.OpenCropHandler
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.ShowInAlbumHandler
import com.gbksoft.neighbourhood.ui.fragments.profile.ProfileFragmentDirections
import com.gbksoft.neighbourhood.ui.widgets.actionbar.ActionBarEvent
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.include_follow_view.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusinessProfileFragment : SystemBarsColorizeFragment(), ShowInAlbumHandler, OpenCropHandler {
    private val viewModel by viewModel<BusinessProfileViewModel>()
    private lateinit var layout: FragmentBusinessProfileBinding
    private lateinit var tabAdapter: BusinessTabAdapter
    private var profile: BusinessProfile? = null

    private var currentTabPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabAdapter = BusinessTabAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        KeyboardUtils.hideKeyboard(requireActivity())
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_business_profile, container, false)
        viewModel.loadFollowedCount()
        viewModel.loadFollowersCount()

        setupViewPager()
        setupClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    override fun onDestroyView() {
        layout.viewPager.adapter = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        showNavigateBar()
    }

    @SuppressLint("WrongConstant")
    private fun setupViewPager() {
        layout.viewPager.adapter = tabAdapter
        layout.viewPager.offscreenPageLimit = BusinessTabAdapter.TABS_COUNT
        layout.viewPager.isSaveEnabled = false
        TabLayoutMediator(layout.tabLayout, layout.viewPager, TabConfigurationStrategy { tab, position ->
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
            BusinessTabAdapter.INFO_TAB_POSITION -> tab.setText(R.string.business_profile_tab_info)
            BusinessTabAdapter.IMAGES_TAB_POSITION -> tab.setText(R.string.business_profile_tab_images)
        }
    }

    private fun setupClickListeners() {
        layout.actionBar.setEventHandler {
            if (it == ActionBarEvent.SETTINGS) openProfileSettings()
        }
    }

    private fun openProfileSettings() {
        profile?.let {
            val direction =
                    BusinessProfileFragmentDirections.toEditBusinessProfileFragment()
            direction.businessProfileId = it.id
            findNavController().navigate(direction)
        }
    }

    private fun subscribeToViewModel() {
        viewModel.profile.observe(viewLifecycleOwner, Observer { onProfileLoaded(it) })
        viewModel.profileSwitched.observe(viewLifecycleOwner, Observer { onProfileSwitched(it) })
        viewModel.followersCount.observe(viewLifecycleOwner, Observer { onFollowersCountLoaded(it) })
        viewModel.followedCount.observe(viewLifecycleOwner, Observer { onFollowedCountLoaded(it) })
    }

    private fun onProfileLoaded(profile: BusinessProfile) {
        this.profile = profile
        layout.businessProfile = profile
        layout.chipGroup.setInterestList(profile.hashtags)
    }

    private fun onFollowersCountLoaded(followers: Int) {
        val followersColor = if (followers > 0) R.color.main_black else R.color.dark_grey
        layout.followers.textFollowersCount.setTextColor(ResourcesCompat.getColor(resources, followersColor, null))
        layout.followers.textFollowersCount.text = followers.toString()
        if (followers > 0) {
            layout.followers.btnFollowers.setOnClickListener { openFollowers() }
        }
    }

    private fun onFollowedCountLoaded(followed: Int) {
        val followedColor = if (followed > 0) R.color.main_black else R.color.dark_grey
        layout.followers.textFollowedCount.setTextColor(ResourcesCompat.getColor(resources, followedColor, null))
        layout.followers.textFollowedCount.text = followed.toString()
        if (followed > 0) {
            layout.followers.btnFollowed.setOnClickListener { openFollowed() }
        }
    }

    private fun onProfileSwitched(isBusiness: Boolean) {
        val direction = if (isBusiness) {
            ProfileFragmentDirections.switchToBusinessProfile()
        } else {
            ProfileFragmentDirections.switchToProfile()
        }
        findNavController().navigate(direction)
    }

    override fun showInAlbum(picture: Media.Picture, position: Int) {
        val profile = profile ?: return

        val publicProfile = PublicProfile(profile.id, true, profile.avatar, profile.name)
        val direction =
                BusinessProfileFragmentDirections.toAlbumList(publicProfile)
        direction.imagePosition = position
        findNavController().navigate(direction)
    }

    override fun openCrop(cropOptions: CropOptions) {
        val direction = BusinessProfileFragmentDirections.toCropImage(cropOptions)

        findNavController().navigate(direction)
    }

    private fun openFollowers() {
        val direction = BusinessProfileFragmentDirections.toFollowersFragment()
        findNavController().navigate(direction)
    }

    private fun openFollowed() {
        val direction = BusinessProfileFragmentDirections.toFollowedFragment()
        findNavController().navigate(direction)
    }
}