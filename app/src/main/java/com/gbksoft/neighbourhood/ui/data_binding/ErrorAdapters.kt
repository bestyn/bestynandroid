package com.gbksoft.neighbourhood.ui.data_binding

import androidx.databinding.BindingAdapter
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.material.textfield.TextInputLayout

object ErrorAdapters {
    @JvmStatic
    @BindingAdapter("app:errorText", "app:errorField")
    fun setErrorMessage(view: TextInputLayout, errorsMap: Map<ValidationField?, String?>?, validationField: ValidationField?) {
        view.isErrorEnabled = errorsMap != null && errorsMap.containsKey(validationField)
        view.error = errorsMap?.get(validationField)
    }

}