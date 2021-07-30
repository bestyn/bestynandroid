package com.gbksoft.neighbourhood.ui.fragments.search.search_view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.search_engine.ProfilesSearchEngine
import com.gbksoft.neighbourhood.ui.fragments.search.search_engine.SearchEngine
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfilesSearchViewModel(
    private val globalSearchRepository: GlobalSearchRepository
) : BaseViewModel() {

    private val profiles = mutableListOf<ProfileSearchItem>()

    private val _searchResult = MutableLiveData<List<ProfileSearchItem>>()
    val searchResult = _searchResult as LiveData<List<ProfileSearchItem>>
    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>
    private val _progressBarVisibility = MutableLiveData<Boolean>()
    val progressBarVisibility = _progressBarVisibility as LiveData<Boolean>

    private val searchEngine: SearchEngine = ProfilesSearchEngine(globalSearchRepository).apply {
        disposableCallback = ::addDisposable
        successCallback = ::onProfilesSearchSuccess
        failureCallback = ::onSearchFailure
    }

    init {
        subscribeToCurrentProfile()
    }

    private fun subscribeToCurrentProfile() {
        addDisposable("subscribeToCurrentProfile", sharedStorage
            .subscribeCurrentProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _currentProfile.value = it
            }
        )
    }

    fun findProfiles(query: String) {
        _progressBarVisibility.value = true
        searchEngine.search(query)
    }

    fun onVisibleItemChanged(position: Int) {
        searchEngine.onVisibleItemChanged(position)
    }

    fun cancelSearch() {
        _progressBarVisibility.value = false
        searchEngine.cancel()
        profiles.clear()
        _searchResult.value = profiles
    }

    private fun onProfilesSearchSuccess(data: List<ProfileSearchItem>) {
        _progressBarVisibility.value = false
        val currentProfileId = sharedStorage.requireCurrentProfile().id
        data.forEach { it.isMyCurrentProfile = it.id == currentProfileId }
        profiles.clear()
        profiles.addAll(data)
        _searchResult.value = profiles
    }

    private fun onSearchFailure(t: Throwable) {
        _progressBarVisibility.value = false
        handleError(t)
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