package com.gbksoft.neighbourhood.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor()
        return constructor.newInstance()
    }
}