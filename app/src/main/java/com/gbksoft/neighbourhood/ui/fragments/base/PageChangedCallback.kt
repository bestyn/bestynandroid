package com.gbksoft.neighbourhood.ui.fragments.base

import androidx.annotation.Px
import androidx.viewpager2.widget.ViewPager2

class PageChangedCallback(val callback: (Int) -> Unit) : ViewPager2.OnPageChangeCallback() {
    private var pageScrollStateChanged = false

    override fun onPageScrolled(position: Int, positionOffset: Float, @Px positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
        pageScrollStateChanged = true
    }

    override fun onPageSelected(position: Int) {
        if (pageScrollStateChanged) {
            callback.invoke(position)
        }
        pageScrollStateChanged = false
    }

}