package com.gbksoft.neighbourhood.ui.fragments.profile.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.BusinessImagesFragment

class SettingsTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val interestsFragment: InterestsFragment = InterestsFragment()
    private val imagesFragment: BusinessImagesFragment = BusinessImagesFragment()

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            INTERESTS_TAB_POSITION -> interestsFragment
            IMAGES_TAB_POSITION -> imagesFragment
            else -> throw IllegalStateException("SettingsTabAdapter position: $position")
        }
    }

    override fun getItemCount(): Int {
        return TABS_COUNT
    }

    companion object {
        const val TABS_COUNT = 2
        const val INTERESTS_TAB_POSITION = 0
        const val IMAGES_TAB_POSITION = 1
    }

}