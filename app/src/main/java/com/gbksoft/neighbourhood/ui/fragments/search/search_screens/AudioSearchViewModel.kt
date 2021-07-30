package com.gbksoft.neighbourhood.ui.fragments.search.search_screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.AudioRepository
import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.search_engine.AudioSearchEngine
import com.gbksoft.neighbourhood.ui.fragments.search.search_engine.SearchEngine
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.list.AudioTab
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AudioSearchViewModel(private val audioRepository: AudioRepository, private val globalSearchRepository: GlobalSearchRepository) : BaseViewModel() {

    private val audioListLiveData = MutableLiveData<List<Audio>>()

    private val currentAudioList = mutableListOf<Audio>()

    private var paging: Paging<List<Audio>>? = null
    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    private var isLoading = false

    private var currentTab: AudioTab = AudioTab.DISCOVER
    private var currentSearch: String? = null

    private val profiles = mutableListOf<Audio>()

    private val _searchResult = MutableLiveData<List<Audio>>()
    val searchResult = _searchResult as LiveData<List<Audio>>
    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>
    private val _progressBarVisibility = MutableLiveData<Boolean>()
    val progressBarVisibility = _progressBarVisibility as LiveData<Boolean>

    private val searchEngine: SearchEngine = AudioSearchEngine(globalSearchRepository).apply {
        disposableCallback = ::addDisposable
        successCallback = ::onProfilesSearchSuccess
        failureCallback = ::onSearchFailure
    }

    init {
        loadAudioList()
    }

    fun findProfiles(query: String) {
        _progressBarVisibility.value = true
        searchEngine.search(query)
    }


    fun cancelSearch() {
        _progressBarVisibility.value = false
        searchEngine.cancel()
        profiles.clear()
        _searchResult.value = profiles
    }

    private fun loadAudioList() {
        addDisposable("loadAudioList", getLoadAudioEntryPoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { isLoading = false }
                .subscribe({ handleAudioList(it) }, { handleError(it) }))
    }

    private fun getLoadAudioEntryPoint(): Observable<Paging<List<Audio>>> {
        return when (currentTab) {
            //AudioTab.DISCOVER -> audioRepository.loadDiscoverAudio(sharedStorage.requireCurrentProfile().id.toString(), currentSearch, paging)
            AudioTab.MY_TRACKS -> audioRepository.loadMyAudio(sharedStorage.requireCurrentProfile().id, currentSearch, paging)
            //AudioTab.FAVORITES -> audioRepository.loadFavoriteAudio(sharedStorage.requireCurrentProfile().id, currentSearch, paging)
            else -> audioRepository.loadMyAudio(sharedStorage.requireCurrentProfile().id, currentSearch, paging)
        }
    }

    private fun handleAudioList(audioListPaging: Paging<List<Audio>>) {
        paging = audioListPaging
        currentAudioList.addAll(audioListPaging.content)
        audioListLiveData.value = currentAudioList
    }

    private fun addAudioToFavorites(id: Long) {
        addDisposable("addAudioToFavorites", audioRepository.addAudioToFavorites(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleAudioAddedToFavorites(id) }, { handleError(it) }))
    }

    private fun handleAudioAddedToFavorites(id: Long) {
        currentAudioList.find { it.id == id }?.isFavorite = true
        audioListLiveData.value = currentAudioList
    }

    private fun removeAudioFromFavorites(id: Long) {
        addDisposable("addAudioToFavorites", audioRepository.removeAudioFromFavorites(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleAudioRemovedFromFavorites(id) }, { handleError(it) }))
    }

    private fun handleAudioRemovedFromFavorites(id: Long) {
        val audio = currentAudioList.find { it.id == id } ?: return
        if (currentTab == AudioTab.FAVORITES) {
            currentAudioList.remove(audio)
        } else {
            audio.isFavorite = false
        }
        audioListLiveData.value = currentAudioList
    }

    private fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    fun handleStarButtonClick(audio: Audio) {
        if (audio.isFavorite) {
            removeAudioFromFavorites(audio.id)
        } else {
            addAudioToFavorites(audio.id)
        }
    }

    private fun onProfilesSearchSuccess(data: List<Audio>) {
        _progressBarVisibility.value = false
        val currentProfileId = sharedStorage.requireCurrentProfile().id
        profiles.clear()
        profiles.addAll(data)
        _searchResult.value = profiles
    }
    private fun onSearchFailure(t: Throwable) {
        _progressBarVisibility.value = false
        handleError(t)
    }


    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= currentAudioList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > currentAudioList.count()
        if (hasMorePages) {
            loadAudioList()
        }
    }

}