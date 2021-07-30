package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.repositories.StoryRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.StoryCreationFormBuilder
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.StoryEditingFormBuilder
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.StoryValidationDelegate
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class StoryDescriptionViewModel(
        private val context: Context,
        private val constructStory: ConstructStory,
        private val storyRepository: StoryRepository,
        private val globalSearchRepository: GlobalSearchRepository
) : BaseViewModel() {
    val isCreationMode = constructStory.post == null
    val storyModel: StoryDescriptionModel = StoryDescriptionModel().apply {
        if (constructStory.post != null) {
            video.set(constructStory.post.media[0].origin)
            posterUrl.set(constructStory.post.media[0].preview)
            description.set(constructStory.post.description)
            address.set(constructStory.post.address)
            isAllowComments.set(constructStory.post.allowedComment)
            isAllowDuet.set(constructStory.post.allowedDuet)
            audioId.set(constructStory.audio?.id)
        } else {
            audioId.set(constructStory.audio?.id)
            posterTimestamp.set(Constants.STORY_DEFAULT_COVER_TIMESTAMP)
        }
    }

    private val _foundHashtags = SingleLiveEvent<List<Hashtag>>()
    val foundHashtags = _foundHashtags as LiveData<List<Hashtag>>

    private val _foundMentions = SingleLiveEvent<List<ProfileSearchItem>>()
    val foundMentions = _foundMentions as LiveData<List<ProfileSearchItem>>

    private val _errorFields = MutableLiveData<ErrorFieldsModel>()
    val errorFields = _errorFields as LiveData<ErrorFieldsModel>

    private val _storyCreated = SingleLiveEvent<Boolean>()
    val storyCreated = _storyCreated as LiveData<Boolean>

    private val _storyUpdated = SingleLiveEvent<FeedPost>()
    val storyUpdated = _storyUpdated as LiveData<FeedPost>

    private val _editingCanceled = SingleLiveEvent<Boolean>()
    val editingCanceled = _editingCanceled as LiveData<Boolean>

    private val _navigateToBusinessProfile = SingleLiveEvent<Long>()
    val navigateToBusinessProfile = _navigateToBusinessProfile as LiveData<Long>

    private val _navigateToBasicProfile = SingleLiveEvent<Long>()
    val navigateToBasicProfile = _navigateToBasicProfile as LiveData<Long>

    private val _createStory = SingleLiveEvent<StoryCreationFormBuilder>()
    val createStory = _createStory as LiveData<StoryCreationFormBuilder>

    private var searchHashtagsDisposable: Disposable? = null
    private var searchMentionsDisposable: Disposable? = null
    private val validationDelegate = StoryValidationDelegate(validationUtils)
    private val creationFormBuilder by lazy {
        StoryCreationFormBuilder(storyModel, validationDelegate)
    }
    private val editingFormBuilder by lazy {
        StoryEditingFormBuilder(constructStory.post, storyModel, validationDelegate)
    }

    fun handleAddressResponse(resultCode: Int, data: Intent?) {
        data ?: return
        when (resultCode) {
            Activity.RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(data)
                storyModel.setAddress(place)
            }
            AutocompleteActivity.RESULT_ERROR -> {
                val status = Autocomplete.getStatusFromIntent(data)
                Timber.i(status.statusMessage)
            }
            Activity.RESULT_CANCELED -> {
                Timber.d("set place canceled")
            }
        }
    }

    fun searchHashtags(query: String?) {
        searchHashtagsDisposable = RepositoryProvider.hashtagsRepository
                .loadHashtags(null, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it.content.let { res -> _foundHashtags.value = res } }, { handleError(it) })
                .also {
                    addDisposable("searchHashtags", it)
                }

    }

    fun cancelHashtagsSearching() {
        searchHashtagsDisposable?.dispose()
    }

    fun searchMentions(query: String?) {
        searchMentionsDisposable = globalSearchRepository
                .findProfiles(query, sort = "-isFollowed,fullName")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it.content.let { res -> _foundMentions.value = res } }, { handleError(it) })

    }

    fun cancelMentionsSearching() {
        searchMentionsDisposable?.dispose()
    }

    fun setPreparedDescription(description: String) {
        storyModel.preparedDescription = description
    }

    fun hasEditingChanges(): Boolean {
        val story = constructStory.post ?: return false

        return when {
            story.description != storyModel.description.get() -> true
            story.address != storyModel.address.get() -> true
            story.allowedComment != storyModel.isAllowComments.get() -> true
            story.allowedDuet != storyModel.isAllowDuet.get() -> true
            else -> false
        }
    }

    fun postStory() {
        if (isCreationMode) createStory()
        else updateStory()
    }

    private fun createStory() {
        val errorFieldsModel: ErrorFieldsModel = creationFormBuilder.validateData()
        if (!errorFieldsModel.isValid) {
            _errorFields.postValue(errorFieldsModel)
            return
        }

        _createStory.value = creationFormBuilder
    }

    private fun updateStory() {
        val errorFieldsModel: ErrorFieldsModel = editingFormBuilder.validateData()
        if (!errorFieldsModel.isValid) {
            _errorFields.postValue(errorFieldsModel)
            return
        }

        onLoadingStart()

        val form = editingFormBuilder.build()
        if (form == null) {
            onLoadingFinish()
            onEditingCanceled()
            return
        }

        form.description = storyModel.preparedDescription

        addDisposable("updateStory", storyRepository
                .updateStory(form)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { onLoadingFinish() }
                .doOnDispose { onLoadingFinish() }
                .subscribe({ onUpdateSuccess(it) }, { handleError(it) }))
    }

    private fun onUpdateSuccess(feedPost: FeedPost) {
        //ToastUtils.showToastMessageLong(R.string.toast_story_updated)
        _storyUpdated.value = feedPost
    }

    private fun onEditingCanceled() {
        _editingCanceled.value = true
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
}