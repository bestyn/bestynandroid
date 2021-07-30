package com.gbksoft.neighbourhood.ui.fragments.profile.hashtags

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.models.request.user.UpdateProfileReq
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EditInterestsViewModel(val context: Context) : BaseViewModel() {

    private val currentInterestsLiveData = MutableLiveData<List<Hashtag>>()
    fun getCurrentInterests(): LiveData<List<Hashtag>> = currentInterestsLiveData

    private val interestsSavedLiveData = MutableLiveData<Boolean>()
    fun interestsSaved(): LiveData<Boolean> = interestsSavedLiveData

    private val allInterestsLiveData = MutableLiveData<List<Hashtag>>()
    fun getAllInterests(): LiveData<List<Hashtag>> = allInterestsLiveData

    private val interestsDelegate = InterestsDelegate()

    init {
        loadCurrentInterests()
    }

    private fun loadCurrentInterests() {
        val profileRepository = RepositoryProvider.profileRepository
        addDisposable("loadCurrentInterests", profileRepository.getCurrentUserFromServer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onCurrentInterestsLoaded(it) }) { onCurrentInterestsError(it) })
    }

    private fun onCurrentInterestsLoaded(userModel: UserModel) {
        val interests = ProfileMapper.toProfile(userModel).hashtags
        interestsDelegate.setCurrentInterests(interests)
        currentInterestsLiveData.value = interestsDelegate.getFinalList()
        searchInterests(null)
    }

    private fun onCurrentInterestsError(throwable: Throwable) {
        handleError(throwable)
    }


    fun searchInterests(query: String?) {
        addDisposable("getMyInterests", RepositoryProvider.hashtagsRepository
            .loadHashtags(null, query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onInterestsFound(it.content)
            }, { ParseErrorUtils.parseError(it, errorsFuncs) }))
    }

    private fun onInterestsFound(interests: List<Hashtag>) {
        interestsDelegate.setAllInterests(interests)
        allInterestsLiveData.value = interestsDelegate.getAllInterests()
    }

    fun addInterest(interest: Hashtag) {
        val added = interestsDelegate.addInterest(interest)
        if (added) {
            currentInterestsLiveData.value = interestsDelegate.getFinalList()
            allInterestsLiveData.value = interestsDelegate.getAllInterests()
        }
    }

    fun removeInterest(interest: Hashtag) {
        val removed = interestsDelegate.removeInterest(interest)
        if (removed) {
            currentInterestsLiveData.value = interestsDelegate.getFinalList()
            allInterestsLiveData.value = interestsDelegate.getAllInterests()
        }
    }

    fun saveInterests() {
        val interests = interestsDelegate.getFinalList()
        if (interests.isNotEmpty() && interestsDelegate.hasNotChanged()) {
            interestsSavedLiveData.value = true
            return
        }

        if (validateInterests(interests)) {
            val req = UpdateProfileReq()
            val hashtagIds = interests.map { it.id }
            req.setHashtags(hashtagIds)
            addDisposable("saveInterests", RepositoryProvider.profileRepository
                .updateProfile(req)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    interestsSavedLiveData.value = true
                    updateCurrentProfile(it)
                }, { handleError(it) }))
        }
    }

    private fun updateCurrentProfile(currentProfile: CurrentProfile) {
        sharedStorage.setCurrentProfile(currentProfile)
    }

    private fun validateInterests(interests: List<Hashtag>): Boolean {
        val errorFieldsModel = ErrorFieldsModel()
        errorFieldsModel.addError(ValidationField.MY_INTERESTS,
            validationUtils.validateFieldOnRequired(ValidationField.MY_INTERESTS, interests))
        handleValidationError(errorFieldsModel)
        return errorFieldsModel.isValid
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    private fun handleValidationError(errorFieldsModel: ErrorFieldsModel) {
        for (error in errorFieldsModel.errorsMap.values) {
            ToastUtils.showToastMessage(error)
        }
    }

    fun hasChanges(): Boolean = interestsDelegate.hasChanged()
}