package com.gbksoft.neighbourhood.ui.fragments.profile.public_view.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gbksoft.neighbourhood.model.profile.PublicBasicProfile
import com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.tabs.ImagesFragment

class PublicTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val infoFragment = PublicInterestsFragment()
    private val imagesFragment = ImagesFragment()

    fun setProfile(profile: PublicBasicProfile) {
        infoFragment.setProfile(profile)
        imagesFragment.setProfile(profile.id)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            INFO_TAB_POSITION -> infoFragment
            IMAGES_TAB_POSITION -> imagesFragment
            else -> infoFragment
        }
    }

    override fun getItemCount(): Int {
        return TABS_COUNT
    }

    companion object {
        const val TABS_COUNT = 2
        const val INFO_TAB_POSITION = 0
        const val IMAGES_TAB_POSITION = 1
    }

}