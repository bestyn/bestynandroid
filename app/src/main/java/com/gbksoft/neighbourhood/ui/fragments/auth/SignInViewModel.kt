package com.gbksoft.neighbourhood.ui.fragments.auth

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.models.response.base.ErrorResponse
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.EmailDataRepository
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.DeviceIdProvider
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.auth.SignInModel
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.notifications.worker.FirebaseTokenWorkManager
import com.gbksoft.neighbourhood.utils.validation.ErrorCodes
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class SignInViewModel(
    private val deviceIdProvider: DeviceIdProvider,
    private val userRepository: UserRepository,
    private val emailDataRepository: EmailDataRepository,
    private val profileRepository: ProfileRepository
) : BaseViewModel() {

    private val _errorFields = SingleLiveEvent<ErrorFieldsModel>()
    val errorFields = _errorFields as LiveData<ErrorFieldsModel>

    private val _signInResult = SingleLiveEvent<Boolean>()
    val signInResult = _signInResult as LiveData<Boolean>

    private val _emailNotVerified = SingleLiveEvent<Boolean>()
    val emailNotVerified = _emailNotVerified as LiveData<Boolean>

    private val _resendEmailVerificationResult = SingleLiveEvent<Pair<String, Boolean>>()
    val resendEmailVerificationResult = _resendEmailVerificationResult
        as LiveData<Pair<String, Boolean>>

    private val _userBlocked = SingleLiveEvent<Boolean>()
    val userBlocked = _userBlocked as LiveData<Boolean>

    init {
        errorHandler.on422Callback = { onError422(it) }
    }

    fun signIn(view: View, signInModel: SignInModel) = viewModelScope.launch {
        if (validation(signInModel)) {
            val deviceId = deviceIdProvider.getDeviceId()

            addDisposable("signIn", userRepository.signInWithEmail(
                signInModel.email,
                signInModel.password,
                deviceId)
                .doOnSubscribe {
                    showLoader()
                    changeControlState(view.id, false)
                }
                .doOnError {
                    hideLoader()
                    changeControlState(view.id, true)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ tokenModel: TokenModel ->
                    accessTokenRepository.saveTokenData(tokenModel)
                    loadCurrentUser(view)
                }, { ParseErrorUtils.parseError(it, errorsFuncs) }))
        }
    }

    private fun validation(signInModel: SignInModel): Boolean {
        val errorFieldsModel = ErrorFieldsModel()

        val requiredEmailError = validationUtils.validateFieldOnRequired(
            ValidationField.EMAIL, signInModel.email)
        errorFieldsModel.addError(ValidationField.EMAIL, requiredEmailError)

        val invalidEmailError = validationUtils.validateFieldOnEmail(
            ValidationField.EMAIL, signInModel.email)
        errorFieldsModel.addError(ValidationField.EMAIL, invalidEmailError)

        val requiredPasswordError = validationUtils.validateFieldOnRequired(
            ValidationField.PASSWORD, signInModel.password)
        errorFieldsModel.addError(ValidationField.PASSWORD, requiredPasswordError)

        _errorFields.postValue(errorFieldsModel)
        return errorFieldsModel.isValid
    }

    private fun loadCurrentUser(view: View) {
        addDisposable("loadCurrentUser", profileRepository
            .getCurrentUserFromServer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnEach {
                hideLoader()
                changeControlState(view.id, true)
            }
            .doOnError {
                hideLoader()
                changeControlState(view.id, true)
            }
            .subscribe({ userModel: UserModel ->
                sharedStorage.setNeedSelectInterestsAfterLogin(userModel.profile.hashtags.isNullOrEmpty())
                sharedStorage.setCurrentProfile(ProfileMapper.toProfile(userModel))
                _signInResult.postValue(true)
            }) { error: Throwable? ->
                accessTokenRepository.deleteTokenData()
                ParseErrorUtils.parseError(error, errorsFuncs)
            })
    }

    fun resendEmailVerification(email: String) {
        addDisposable("resendEmail", emailDataRepository
            .resendEmail(email)
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

    private fun onError422(errors: List<ErrorResponse>): Boolean {
        for (errorResponse in errors) {
            if (isPopupError(errorResponse.code)) return false
        }
        return true
    }


    private fun isPopupError(code: Int): Boolean {
        return when (code) {
            ErrorCodes.EMAIL_NOT_VERIFIED -> {
                _emailNotVerified.postValue(true)
                true
            }
            ErrorCodes.USER_BLOCKED -> {
                _userBlocked.postValue(true)
                true
            }
            else -> false
        }
    }
}