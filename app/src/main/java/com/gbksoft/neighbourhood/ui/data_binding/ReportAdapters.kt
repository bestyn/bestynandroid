package com.gbksoft.neighbourhood.ui.data_binding

import android.widget.RadioButton
import androidx.databinding.BindingAdapter

object ReportAdapters {

    @JvmStatic
    @BindingAdapter("app:checkedTextColor", "app:uncheckedTextColor")
    fun setRadioButtonTextColor(radioButton: RadioButton, checkedColor: Int, uncheckedColor: Int) {
        radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setTextColor(checkedColor)
            } else {
                buttonView.setTextColor(uncheckedColor)
            }
        }
    }
}