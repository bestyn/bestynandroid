package com.gbksoft.neighbourhood.ui.activities.splash

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.models.response.ConfigModel
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.AnyBodyDataRepository
import com.gbksoft.neighbourhood.data.repositories.EmailDataRepository
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils.parseError
import com.gbksoft.neighbourhood.domain.DeviceIdProvider
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper.toProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.Route
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class SplashActivityViewModel(
    private val deviceIdProvider: DeviceIdProvider,
    private val anyBodyDataRepository: AnyBodyDataRepository,
    private val emailDataRepository: EmailDataRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository

) : BaseViewModel() {
    private val _configUpdates = SingleLiveEvent<Boolean>()
    val configUpdates: LiveData<Boolean> = _configUpdates

    private val _confirmEmail = SingleLiveEvent<Boolean>()
    val confirmEmail: LiveData<Boolean> = _confirmEmail

    private val _confirmChangedEmail = SingleLiveEvent<Boolean>()
    val confirmChangedEmail: LiveData<Boolean> = _confirmChangedEmail

    private val _resetPasswordTokenValidation = SingleLiveEvent<String?>()
    val resetPasswordTokenValidation: LiveData<String?> = _resetPasswordTokenValidation

    private val _login = SingleLiveEvent<LoginData>()
    val login: LiveData<LoginData> = _login

    init {
        loadConfigs()
    }

    fun loadConfigs() {
        addDisposable("getConfig", anyBodyDataRepository
            .getConfig()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp: ConfigModel ->
                sharedStorage.saveApiVersion(resp.version)
                sharedStorage.saveParametersConfig(resp.parameters)
                sharedStorage.saveErrorsConfig(resp.errors)
                _configUpdates.postValue(true)
            }, { error: Throwable? ->
                parseError(error, errorsFuncs)
                _configUpdates.postValue(false)
            }))
    }

    fun checkLoginAndRoute(route: Route?, args: Bundle?) {
        val loginData = LoginData(sharedStorage.isTokenAlive())
        loginData.route = route
        loginData.args = args
        _login.postValue(loginData)
    }

    fun confirmEmail(confirmEmailToken: String) = viewModelScope.launch {
        signOutApp()
        val deviceId = deviceIdProvider.getDeviceId()

        addDisposable("confirmEmail", emailDataRepository
            .confirmEmail(confirmEmailToken, deviceId)
            .doOnSubscribe { showLoader() }
            .doOnError { hideLoader() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp: TokenModel ->
                accessTokenRepository.saveTokenData(resp)
                Analytics.getInstance().onConfirmedEmail()
                loadCurrentUserAfterConfirmEmail()
            }, { error: Throwable? ->
                parseError(error, errorsFuncs)
                _confirmEmail.postValue(false)
            }))
    }

    private fun loadCurrentUserAfterConfirmEmail() {
        addDisposable("loadCurrentProfile", profileRepository
            .getCurrentUserFromServer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnEach { hideLoader() }
            .doOnError { hideLoader() }
            .subscribe({ userModel: UserModel ->
                sharedStorage.setNeedSelectInterestsAfterLogin(userModel.profile.hashtags.isNullOrEmpty())
                sharedStorage.setCurrentProfile(toProfile(userModel))
                _confirmEmail.postValue(true)
            }) { error: Throwable? ->
                accessTokenRepository.deleteTokenData()
                parseError(error, errorsFuncs)
            })
    }

    fun confirmChangedEmail(confirmEmailToken: String) = viewModelScope.launch {
        val deviceId = deviceIdProvider.getDeviceId()

        addDisposable("confirmChangedEmail", emailDataRepository
            .confirmChangedEmail(confirmEmailToken, deviceId)
            .doOnSubscribe { showLoader() }
            .doOnTerminate { hideLoader() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                signOutApp()
                _confirmChangedEmail.postValue(true)
            }, { error: Throwable? ->
                parseError(error, errorsFuncs)
                _confirmChangedEmail.postValue(false)
            }))
    }

    fun validateResetPasswordToken(resetPasswordToken: String) {
        addDisposable("validateResetPasswordToken", userRepository
            .validateResetPasswordToken(resetPasswordToken)
            .doOnSubscribe { showLoader() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                hideLoader()
                signOutApp()
                _resetPasswordTokenValidation.postValue(resetPasswordToken)
            }, { error: Throwable? ->
                hideLoader()
                parseError(error, errorsFuncs)
                _resetPasswordTokenValidation.postValue(null)
            }))
    }
}