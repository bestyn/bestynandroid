package com.gbksoft.neighbourhood.ui.fragments.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils.parseError
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField.EMAIL
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ForgotPasswordViewModel(
    private val userRepository: UserRepository
) : BaseViewModel() {
    private val _errorFields = MutableLiveData<ErrorFieldsModel>()
    val errorFields: LiveData<ErrorFieldsModel> = _errorFields

    private val _resetPasswordResult = SingleLiveEvent<Boolean>()
    val resetPasswordResult: LiveData<Boolean> = _resetPasswordResult

    fun resetPassword(email: String) {
        if (validation(email)) {
            addDisposable("resetPassword", userRepository
                .recoveryPassword(email)
                .doOnSubscribe {
                    showLoader()
                    changeControlState(R.id.btnResetPassword, false)
                }
                .doOnTerminate {
                    hideLoader()
                    changeControlState(R.id.btnResetPassword, true)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _resetPasswordResult.postValue(true) },
                    { parseError(it, errorsFuncs) },
                    { _resetPasswordResult.postValue(true) }
                ))
        }
    }

    private fun validation(email: String): Boolean {
        val errorFieldsModel = ErrorFieldsModel()
        errorFieldsModel.addError(EMAIL, validationUtils.validateFieldOnRequired(EMAIL, email))
        errorFieldsModel.addError(EMAIL, validationUtils.validateFieldOnEmail(EMAIL, email))
        _errorFields.postValue(errorFieldsModel)
        return errorFieldsModel.isValid
    }
}