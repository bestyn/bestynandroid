package com.gbksoft.neighbourhood.ui.fragments.stories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class StoriesViewModel : BaseViewModel() {
    private var currentProfile: CurrentProfile? = null

    private val _profileSwitched = SingleLiveEvent<Any>()
    val profileSwitched = _profileSwitched as LiveData<Any>

    private val _isAudioEnabled = MutableLiveData<Boolean>().apply { value = true }
    val isAudioEnabled = _isAudioEnabled as LiveData<Boolean>

    private val _navigateToMyProfile = SingleLiveEvent<Unit>()
    val navigateToMyProfile = _navigateToMyProfile as LiveData<Unit>

    private val _navigateToMyBusinessProfile = SingleLiveEvent<Unit>()
    val navigateToMyBusinessProfile = _navigateToMyBusinessProfile as LiveData<Unit>

    private val _navigateToPublicProfile = SingleLiveEvent<Long>()
    val navigateToPublicProfile = _navigateToPublicProfile as LiveData<Long>

    private val _navigateToPublicBusinessProfile = SingleLiveEvent<Long>()
    val navigateToPublicBusinessProfile = _navigateToPublicBusinessProfile as LiveData<Long>

    init {
        subscribeCurrentProfile()
    }

    private fun subscribeCurrentProfile() {
        addDisposable("subscribeCurrentProfile", sharedStorage
            .subscribeCurrentProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }) { handleError(it) })
    }

    private fun onProfileLoaded(profile: CurrentProfile) {
        val currentProfileId = currentProfile?.id
        currentProfile = profile
        if (currentProfileId != null) checkProfileSwitched(currentProfileId, profile.id)
    }


    private fun checkProfileSwitched(currentProfileId: Long, loadedProfileId: Long) {
        if (currentProfileId != loadedProfileId) _profileSwitched.value = Any()
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

    fun toggleAudio() {
        val isEnabled = _isAudioEnabled.value ?: true
        _isAudioEnabled.value = !isEnabled
    }

    fun saveUnAuthorizedStoryId(storyId: Int){
        sharedStorage.setUnAuthorizedStoryId(storyId)
    }

    fun onMentionClicked(profileId: Long) {
        checkIsMyProfile(profileId)
        checkPublicProfile(profileId)
        checkIsPublicBusinessProfile(profileId)
    }

    private fun checkIsMyProfile(profileId: Long) {
        val currentProfile = sharedStorage.getCurrentProfile() ?: return
        if (currentProfile.id != profileId) {
            return
        }
        if (currentProfile.isBusiness) {
            _navigateToMyBusinessProfile.call()
        } else {
            _navigateToMyProfile.call()
        }
    }

    private fun checkPublicProfile(profileId: Long) {
        addDisposable("getPublicProfile", RepositoryProvider.profileRepository.getPublicProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    _navigateToPublicProfile.value = it.id
                })
    }

    private fun checkIsPublicBusinessProfile(profileId: Long) {
        addDisposable("getPublicBusinessProfile", RepositoryProvider.profileRepository.getPublicBusinessProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    _navigateToPublicBusinessProfile.value = it.id
                })
    }
}