package com.gbksoft.neighbourhood.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.gbksoft.neighbourhood.domain.utils.clearChildrenFocus

object KeyboardUtils {
    @JvmStatic
    fun hideKeyboard(activity: Activity?) {
        if (activity != null && activity.currentFocus != null) {
            (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    @JvmStatic
    fun hideKeyboard(view: View) {
        (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun hideKeyboardWithClearFocus(rootView: View, requestFocusView: View) {
        if (rootView is ViewGroup) rootView.clearChildrenFocus()
        (rootView.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(rootView.windowToken, 0)
        if (requestFocusView.isFocusable.not()) requestFocusView.isFocusable = true
        if (requestFocusView.isFocusableInTouchMode.not()) requestFocusView.isFocusableInTouchMode = true
        requestFocusView.requestFocus()
    }

    fun showKeyboard(view: View) {
        (view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}