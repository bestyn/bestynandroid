package com.gbksoft.neighbourhood.ui.fragments.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.shared_prefs.SharedStorage
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.activities.base.BaseActivity
import com.gbksoft.neighbourhood.ui.activities.base.LogoutHandler
import com.gbksoft.neighbourhood.utils.DimensionUtil
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorsMessageUtils
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils


open class BaseFragment : Fragment() {
    protected var sharedStorage: SharedStorage = NApplication.sharedStorage
    protected var errorsMessageUtils: ErrorsMessageUtils? = null
    protected var validationUtils: ValidationUtils? = null
    val viewModelFactory: ViewModelProvider.Factory = ViewModelFactory()
    protected val requireArgs: Bundle by lazy {
        if (arguments == null) arguments = Bundle()
        requireArguments()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (errorsMessageUtils == null) {
            errorsMessageUtils = NApplication.errorsMessageUtils
        }
        if (validationUtils == null) {
            validationUtils = NApplication.validationUtils
        }
    }

    fun hideNavigateBar() {
        getParentActivity()?.hideFloatingMenu()
    }

    fun showNavigateBar() {
        getParentActivity()?.showFloatingMenu()
    }

    fun getParentActivity(): BaseActivity? {
        val activity = activity
        return if (activity is BaseActivity) activity else null
    }

    fun controlStateIsActive(viewId: Int, stateMap: Map<Int, List<Boolean>>?): Boolean {
        if (stateMap == null) {
            return true
        }
        return if (stateMap.containsKey(viewId)) {
            val list = stateMap[viewId]
            !(list != null && list.isNotEmpty())
        } else {
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeOnKeyboardVisibility(view)
        hideOnTouch(getHideKeyboardOnTouchViews())
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, BackPressedCallback())
    }

    private fun subscribeOnKeyboardVisibility(view: View) {
        val keyboardHeight = DimensionUtil.dpToPx(200f, requireContext())
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = view.rootView.height - view.height
            // if more than 200 dp, it's probably a keyboard
            val isVisible = heightDiff > keyboardHeight
            if (isVisible) onKeyboardVisibilityChanged(isVisible)
            else view.post { onKeyboardVisibilityChanged(isVisible) }
        }
    }

    open fun onKeyboardVisibilityChanged(isVisible: Boolean) {
    }

    protected open fun getHideKeyboardOnTouchViews(): List<View> = emptyList()

    private fun hideOnTouch(views: List<View>) {
        for (view in views) {
            view.setOnTouchListener { v, _ ->
                KeyboardUtils.hideKeyboard(v)
                false
            }
        }
    }

    protected open fun handleOnBackPressed(): Boolean {
        return false
    }

    private inner class BackPressedCallback internal constructor() : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val isHandled: Boolean = this@BaseFragment.handleOnBackPressed()
            if (!isHandled) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

    protected fun logout() {
        val activity = activity
        if (activity is LogoutHandler) {
            activity.logout()
        }
    }
}