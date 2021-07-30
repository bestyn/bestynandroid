package com.gbksoft.neighbourhood.ui.fragments.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils.parseError
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField.CONFIRM_NEW_PASSWORD
import com.gbksoft.neighbourhood.utils.validation.ValidationField.NEW_PASSWORD
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ResetPasswordViewModel(
    private val userRepository: UserRepository,
    private val resetToken: String
) : BaseViewModel() {
    private val _errorFields = MutableLiveData<ErrorFieldsModel>()
    val errorFields: LiveData<ErrorFieldsModel> = _errorFields

    private val _changePasswordResult = SingleLiveEvent<Boolean>()
    val changePasswordResult: LiveData<Boolean> = _changePasswordResult

    fun changePassword(newPassword: String, confirmNewPassword: String) {
        if (validation(newPassword, confirmNewPassword)) {
            addDisposable("changePassword", userRepository
                .setNewPassword(resetToken, newPassword, confirmNewPassword)
                .doOnSubscribe {
                    showLoader()
                    changeControlState(R.id.btnChangePassword, false)
                }
                .doOnTerminate {
                    hideLoader()
                    changeControlState(R.id.btnChangePassword, true)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _changePasswordResult.postValue(true) },
                    { parseError(it, errorsFuncs) },
                    { _changePasswordResult.postValue(true) }
                ))
        }
    }

    private fun validation(newPassword: String, confirmNewPassword: String): Boolean {
        val errorFieldsModel = ErrorFieldsModel()
        errorFieldsModel.addError(NEW_PASSWORD, validationUtils.validateFieldOnRequired(NEW_PASSWORD, newPassword))
        errorFieldsModel.addError(NEW_PASSWORD, validationUtils.validateFieldOnStringTooShort(NEW_PASSWORD, newPassword, Constants.MIN_PASSWORD_LENGTH))
        errorFieldsModel.addError(NEW_PASSWORD, validationUtils.validateFieldOnStringTooLong(NEW_PASSWORD, newPassword, Constants.MAX_PASSWORD_LENGTH))
        errorFieldsModel.addError(NEW_PASSWORD, validationUtils.validateFieldOnPassword(NEW_PASSWORD, newPassword))
        errorFieldsModel.addError(CONFIRM_NEW_PASSWORD, validationUtils.validateFieldOnRequired(CONFIRM_NEW_PASSWORD, confirmNewPassword))
        errorFieldsModel.addError(CONFIRM_NEW_PASSWORD, validationUtils.validateFieldOnCompareEqual(CONFIRM_NEW_PASSWORD, NEW_PASSWORD.attrSpecName, confirmNewPassword, newPassword))
        _errorFields.postValue(errorFieldsModel)
        return errorFieldsModel.isValid
    }
}