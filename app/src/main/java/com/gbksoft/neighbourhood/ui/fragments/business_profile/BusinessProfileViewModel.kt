package com.gbksoft.neighbourhood.ui.fragments.business_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.FollowersRepository
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class BusinessProfileViewModel(private val profileRepository: ProfileRepository,
                               private val followersRepository: FollowersRepository) : BaseViewModel() {

    private val _profile = MutableLiveData<BusinessProfile>()
    val profile: LiveData<BusinessProfile> = _profile

    //true if switched to business profile
    private val _profileSwitched = MutableLiveData<Boolean>()
    val profileSwitched: LiveData<Boolean> = _profileSwitched

    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int> = _followersCount

    private val _followedCount = MutableLiveData<Int>()
    val followedCount: LiveData<Int> = _followedCount

    private var businessProfile: BusinessProfile? = null
    private var currentProfile: CurrentProfile? = null

    init {
        loadCurrentProfile(sharedStorage.requireCurrentProfile().id)
        subscribeCurrentProfile()
    }

    private fun loadCurrentProfile(id: Long) {
        Timber.tag("SwitchTag").d("loadCurrentProfile()")
        if (businessProfile == null) onLoadingStart()
        addDisposable("loadCurrentProfile", profileRepository.pullBusinessProfile(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEach { onLoadingFinish() }
                .subscribe({ onProfileLoaded(it) }) { handleError(it) })
    }

    private fun onProfileLoaded(profile: BusinessProfile) {
        businessProfile = profile
        _profile.value = businessProfile
        checkIsProfileSwitched()
    }

    private fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    private fun subscribeCurrentProfile() {
        addDisposable("subscribeCurrentProfile", sharedStorage.subscribeCurrentProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onCurrentProfileLoaded(it) }) { handleError(it) })
    }

    private fun onCurrentProfileLoaded(currentProfile: CurrentProfile) {
        this.currentProfile = currentProfile
        checkIsProfileSwitched()
    }

    private fun checkIsProfileSwitched() {
        val businessProfile = businessProfile ?: return
        val currentProfile = currentProfile ?: return

        if (currentProfile.id != businessProfile.id) {
            _profileSwitched.value = currentProfile.isBusiness
        }
    }

    fun loadFollowersCount() {
        addDisposable("loadFollowersCount", followersRepository.getFollowerProfiles(null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _followersCount.value = it.totalCount
                }, { handleError(it) }))
    }

    fun loadFollowedCount() {
        addDisposable("loadFollowedCount", followersRepository.getFollowedProfiles(null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _followedCount.value = it.totalCount
                }, { handleError(it) }))
    }

    private fun onLoadingStart() {
        showLoader()
    }

    private fun onLoadingFinish() {
        hideLoader()
    }
}