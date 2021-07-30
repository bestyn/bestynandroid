package com.gbksoft.neighbourhood.utils

import android.text.TextUtils
import android.widget.TextView
import com.google.android.material.tabs.TabLayout

object TabLayoutUtils {


    fun setupEllipsize(tab: TabLayout.Tab) {
        val tabView = tab.view
        for (i in 0 until tabView.childCount) {
            val tabViewChild = tabView.getChildAt(i)
            if (tabViewChild is TextView) {
                tabViewChild.maxLines = 1
                tabViewChild.isSingleLine = true
                tabViewChild.ellipsize = TextUtils.TruncateAt.MIDDLE
            }
        }

    }
}