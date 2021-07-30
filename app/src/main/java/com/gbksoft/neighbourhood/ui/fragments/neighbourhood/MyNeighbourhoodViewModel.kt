package com.gbksoft.neighbourhood.ui.fragments.neighbourhood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyNeighbourhoodViewModel : BaseViewModel() {
    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        addDisposable("loadCurrentProfile", sharedStorage
            .subscribeCurrentProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }) { handleError(it) })
    }

    private fun onProfileLoaded(currentProfile: CurrentProfile) {
        _currentProfile.value = currentProfile
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
}