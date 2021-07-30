package com.gbksoft.neighbourhood.ui.fragments.auth

import android.content.Context
import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.repositories.EmailDataRepository
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.PlaceAddress
import com.gbksoft.neighbourhood.model.auth.SignUpModel
import com.gbksoft.neighbourhood.model.map.Coordinates
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SignUpViewModel(
    context: Context,
    private val userRepository: UserRepository,
    private val emailDataRepository: EmailDataRepository
) : BaseViewModel() {
    private val googleApiKey = context.getString(R.string.google_api_key)

    private val _errorFieldsModel = MutableLiveData<ErrorFieldsModel>()
    val errorFieldsModel = _errorFieldsModel as LiveData<ErrorFieldsModel>

    private val signUpModel = SignUpModel()
    private val _signUpModelData = MutableLiveData<SignUpModel>()
        .apply { value = signUpModel }
    val signUpModelData = _signUpModelData as LiveData<SignUpModel>

    private val _signUpResult = SingleLiveEvent<Pair<String, Boolean>>()
    val signUpResult = _signUpResult as LiveData<Pair<String, Boolean>>

    private val _resendEmailVerificationResult = SingleLiveEvent<Pair<String, Boolean>>()
    val resendEmailVerificationResult = _resendEmailVerificationResult
        as LiveData<Pair<String, Boolean>>

    private val _locationChanged = MutableLiveData<PlaceAddress>()
    val locationChanged = _locationChanged as LiveData<PlaceAddress>

    private val _scrollToError = SingleLiveEvent<ValidationField>()
    val scrollToError = _scrollToError as LiveData<ValidationField>

    fun validation(): Boolean {
        val errorFieldsModel = ErrorFieldsModel()

        errorFieldsModel.addError(ValidationField.ADDRESS, validationUtils.validateFieldOnRequired(ValidationField.ADDRESS, signUpModel.address))
        errorFieldsModel.addError(ValidationField.ADDRESS, validationUtils.validateFieldOnAddressCorrect(ValidationField.ADDRESS, signUpModel.addressComponents))

        errorFieldsModel.addError(ValidationField.FULL_NAME, validationUtils.validateFieldOnRequired(ValidationField.FULL_NAME, signUpModel.fullName))
        errorFieldsModel.addError(ValidationField.FULL_NAME, validationUtils.validateFieldOnStringTooShort(ValidationField.FULL_NAME, signUpModel.fullName, Constants.FULL_NAME_MIN_LENGTH))
        errorFieldsModel.addError(ValidationField.FULL_NAME, validationUtils.validateFieldOnStringTooLong(ValidationField.FULL_NAME, signUpModel.fullName, Constants.FULL_NAME_MAX_LENGTH))

        errorFieldsModel.addError(ValidationField.EMAIL, validationUtils.validateFieldOnRequired(ValidationField.EMAIL, signUpModel.email))
        errorFieldsModel.addError(ValidationField.EMAIL, validationUtils.validateFieldOnEmail(ValidationField.EMAIL, signUpModel.email))

        errorFieldsModel.addError(ValidationField.PASSWORD, validationUtils.validateFieldOnRequired(ValidationField.PASSWORD, signUpModel.password))
        errorFieldsModel.addError(ValidationField.PASSWORD, validationUtils.validateFieldOnStringTooShort(ValidationField.PASSWORD, signUpModel.password, Constants.MIN_PASSWORD_LENGTH))
        errorFieldsModel.addError(ValidationField.PASSWORD, validationUtils.validateFieldOnStringTooLong(ValidationField.PASSWORD, signUpModel.password, Constants.MAX_PASSWORD_LENGTH))
        errorFieldsModel.addError(ValidationField.PASSWORD, validationUtils.validateFieldOnPassword(ValidationField.PASSWORD, signUpModel.password))

        errorFieldsModel.addError(ValidationField.CONFIRM_PASSWORD, validationUtils.validateFieldOnRequired(ValidationField.CONFIRM_PASSWORD, signUpModel.confirmPassword))
        errorFieldsModel.addError(ValidationField.CONFIRM_PASSWORD, validationUtils.validateFieldOnCompareEqual(ValidationField.CONFIRM_PASSWORD, ValidationField.PASSWORD.attrSpecName, signUpModel.confirmPassword, signUpModel.password))

        _errorFieldsModel.postValue(errorFieldsModel)
        if (!errorFieldsModel.isValid) {
            scrollToError(errorFieldsModel)
        }
        return errorFieldsModel.isValid
    }

    fun signUp(view: View) {
        KeyboardUtils.hideKeyboard(view)
        val email = signUpModel.email
        addDisposable("createConference", userRepository.signUp(
            signUpModel.addressPlaceId,
            signUpModel.fullName,
            signUpModel.email,
            signUpModel.password)
            .doOnSubscribe {
                showLoader()
                changeControlState(view.id, false)
            }
            .doOnTerminate {
                hideLoader()
                changeControlState(view.id, true)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Analytics.onSignedUp()
                    _signUpResult.postValue(Pair(email, true))
                },
                { ParseErrorUtils.parseError(it, errorsFuncs) },
                { _signUpResult.postValue(Pair(email, true)) }))
    }

    fun resendEmailVerification(email: String) {
        addDisposable("resendEmail", emailDataRepository
            .resendEmail(signUpModel.email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _resendEmailVerificationResult.value = Pair(email, true)
            }, {
                _resendEmailVerificationResult.value = Pair(email, false)
                ParseErrorUtils.parseError(it, errorsFuncs)
            }, {
                _resendEmailVerificationResult.value = Pair(email, true)
            }))
    }

    private fun scrollToError(errorFieldsModel: ErrorFieldsModel) {
        val fields = errorFieldsModel.errorsMap.keys
        when {
            fields.contains(ValidationField.ADDRESS) -> _scrollToError.value = ValidationField.ADDRESS
            fields.contains(ValidationField.FULL_NAME) -> _scrollToError.value = ValidationField.FULL_NAME
            fields.contains(ValidationField.EMAIL) -> _scrollToError.value = ValidationField.EMAIL
            fields.contains(ValidationField.MY_INTERESTS) -> _scrollToError.value = ValidationField.MY_INTERESTS
            fields.contains(ValidationField.PASSWORD) -> _scrollToError.value = ValidationField.PASSWORD
            fields.contains(ValidationField.CONFIRM_PASSWORD) -> _scrollToError.value = ValidationField.CONFIRM_PASSWORD
        }
    }

    fun onLocationChanged(location: Location) {
        addDisposable("onLocationChanged", userRepository
            .getAddress(Coordinates.parse(location), googleApiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _locationChanged.value = it
            }, {
                it.printStackTrace()
            }))
    }
}