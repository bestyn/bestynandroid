package com.gbksoft.neighbourhood.ui.fragments.profile.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.models.request.user.UpdateProfileReq
import com.gbksoft.neighbourhood.data.models.response.base.ErrorResponse
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper.toProfile
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.profile.component.FieldsValidationDelegate
import com.gbksoft.neighbourhood.ui.fragments.profile.component.UpdateProfileForm
import com.gbksoft.neighbourhood.ui.fragments.profile.model.ProfileSettingsModel
import com.gbksoft.neighbourhood.utils.AddressFormatter.format
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.BitmapResizeResult
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class ProfileSettingsViewModel(private val context: Context) : BaseViewModel() {
    private val profileRepository: ProfileRepository = RepositoryProvider.profileRepository
    private val validationDelegate: FieldsValidationDelegate = FieldsValidationDelegate(validationUtils)
    private val updateProfileForm = UpdateProfileForm(validationDelegate)

    private var profileModel: ProfileSettingsModel? = null

    private val _profileSettingsModel = MutableLiveData<ProfileSettingsModel>()
    val profileSettingsModel: LiveData<ProfileSettingsModel> = _profileSettingsModel

    private val _profileDataSaved = MutableLiveData<Boolean>()
    val profileDataSaved: LiveData<Boolean> = _profileDataSaved

    private val _businessContentShown = SingleLiveEvent<Boolean>()
    val businessContentShown: LiveData<Boolean> = _businessContentShown

    private val _errorFields = MutableLiveData<ErrorFieldsModel>()
    val errorFields: LiveData<ErrorFieldsModel> = _errorFields

    private val _passwordChanged = SingleLiveEvent<Boolean>()
    val passwordChanged: LiveData<Boolean> = _passwordChanged

    private val _confirmEmail = SingleLiveEvent<Boolean>()
    val confirmEmail: LiveData<Boolean> = _confirmEmail


    var dateOfBirthTime: Long = 0
        private set

    init {
        errorHandler.on422Callback = { onError422(it) }
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        addDisposable("getCurrentUser", profileRepository.subscribeCurrentUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userModel: UserModel -> onProfileLoaded(userModel) }) { t: Throwable -> onProfileError(t) })
    }

    private fun onProfileLoaded(userModel: UserModel) {
        val profile = toProfile(userModel)
        extractDateOfBirthTime(profile)
        updateProfileForm.setProfile(profile)
        profileModel?.setProfile(profile)
            ?: run {
                profileModel = ProfileSettingsModel(profile)
                _profileSettingsModel.value = profileModel
            }
    }

    private fun extractDateOfBirthTime(profile: BasicProfile) {
        dateOfBirthTime = profile.birthday?.timeInMillis ?: System.currentTimeMillis()
    }

    private fun onProfileError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    fun handleCameraResponse(resultCode: Int, photo: File?) {
        if (resultCode != Activity.RESULT_OK) return
        if (photo == null) return
        val pictureDecodeDisposable = MediaUtils.decodeFromFile(context, photo)
            .map { bitmap -> MediaUtils.adjustBitmapSize(bitmap) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { removeTempCameraFile(photo) }
            .subscribe({ res: BitmapResizeResult ->
                updateProfileForm.setAvatar(res.file)
                profileModel?.avatar?.set(res.file)
            }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    private fun removeTempCameraFile(tempFile: File) {
        tempFile.delete()
    }

    fun handleGalleryResponse(resultCode: Int, uri: Uri?) {
        if (resultCode != Activity.RESULT_OK) return
        if (uri == null) return
        val pictureDecodeDisposable = MediaUtils.decodeFromUri(context, uri)
            .map { bitmap -> MediaUtils.adjustBitmapSize(bitmap) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res: BitmapResizeResult ->
                updateProfileForm.setAvatar(res.file)
                profileModel?.avatar?.set(res.file)
            }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    fun handleAddressResponse(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val components = place.addressComponents
                val addressStr = format(components)
                updateProfileForm.setAddressPlace(place.id, components)
                profileModel?.address?.set(addressStr)
                Timber.d("Place: " + place.name + ", " + place.id)
            }
            AutocompleteActivity.RESULT_ERROR -> {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
                Timber.i(status.statusMessage)
            }
            Activity.RESULT_CANCELED -> {
                // The user canceled the operation.
                Timber.d("set place canceled")
            }
        }
    }

    fun removeAvatar() {
        updateProfileForm.removeAvatar()
        profileModel?.avatarUrl?.set(null)
        profileModel?.avatar?.set(null)
    }

    fun setFullName(fullName: String) {
        updateProfileForm.setFullName(fullName)
    }

    fun setGenderType(genderType: Int?) {
        updateProfileForm.setGenderType(genderType)
    }

    fun setDateOfBirth(timeInMillis: Long) {
        dateOfBirthTime = timeInMillis
        val date = DateTimeUtils.formatProfileDateOfBirth(timeInMillis)
        profileModel?.dateOfBirth?.set(date)
        updateProfileForm.setDateOfBirth(date, timeInMillis)
    }

    fun saveProfileData() {
        if (!updateProfileForm.areThereChanges()) {
            _profileDataSaved.value = true
            return
        }
        val errorFieldsModel = updateProfileForm.validateData()
        if (!errorFieldsModel.isValid) {
            _errorFields.postValue(errorFieldsModel)
            return
        }
        onLoadingStart()
        val req = updateProfileForm.buildUpdateProfileReq()
        addDisposable("updateProfile", profileRepository.updateProfile(req)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ currentProfile: CurrentProfile ->
                updateCurrentProfile(currentProfile)
                _profileDataSaved.setValue(true)
            }) { handleError(it) })
    }

    fun switchBusinessContentShown(isShown: Boolean) {
        onLoadingStart()
        val req = UpdateProfileReq()
        req.setSeeBusinessPosts(if (isShown) 1 else 0)
        addDisposable("switchBusinessContentShown", profileRepository
            .updateProfile(req)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ currentProfile: CurrentProfile ->
                updateCurrentProfile(currentProfile)
                _businessContentShown.setValue(isShown)
            }) { t: Throwable ->
                _businessContentShown.value = !isShown
                handleError(t)
            })
    }

    private fun updateCurrentProfile(currentProfile: CurrentProfile) {
        sharedStorage.setCurrentProfile(currentProfile)
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validateChangePassword(errorFieldsModel,
            currentPassword, newPassword, confirmPassword)
        if (!errorFieldsModel.isValid) {
            _errorFields.postValue(errorFieldsModel)
            return
        }
        onLoadingStart()
        addDisposable("changePassword", profileRepository
            .changePassword(currentPassword, newPassword)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ _passwordChanged.setValue(true) }) { handleError(it) })
    }

    fun changeEmail(newEmail: String) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validateChangeEmail(errorFieldsModel, newEmail)
        if (!errorFieldsModel.isValid) {
            _errorFields.postValue(errorFieldsModel)
            return
        }
        onLoadingStart()
        addDisposable("changeEmail", profileRepository
            .changeEmail(newEmail)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onChangeEmailTokenSent(newEmail) }) { handleError(it) })
    }

    private fun onChangeEmailTokenSent(email: String?) {
        sharedStorage.saveNewEmailForConfirm(email)
        _confirmEmail.value = true
    }

    fun resendChangeEmailToken(newEmail: String) {
        val sentMessage = context.getString(R.string.dialog_confirm_your_email_message)
        onLoadingStart()
        addDisposable("changeEmail", profileRepository
            .changeEmail(newEmail)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ ToastUtils.showToastMessage(sentMessage) }) { handleError(it) })
    }

    private fun onLoadingStart() {
        showLoader()
        changeControlState(R.id.btnSaveProfile, false)
        changeControlState(R.id.btnChangePhoto, false)
        changeControlState(R.id.btnSaveEmail, false)
        changeControlState(R.id.btnSavePassword, false)
        changeControlState(R.id.switchBusinessContent, false)
    }

    private fun onLoadingFinish() {
        hideLoader()
        changeControlState(R.id.btnSaveProfile, true)
        changeControlState(R.id.btnChangePhoto, true)
        changeControlState(R.id.btnSaveEmail, true)
        changeControlState(R.id.btnSavePassword, true)
        changeControlState(R.id.switchBusinessContent, true)
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        Timber.tag("ErrTag").d("handleError: " + t.message)
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    private fun onError422(errors: List<ErrorResponse>): Boolean {
        Timber.tag("ErrTag").d("onError422")
        val errorFieldsModel = ErrorFieldsModel()
        for (error in errors) {
            val field = fetchField(error.field)
            errorFieldsModel.addError(field, error.message)
        }
        _errorFields.postValue(errorFieldsModel)
        return false
    }

    private fun fetchField(field: String?): ValidationField? {
        when (field) {
            "password" -> return ValidationField.CURRENT_PASSWORD
            "newPassword" -> return ValidationField.NEW_PASSWORD
            "confirmPassword" -> return ValidationField.CONFIRM_NEW_PASSWORD
            "newEmail" -> return ValidationField.NEW_EMAIL
            "placeId" -> return ValidationField.ADDRESS
            "birthday" -> return ValidationField.DATE_OF_BIRTH
        }
        return null
    }
}