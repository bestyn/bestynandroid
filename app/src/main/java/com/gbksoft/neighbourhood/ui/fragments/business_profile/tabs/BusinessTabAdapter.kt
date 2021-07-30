package com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BusinessTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val infoFragment = BusinessInfoFragment()
    private val imagesFragment = BusinessImagesFragment()

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