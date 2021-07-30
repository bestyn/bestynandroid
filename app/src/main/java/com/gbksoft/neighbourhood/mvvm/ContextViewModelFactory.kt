package com.gbksoft.neighbourhood.mvvm

import android.content.Context
import androidx.lifecycle.ViewModel

class ContextViewModelFactory(val context: Context) : ViewModelFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor(Context::class.java)
        return constructor.newInstance(context)
    }
}