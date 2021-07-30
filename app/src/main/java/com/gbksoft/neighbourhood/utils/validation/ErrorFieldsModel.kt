package com.gbksoft.neighbourhood.utils.validation

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import java.util.*

class ErrorFieldsModel : BaseObservable() {
    @Bindable
    val errorsMap: MutableMap<ValidationField, String> = LinkedHashMap()

    fun addError(key: ValidationField?, error: String?) {
        if (key == null || error.isNullOrEmpty()) return

        errorsMap[key]?.let { errors ->
            errorsMap[key] = errors
        } ?: run {
            errorsMap[key] = error
        }
        notifyPropertyChanged(BR.errorsMap)
    }

    fun clearError(key: ValidationField) {
        errorsMap.remove(key)
        notifyPropertyChanged(BR.errorsMap)
    }

    val isValid: Boolean
        get() {
            var countError = 0
            for ((key, value) in errorsMap) {
                countError++
            }
            return countError == 0
        }
}