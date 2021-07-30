package com.gbksoft.neighbourhood.ui.contract.system_bars

import androidx.annotation.ColorRes

interface SystemBarColorizerHost {
    fun setStatusBarColor(@ColorRes colorRes: Int)
    fun setNavigationBarColor(@ColorRes colorRes: Int)
}