package com.gbksoft.neighbourhood.ui.fragments.profile.tabs

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider.profileRepository
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class InterestsViewModel(context: Context?) : BaseViewModel() {
    private val interestsLiveData = MutableLiveData<List<Hashtag>>()
    val interest: LiveData<List<Hashtag>>
        get() = interestsLiveData

    private fun loadProfile() {
        val profileRepository = profileRepository
        addDisposable("getCurrentUser", profileRepository.subscribeCurrentUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userModel: UserModel -> onProfileLoaded(userModel) }) { throwable: Throwable -> onProfileError(throwable) })
    }

    private fun onProfileLoaded(userModel: UserModel) {
        interestsLiveData.value = ProfileMapper.toProfile(userModel).hashtags
    }

    private fun onProfileError(throwable: Throwable) {
        throwable.printStackTrace()
    }

    init {
        loadProfile()
    }
}