package com.gbksoft.neighbourhood.mvvm

import androidx.lifecycle.ViewModel

class SimpleViewModelFactory : ViewModelFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor()
        return constructor.newInstance()
    }
}