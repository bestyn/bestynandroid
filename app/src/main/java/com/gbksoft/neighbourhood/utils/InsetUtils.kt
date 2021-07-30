package com.gbksoft.neighbourhood.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.util.Pair
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object InsetUtils {

    var shouldRemovePaddings = false

    fun setWindowTransparency(activity: Activity, listener: OnSystemBarsSizeChangedListener?) {
        val window = activity.window
        //removeSystemInsets(window.getDecorView(), listener);
        //window.setNavigationBarColor(Color.TRANSPARENT);
        // set transparent for lollipop if your layout have dark layout
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.BLACK
        } else {
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun setTopAndBottomPaddingForRootView(view: View, bottomInset: Int, topInset: Int) {
        view.setPadding(view.paddingStart, topInset, view.paddingEnd, bottomInset)
    }

    private fun removeSystemInsets(view: View, listener: OnSystemBarsSizeChangedListener) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View?, insets: WindowInsetsCompat ->
            val desiredBottomInset = calculateDesiredBottomInset(
                view,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetBottom,
                listener
            )
            var topInset = 0
            // remove, if your layout have dark color,because on lollipop we are unable to change
            // text color on status bar (default white)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                topInset = insets.systemWindowInsetTop
            }
            ViewCompat.onApplyWindowInsets(
                view,
                insets.replaceSystemWindowInsets(0, topInset, 0, desiredBottomInset)
            )
        }
    }

    private fun calculateDesiredBottomInset(view: View, topInset: Int, bottomInset: Int, listener: OnSystemBarsSizeChangedListener): Int {
        val hasKeyboard = isKeyboardAppeared(view, bottomInset)
        var desiredBottomInset = 0
        if (hasKeyboard) {
            desiredBottomInset = bottomInset
        }
        var insetToListener = bottomInset
        if (hasKeyboard) {
            insetToListener = 0
        }
        listener.onBarSizesChanged(topInset, insetToListener)
        return desiredBottomInset
    }

    private fun isKeyboardAppeared(view: View, bottomInset: Int): Boolean {
        val height = view.context.resources.displayMetrics.heightPixels.toDouble()
        return bottomInset / height > .25
    }

    interface OnSystemBarsSizeChangedListener {
        fun onBarSizesChanged(statusBarSize: Int, navigationBarSize: Int): Pair<Int, Int>?
    }
}