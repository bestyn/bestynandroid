package com.gbksoft.neighbourhood.ui.contract.system_bars

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.gbksoft.neighbourhood.R

class SystemBarColorizer(val activity: Activity) {

    @ColorInt
    private var statusBarColor: Int = activity.resources.getColor(R.color.screen_background_color)

    @ColorInt
    private var navigationBarColor: Int = activity.resources.getColor(R.color.navigation_bar_bg)

    init {
        colorizeStatusBar(statusBarColor, false)
        colorizeNavigationBar(navigationBarColor, false)
        colorizeNavigationBarIcons(false)
    }

    fun setStatusBarColor(@ColorRes colorRes: Int) {
        val color = activity.resources.getColor(colorRes)
        colorizeStatusBarIcons(isColorDark(color))
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness: Double = 1 - (0.299 * Color.red(color)
            + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.3
    }

    fun setNavigationBarColor(@ColorRes colorRes: Int) {
        val color = activity.resources.getColor(colorRes)
        if (isNotNavigationBarThisColor(color)) {
            colorizeNavigationBar(color, isColorDark(color))
        }
        colorizeNavigationBarIcons(isColorDark(color))
    }

    private fun isNotNavigationBarThisColor(@ColorInt color: Int): Boolean {
        return activity.window.navigationBarColor != color
    }

    private fun colorizeStatusBar(@ColorInt color: Int, isLightIcons: Boolean) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
        colorizeStatusBarIcons(isLightIcons)
    }

    private fun colorizeStatusBarIcons(isLightIcons: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val window = activity.window
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (isLightIcons) {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }

    private fun colorizeNavigationBar(@ColorInt color: Int, isLightIcons: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val window = activity.window
        window.navigationBarColor = color
        window.decorView

        colorizeNavigationBarIcons(isLightIcons)
    }

    private fun colorizeNavigationBarIcons(isLightIcons: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val window = activity.window
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (isLightIcons) {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        } else {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }

}