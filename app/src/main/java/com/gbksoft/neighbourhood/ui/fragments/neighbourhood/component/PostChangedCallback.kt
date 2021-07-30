package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component

class PostChangedCallback(val callback: () -> Unit) {
    fun onChanged() {
        callback.invoke()
    }
}