package com.gbksoft.neighbourhood.ui.fragments.stories.audio.list

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.AudioRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AudioListViewModel(private val context: Context,
                         private val audioRepository: AudioRepository) : BaseViewModel() {

    private val audioListLiveData = MutableLiveData<List<Audio>>()
    val audioList: LiveData<List<Audio>> = audioListLiveData

    private val currentAudioList = mutableListOf<Audio>()

    private var paging: Paging<List<Audio>>? = null
    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    private var isLoading = false

    private var currentTab: AudioTab = AudioTab.DISCOVER
    private var currentSearch: String? = null

    init {
        loadAudioList()
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
            AudioTab.DISCOVER -> audioRepository.loadDiscoverAudio(currentSearch, paging)
            AudioTab.MY_TRACKS -> audioRepository.loadMyAudio(sharedStorage.requireCurrentProfile().id, currentSearch, paging)
            AudioTab.FAVORITES -> audioRepository.loadFavoriteAudio(currentSearch, paging)
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

    fun setCurrentTab(audioTab: AudioTab) {
        currentTab = audioTab
        currentAudioList.clear()
        loadAudioList()
    }

    fun setCurrentSearch(search: String?) {
        currentSearch = search
        currentAudioList.clear()
        loadAudioList()
    }

    fun handleStarButtonClick(audio: Audio) {
        if (audio.isFavorite) {
            removeAudioFromFavorites(audio.id)
        } else {
            addAudioToFavorites(audio.id)
        }
    }

    fun validateAddedAudio(audioUri: Uri): Boolean {
        validationUtils.validateFileTooBig(context.contentResolver, audioUri, Constants.AUDIO_FILE_MAX_LENGTH)?.let {
            ToastUtils.showToastMessage(it)
        }

        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(context, audioUri)
        }
        validationUtils.validateDurationTooLong(ValidationField.AUDIO_DURATION, mediaMetadataRetriever, Constants.AUDIO_MAX_DURATION)?.let {
            ToastUtils.showToastMessage(it)
            mediaMetadataRetriever.release()
            return false
        }
        mediaMetadataRetriever.release()
        return true
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