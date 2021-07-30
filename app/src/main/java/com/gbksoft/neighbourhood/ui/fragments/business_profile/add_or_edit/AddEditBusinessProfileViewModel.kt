package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.models.response.base.ErrorResponse
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.repositories.payments.PaymentRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.mappers.payment.SubscriptionPlanMapper
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.payment.SkuType
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class AddEditBusinessProfileViewModel(
    private val paymentRepository: PaymentRepository,
    val context: Context
) : BaseViewModel() {

    companion object {
        private const val CREATION_ID = -1L
    }

    private val subscriptionPlanMapper = SubscriptionPlanMapper(context)

    private val profileRepository: ProfileRepository = RepositoryProvider.profileRepository
    private val validationDelegate = FieldsValidationDelegate(context, validationUtils)

    private val titleLiveData = MutableLiveData<String>()
    fun title(): LiveData<String> = titleLiveData

    private val profileModelLiveData = MutableLiveData<AddEditBusinessProfileModel>()
    fun getProfileModel(): LiveData<AddEditBusinessProfileModel> = profileModelLiveData

    private val profileDataSavedLiveData = MutableLiveData<Boolean>()
    fun profileDataSaved(): LiveData<Boolean> = profileDataSavedLiveData

    private val errorFieldsLiveData = MutableLiveData<ErrorFieldsModel>()
    fun errorFields(): LiveData<ErrorFieldsModel> = errorFieldsLiveData

    private val foundInterestsLiveData = MutableLiveData<List<Hashtag>>()
    fun getFoundInterests(): LiveData<List<Hashtag>> = foundInterestsLiveData

    private val increaseButtonTextLiveData = MutableLiveData<String>()
    fun getIncreaseButtonTextLiveData(): LiveData<String> = increaseButtonTextLiveData

    private var profileModel = AddEditBusinessProfileModel()
    private val creationFormBuilder by lazy {
        CreationFormBuilder(profileModel, validationDelegate)
    }
    private val updatingFormBuilder by lazy {
        UpdatingFormBuilder(profileModel, validationDelegate)
    }

    private var profileId: Long? = null

    init {
        profileModelLiveData.value = profileModel
    }

    fun setProfileId(id: Long) {
        if (profileId != null) return
        this.profileId = id

        if (profileId == CREATION_ID) {
            titleLiveData.value = context.getString(R.string.title_add_business_profile)
            increaseButtonTextLiveData.value = context.getString(R.string.edit_business_profile_increase_radius)
            profileModel.setNullProfile()
        } else {
            titleLiveData.value = context.getString(R.string.title_edit_business_profile)
            increaseButtonTextLiveData.value = context.getString(R.string.edit_business_profile_change_radius)
            loadCurrentProfile(id)
        }
        loadActiveSubscription()
    }

    private fun loadActiveSubscription() {
        addDisposable("fetchPurchase", paymentRepository.getSavedSubscription()
            .flatMap { savedSubscription ->
                Maybe.create<SkuType> { emitter ->
                    val skuType = SkuType.getSkuTypeByProductName(savedSubscription.productName)
                    skuType?.let { emitter.onSuccess(it) } ?: emitter.onComplete()
                }
                    .flatMap {
                        paymentRepository.getSkuDetails(it.sku)
                            .map { subscriptionPlanMapper.map(it) }
                    }
                    .defaultIfEmpty(subscriptionPlanMapper.buildSubscriptionPlanFromAnotherPlatform(savedSubscription))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                profileModel.setActiveSubscriptionPlan(it)
                increaseButtonTextLiveData.value = context.getString(R.string.edit_business_profile_change_radius)
                if (profileId == CREATION_ID) {
                    profileModel.radius.set(AddEditBusinessProfileModel.BOUGHT_RADIUS)
                }
            }) { handleError(it) })
    }

    private fun loadCurrentProfile(profileId: Long) {
        addDisposable("getCurrentUser", profileRepository.getMyBusinessProfile(profileId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }) { handleError(it) })
    }

    private fun onProfileLoaded(profile: BusinessProfile) {
        updatingFormBuilder.setProfile(profile)
        profileModel.setProfile(profile)
    }

    fun handleCameraResponse(resultCode: Int, photo: File?) {
        if (resultCode != Activity.RESULT_OK) return
        if (photo == null) return

        val pictureDecodeDisposable = MediaUtils.decodeFromFile(context, photo)
            .map { MediaUtils.adjustBitmapSize(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { removeTempCameraFile(photo) }
            .subscribe({ profileModel.setAvatar(it.file) }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    private fun removeTempCameraFile(tempFile: File?) {
        tempFile!!.delete()
    }

    fun handleGalleryResponse(resultCode: Int, uri: Uri?) {
        if (resultCode != Activity.RESULT_OK) return
        if (uri == null) return

        val pictureDecodeDisposable = MediaUtils.decodeFromUri(context, uri)
            .map { MediaUtils.adjustBitmapSize(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profileModel.setAvatar(it.file) }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    fun handleAddressResponse(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                profileModel.setAddress(place)
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
        profileModel.removeAvatar()
    }

    fun setRadius(radius: Int) {
        profileModel.radius.set(radius)
    }

    fun searchInterest(query: String?) {
        addDisposable("getMyInterests", RepositoryProvider.hashtagsRepository
            .loadHashtags(null, query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                foundInterestsLiveData.value = it.content
            }, {
                ParseErrorUtils.parseError(it, errorsFuncs)
            }))
    }

    fun addInterest(interest: Hashtag): Boolean {
        if (profileModel.hashtagCount() >= Constants.BUSINESS_CATEGORY_MAX_COUNT) {
            ToastUtils.showToastMessage(context.getString(R.string.msg_category_limit_exceeded))
            return false
        }
        val isAdded = profileModel.addHashtag(interest)
        if (!isAdded) {
            ToastUtils.showToastMessage(context.getString(R.string.you_already_added_such_type))
        }
        return isAdded
    }

    fun removeInterest(interest: Hashtag) {
        profileModel.removeHashtag(interest)
    }

    fun saveProfile() {
        if (profileId == CREATION_ID) createProfile()
        else updateProfile()
    }

    private fun createProfile() {
        val errorFieldsModel: ErrorFieldsModel = creationFormBuilder.validateData()
        if (!errorFieldsModel.isValid) {
            errorFieldsLiveData.postValue(errorFieldsModel)
            return
        }

        onLoadingStart()

        addDisposable("createProfile", profileRepository
            .createBusinessProfile(creationFormBuilder.build())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ onCreateSuccess(it) }, { handleError(it) }))
    }

    private fun onCreateSuccess(profile: BusinessProfile) {
        Analytics.onCreatedBusinessProfile()
        updateCurrentProfile(profile)
        ToastUtils.showToastMessage(context.getString(R.string.msg_business_profile_created))
        profileDataSavedLiveData.value = true
    }

    private fun updateProfile() {
        val errorFieldsModel: ErrorFieldsModel = updatingFormBuilder.validateData()
        if (!errorFieldsModel.isValid) {
            errorFieldsLiveData.postValue(errorFieldsModel)
            return
        }

        onLoadingStart()

        val form = updatingFormBuilder.build()
        if (form == null) {
            onLoadingFinish()
            profileDataSavedLiveData.value = false
            return
        }

        addDisposable("updateProfile", profileRepository
            .updateBusinessProfile(form)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ onUpdateSuccess(it) }, { handleError(it) }))
    }

    private fun onUpdateSuccess(profile: BusinessProfile) {
        updateCurrentProfile(profile)
        ToastUtils.showToastMessage(context.getString(R.string.msg_business_profile_updated))
        profileDataSavedLiveData.value = true
    }

    private fun updateCurrentProfile(profile: BusinessProfile) {
        val currentProfile: CurrentProfile = ProfileMapper.toCurrentProfile(profile)
        sharedStorage.setCurrentProfile(currentProfile)
    }

    private fun onLoadingStart() {
        Timber.tag("SwitchTag").d("onLoadingStart()")
        showLoader()
        changeControlState(R.id.btnChangeAvatar, false)
        changeControlState(R.id.avatarView, false)
        changeControlState(R.id.addAvatar, false)
        changeControlState(R.id.btnCreate, false)
        changeControlState(R.id.etAddress, false)
        changeControlState(R.id.etCategories, false)
        changeControlState(R.id.rbOnlyMe, false)
        changeControlState(R.id.rbRadius10, false)
        changeControlState(R.id.rbIncreaseRadius, false)
    }

    private fun onLoadingFinish() {
        Timber.tag("SwitchTag").d("onLoadingFinish()")
        hideLoader()
        changeControlState(R.id.btnChangeAvatar, true)
        changeControlState(R.id.avatarView, true)
        changeControlState(R.id.addAvatar, true)
        changeControlState(R.id.btnCreate, true)
        changeControlState(R.id.etAddress, true)
        changeControlState(R.id.etCategories, true)
        changeControlState(R.id.rbOnlyMe, true)
        changeControlState(R.id.rbRadius10, true)
        changeControlState(R.id.rbIncreaseRadius, true)
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
        errorFieldsLiveData.postValue(errorFieldsModel)
        return false
    }

    private fun fetchField(field: String?): ValidationField? {
        when (field) {
            "placeId" -> return ValidationField.ADDRESS
        }
        return null
    }
}