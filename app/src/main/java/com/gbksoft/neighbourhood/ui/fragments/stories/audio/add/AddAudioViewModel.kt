package com.gbksoft.neighbourhood.ui.fragments.stories.audio.add

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.AudioRepository
import com.gbksoft.neighbourhood.data.repositories.HashtagRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class AddAudioViewModel(private val context: Context,
                        private val audio: Uri,
                        private val audioRepository: AudioRepository,
                        private val hashtagRepository: HashtagRepository) : BaseViewModel() {

    private val audioValidationDelegate = AudioValidationDelegate(validationUtils, context)
    private val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(context, audio) }

    private val _foundHashtags = SingleLiveEvent<List<Hashtag>>()
    val foundHashtags = _foundHashtags as LiveData<List<Hashtag>>

    private val _errorFields = MutableLiveData<ErrorFieldsModel>()
    val errorFields = _errorFields as LiveData<ErrorFieldsModel>

    private val _duration = MutableLiveData<Int>()
    val duration = _duration as LiveData<Int>
    private var durationMills = -1

    val audioLoadedLiveEvent = SingleLiveEvent<Unit>()

    private var searchHashtagsDisposable: Disposable? = null

    init {
        _duration.value = getAudioDuration()
    }

    fun createAudio(description: String?, startTime: Int) {
        val errorFieldsModel = ErrorFieldsModel()
        audioValidationDelegate.validateDescription(errorFieldsModel, description)
        if (!errorFieldsModel.isValid) {
            _errorFields.postValue(errorFieldsModel)
            return
        }

        onLoadingStart()

        val duration = (getAudioDuration() - startTime) / 1000
        audioRepository.createAudio(audio, description!!, startTime / 1000, duration)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { onLoadingFinish() }
                .doOnDispose { onLoadingFinish() }
                .subscribe({ audioLoadedLiveEvent.call() }, { handleError(it) })
                .also {
                    addDisposable("searchHashtags", it)
                }
    }

    fun searchHashtags(query: String?) {
        searchHashtagsDisposable = hashtagRepository
                .loadHashtags(null, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { onLoadingFinish() }
                .doOnDispose { onLoadingFinish() }
                .subscribe({ _foundHashtags.value = it.content }, { handleError(it) })
                .also {
                    addDisposable("searchHashtags", it)
                }
    }

    fun cancelHashtagsSearching() {
        searchHashtagsDisposable?.dispose()
    }

    private fun getAudioDuration(): Int {
        if (durationMills == -1) {
            durationMills = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
        }
        return durationMills
    }

    private fun onLoadingStart() {
        showLoader()
    }

    private fun onLoadingFinish() {
        hideLoader()
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        ParseErrorUtils.parseError(t, errorsFuncs)
    }

    override fun onCleared() {
        super.onCleared()
        mediaMetadataRetriever.release()
    }
}