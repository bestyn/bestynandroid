package com.gbksoft.neighbourhood.ui.fragments.base.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

abstract class BaseNavigator(protected val fragment: Fragment) {
    protected fun findNavController() = fragment.findNavController()

    open fun popBackStack() {
        findNavController().popBackStack()
    }

    open fun <DATA> subscribeToResult(key: String, observer: (data: DATA) -> Unit) {
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<DATA>(key)
            ?.observe(fragment.viewLifecycleOwner, Observer { observer.invoke(it) })
    }
}