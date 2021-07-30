package com.gbksoft.neighbourhood.ui.fragments.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.FollowersRepository
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel(private val profileRepository: ProfileRepository,
                       private val followersRepository: FollowersRepository) : BaseViewModel() {

    private val _profile = MutableLiveData<BasicProfile>()
    val profile: LiveData<BasicProfile> = _profile

    //true if switched to business profile
    private val _profileSwitched = MutableLiveData<Boolean>()
    val profileSwitched: LiveData<Boolean> = _profileSwitched

    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int> = _followersCount

    private val _followedCount = MutableLiveData<Int>()
    val followedCount: LiveData<Int> = _followedCount

    private var basicProfile: BasicProfile? = null
    private var currentProfile: CurrentProfile? = null

    init {
        loadBasicProfile()
        subscribeCurrentProfile()
    }

    fun loadBasicProfile() {
        if (basicProfile == null) onLoadingStart()
        addDisposable("loadCurrentProfile", profileRepository.subscribeCurrentUserWithRemote()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEach { onLoadingFinish() }
                .map { ProfileMapper.toProfile(it) }
                .subscribe({ onBasicProfileLoaded(it) }) { handleError(it) })
    }

    private fun onBasicProfileLoaded(basicProfile: BasicProfile) {
        this.basicProfile = basicProfile
        _profile.value = basicProfile
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
        val basicProfile = basicProfile ?: return
        val currentProfile = currentProfile ?: return

        if (currentProfile.id != basicProfile.id) {
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