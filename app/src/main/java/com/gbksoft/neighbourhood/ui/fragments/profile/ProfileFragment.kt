package com.gbksoft.neighbourhood.ui.fragments.profile

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentProfileBinding
import com.gbksoft.neighbourhood.model.crop.CropOptions
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.BusinessProfileFragmentDirections
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.OpenCropHandler
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.ShowInAlbumHandler
import com.gbksoft.neighbourhood.ui.fragments.profile.contract.StaticPagesDirectionHandler
import com.gbksoft.neighbourhood.ui.fragments.profile.tabs.SettingsTabAdapter
import com.gbksoft.neighbourhood.ui.widgets.actionbar.ActionBarEvent
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.include_follow_view.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : SystemBarsColorizeFragment(),
        StaticPagesDirectionHandler, ShowInAlbumHandler, OpenCropHandler {

    private lateinit var layout: FragmentProfileBinding
    private val viewModel by viewModel<ProfileViewModel>()

    private var tabAdapter: SettingsTabAdapter? = null
    private var profile: BasicProfile? = null

    private var currentTabPosition: Int? = null

    override fun getStatusBarColor() = R.color.screen_background_color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabAdapter = SettingsTabAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        KeyboardUtils.hideKeyboard(requireActivity())
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        viewModel.loadFollowedCount()
        viewModel.loadFollowersCount()
        viewModel.loadBasicProfile()

        setupViewPager()
        setupClickListeners()
        subscribeToViewModel()
        return layout.root
    }

    override fun onStart() {
        super.onStart()
        showNavigateBar()
    }

    override fun onDestroyView() {
        layout.viewPager.adapter = null
        super.onDestroyView()
    }

    @SuppressLint("WrongConstant")
    private fun setupViewPager() {
        layout.viewPager.adapter = tabAdapter
        layout.viewPager.offscreenPageLimit = SettingsTabAdapter.TABS_COUNT
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
            SettingsTabAdapter.INTERESTS_TAB_POSITION -> tab.setText(R.string.profile_tab_interests)
            SettingsTabAdapter.IMAGES_TAB_POSITION -> tab.setText(R.string.profile_tab_images)
        }
    }

    private fun setupClickListeners() {
        layout.actionBar.setEventHandler {
            if (it == ActionBarEvent.SETTINGS) openProfileSettings()
        }
    }

    private fun openProfileSettings() {
        val directions = ProfileFragmentDirections.navProfileToProfileSettings()
        findNavController().navigate(directions)
    }

    private fun subscribeToViewModel() {
        viewModel.getControlState().observe(viewLifecycleOwner, Observer { stateMap: Map<Int, List<Boolean>> ->
            updateControlsState(stateMap)
        })
        viewModel.profile.observe(viewLifecycleOwner, Observer { onProfileLoaded(it) })
        viewModel.profileSwitched.observe(viewLifecycleOwner, Observer { onProfileSwitched(it) })
        viewModel.followersCount.observe(viewLifecycleOwner, Observer { onFollowersCountLoaded(it) })
        viewModel.followedCount.observe(viewLifecycleOwner, Observer { onFollowedCountLoaded(it) })
    }

    private fun updateControlsState(stateMap: Map<Int, List<Boolean>>) {
    }

    private fun onProfileLoaded(profile: BasicProfile) {
        this.profile = profile
        layout.profile = profile
    }

    private fun onFollowersCountLoaded(followers: Int) {
        val followersColor = if (followers > 0) R.color.main_black else R.color.dark_grey
        layout.followers.textFollowersCount.setTextColor(ResourcesCompat.getColor(resources, followersColor, null))
        layout.followers.textFollowersCount.text = followers.toString()
        layout.followers.btnFollowers.isEnabled = followers > 0
        if (followers > 0) {
            layout.followers.btnFollowers.setOnClickListener { openFollowers() }
        }
    }

    private fun onFollowedCountLoaded(followed: Int) {
        val followedColor = if (followed > 0) R.color.main_black else R.color.dark_grey
        layout.followers.textFollowedCount.setTextColor(ResourcesCompat.getColor(resources, followedColor, null))
        layout.followers.textFollowedCount.text = followed.toString()
        layout.followers.btnFollowed.isEnabled = followed > 0
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

    override fun openPrivacyPolicy() {
        val direction = ProfileFragmentDirections.toPrivacyPolicy()
        try {
            findNavController().navigate(direction)
        } catch (ignored: IllegalStateException) {
        }
    }

    override fun openTermsAndConditions() {
        val direction = ProfileFragmentDirections.toTermsAndConditions()
        try {
            findNavController().navigate(direction)
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun openFollowers() {
        val direction = ProfileFragmentDirections.toFollowersFragment()
        findNavController().navigate(direction)
    }

    private fun openFollowed() {
        val direction = ProfileFragmentDirections.toFollowedFragment()
        findNavController().navigate(direction)
    }

    override fun openAboutApp() {
        val link = getString(R.string.about_app_link)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(requireContext(), R.string.browser_not_found_msg)
        }
    }

    override fun showInAlbum(picture: Media.Picture, position: Int) {
        val profile = profile ?: return
        val publicProfile = PublicProfile(profile.id, false, profile.avatar, profile.fullName)
        val direction = ProfileFragmentDirections.toAlbumList(publicProfile)
        direction.imagePosition = position
        findNavController().navigate(direction)
    }

    override fun openCrop(cropOptions: CropOptions) {
        val direction = BusinessProfileFragmentDirections.toCropImage(cropOptions)
        findNavController().navigate(direction)
    }
}