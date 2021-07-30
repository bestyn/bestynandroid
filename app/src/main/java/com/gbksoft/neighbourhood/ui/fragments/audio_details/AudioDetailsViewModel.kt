package com.gbksoft.neighbourhood.ui.fragments.audio_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.AudioRepository
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AudioDetailsViewModel(
        private val audioId: Long,
        private val audioRepository: AudioRepository,
        private val postDataRepository: PostDataRepository) : BaseViewModel() {

    private var paging: Paging<List<FeedPost>>? = null
    private var isLoading = false
    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER

    private val storyList = mutableListOf<FeedPost>()

    private val storiesLiveData = MutableLiveData<List<FeedPost>>()
    val stories = storiesLiveData as LiveData<List<FeedPost>>

    private val _progressBarVisibility = MutableLiveData<Boolean>()
    val progressBarVisibility = _progressBarVisibility as LiveData<Boolean>

    private val isAudioFavoriteLiveData = MutableLiveData<Boolean>()
    val isAudioFavorite = isAudioFavoriteLiveData as LiveData<Boolean>

    init {
        loadStories()
    }

    private fun loadStories() {
        addDisposable("loadStories", postDataRepository
                .loadPostsWithAudio(paging = paging, audioId = audioId, postTypes = listOf(PostType.STORY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { isLoading = false }
                .subscribe({
                    paging = it
                    onStoriesLoaded(it.content)
                }, { handleError(it) }))
    }

    private fun onStoriesLoaded(stories: List<FeedPost>) {
        storyList.addAll(stories)
        storiesLiveData.value = storyList
    }

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= storyList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > storyList.count()
        if (hasMorePages) {
            loadStories()
        }
    }

    fun handleStarButtonClick(audio: Audio) {
        if (audio.isFavorite) {
            removeAudioFromFavorites(audio.id)
        } else {
            addAudioToFavorites(audio.id)
        }
    }

    private fun addAudioToFavorites(id: Long) {
        addDisposable("addAudioToFavorites", audioRepository.addAudioToFavorites(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleAudioAddedToFavorites() }, { handleError(it) }))
    }

    private fun handleAudioAddedToFavorites() {
        isAudioFavoriteLiveData.value = true
    }

    private fun removeAudioFromFavorites(id: Long) {
        addDisposable("addAudioToFavorites", audioRepository.removeAudioFromFavorites(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleAudioRemovedFromFavorites() }, { handleError(it) }))
    }

    private fun handleAudioRemovedFromFavorites() {
        isAudioFavoriteLiveData.value = false
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