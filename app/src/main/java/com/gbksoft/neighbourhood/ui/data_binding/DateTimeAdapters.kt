package com.gbksoft.neighbourhood.ui.data_binding

import androidx.databinding.BindingAdapter
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import com.google.android.material.textfield.TextInputEditText

object DateTimeAdapters {
    @JvmStatic
    @BindingAdapter("app:dateTime")
    fun setDateTime(view: TextInputEditText, value: Long?) {
        value?.let {
            if (it > 0) {
                val formattedTime = DateTimeUtils.getDateTime(it)
                view.setText(formattedTime)
            } else {
                view.setText("")
            }
        } ?: run {
            view.setText("")
        }

    }
}