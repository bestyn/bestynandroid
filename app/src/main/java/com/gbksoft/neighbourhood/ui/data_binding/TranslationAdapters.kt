package com.gbksoft.neighbourhood.ui.data_binding

import android.view.View
import androidx.databinding.BindingAdapter

object TranslationAdapters {
    @JvmStatic
    @BindingAdapter("translationX")
    fun setTranslationX(view: View, translation: Float) {
        view.translationX = translation
    }

    @JvmStatic
    @BindingAdapter("translationY")
    fun setTranslationY(view: View, translation: Float) {
        view.translationY = translation
    }
}