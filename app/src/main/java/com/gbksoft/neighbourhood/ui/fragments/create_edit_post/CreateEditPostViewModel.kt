package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.models.response.base.ErrorResponse
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.repositories.isFile
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.base.media.MediaProvider
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostConstruct
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.ValidationDelegate
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class CreateEditPostViewModel(val context: Context) : BaseViewModel() {
    fun errorFields() = errorFieldsLiveData as LiveData<ErrorFieldsModel>
    private val errorFieldsLiveData = MutableLiveData<ErrorFieldsModel>()
            .apply { value = ErrorFieldsModel() }

    fun postMediaList() = postMediaListLiveData as LiveData<List<Media>>
    private var postMediaListLiveData = MutableLiveData<List<Media>>()

    var currentMediaPage: Int = 0
    var lastMediaCount: Int = 0

    private var postCreateLiveData = MutableLiveData<FeedPost>()
    fun postCreate() = postCreateLiveData as LiveData<FeedPost>

    private var postEditLiveData = MutableLiveData<FeedPost>()
    fun postEdit() = postEditLiveData as LiveData<FeedPost>

    private var hashtagsLiveData = SingleLiveEvent<List<Hashtag>>()
    fun hashtags() = hashtagsLiveData as LiveData<List<Hashtag>>

    private val mentionsLiveData = SingleLiveEvent<List<ProfileSearchItem>>()
    fun mentions() = mentionsLiveData as LiveData<List<ProfileSearchItem>>

    private val _scrollToError = SingleLiveEvent<ValidationField>()
    val scrollToError = _scrollToError as LiveData<ValidationField>

    var createPostLiveData = SingleLiveEvent<PostConstruct>()
    var editPostLiveData = SingleLiveEvent<PostConstruct>()

    private val tempMediaDir: File = File(context.cacheDir, "temp_media")
    private val validationDelegate = ValidationDelegate(validationUtils, context)

    private var searchHashtagsDisposable: Disposable? = null
    private var searchMentionsDisposable: Disposable? = null

    var postConstruct = PostConstruct()

    init {
        errorHandler.on422Callback = { onError422(it) }
    }

    private var initialized = false
    fun initByPost(post: Post) {
        if (initialized) return
        postConstruct.post = post
        postConstruct.mediaChangesResolver.setOriginMediaList(post.media)
        postConstruct.postModel.setPost(post)
        postConstruct.postMediaList.clear()
        postConstruct.postMediaList.addAll(post.media)
        postMediaListLiveData.value = postConstruct.postMediaList
        initialized = true
    }

    fun handleAddressResponse(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                postConstruct.postModel.setAddress(place)
            }
            AutocompleteActivity.RESULT_ERROR -> {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
                Timber.i(status.statusMessage)
            }
            Activity.RESULT_CANCELED -> {
                // The user canceled the operation.
                Timber.d("set place canceled")
            }
        }
    }

    fun searchHashtags(query: String?) {
        searchHashtagsDisposable = RepositoryProvider.hashtagsRepository
                .loadHashtags(null, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.content.let { content -> hashtagsLiveData.value = content }
                }, { handleError(it) })
    }

    fun cancelHashtagsSearching() {
        searchHashtagsDisposable?.dispose()
    }

    fun searchMentions(query: String?) {
        searchMentionsDisposable = RepositoryProvider.globalSearchRepository
                .findProfiles(query, sort = "-isFollowed,fullName")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it.content.let { content -> mentionsLiveData.value = content } }, { handleError(it) })

    }

    fun cancelMentionsSearching() {
        searchMentionsDisposable?.dispose()
    }

    fun addVideoAttachment(uri: Uri) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validateVideoFileSize(errorFieldsModel, uri)
        if (!errorFieldsModel.isValid) {
            val error = errorFieldsModel.errorsMap[ValidationField.POST_VIDEO]
            ToastUtils.showToastMessage(error)
            return
        }

        postConstruct.postMediaList.add(Media.Video.local(uri))
        postMediaListLiveData.value = postConstruct.postMediaList
        postConstruct.postModel.resolveMediaVisibility(postConstruct.postMediaList)
    }

    fun addPictureAttachment(uri: Uri, previewArea: Rect) {
        val picture = Media.Picture.local(uri)
        picture.previewArea = previewArea
        postConstruct.postMediaList.add(picture)
        postMediaListLiveData.value = postConstruct.postMediaList
        postConstruct.postModel.resolveMediaVisibility(postConstruct.postMediaList)
    }

    fun addAudioAttachment(audio: Media.Audio) {
        postConstruct.postMediaList.add(audio)
    }

    fun removeAllAudioFiles() {
        postConstruct.postMediaList = postConstruct.postMediaList.filter { it !is Media.Audio }.toMutableList()
    }


    fun removeMedia(postMedia: Media) {
        postConstruct.postMediaList.remove(postMedia)
        postMediaListLiveData.value = postConstruct.postMediaList
        postConstruct.postModel.resolveMediaVisibility(postConstruct.postMediaList)
        if (postMedia.isLocal() && postMedia.origin.isFile()) {
            removeIfTempCameraFile(postMedia.preview.toFile())
        }
    }

    fun removeAudioMedia(path: Media.Audio) {
        path.let { postMedia ->
            postConstruct.postMediaList.remove(postMedia)
            if (postMedia.isLocal() && postMedia.origin.isFile()) {
                removeIfTempCameraFile(postMedia.preview.toFile())
            }
        }
    }

    private fun removeIfTempCameraFile(file: File) {
        if (file.absolutePath.startsWith(tempMediaDir.absolutePath)) file.delete()
    }

    fun createEditPost() {
        val errorFieldsModel = validationDelegate.validate(postConstruct.post, postConstruct.postModel)
        errorFieldsLiveData.postValue(errorFieldsModel)
        if (errorFieldsModel.isValid) {
            if (postConstruct.post.isCreation()) {
                createPostLiveData.postValue(postConstruct)
            } else {
                editPostLiveData.postValue(postConstruct)
            }
        } else {
            scrollToError(errorFieldsModel)
        }
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        ParseErrorUtils.parseError(t, errorsFuncs)
    }

    private fun onLoadingFinish() {
        hideLoader()
        changeControlState(R.id.btnPost, true)
        changeControlState(R.id.addMedia, true)
        changeControlState(R.id.btnAddMedia, true)
        changeControlState(R.id.addedMediaPager, true)
    }

    override fun onCleared() {
        super.onCleared()
        clearTempPhotos()
        onLoadingFinish()
    }

    private fun clearTempPhotos() {
        val children: Array<String> = tempMediaDir.list() ?: return

        for (i in children.indices) {
            File(tempMediaDir, children[i]).delete()
        }
    }

    fun setStartDateTime(timeInMillis: Long) {
        postConstruct.postModel.startDateTime.set(timeInMillis)
    }

    fun getStartDateTime(): Long? = postConstruct.postModel.startDateTime.get()

    fun setEndDateTime(timeInMillis: Long) {
        postConstruct.postModel.endDateTime.set(timeInMillis)
    }

    fun getEndDateTime(): Long? = postConstruct.postModel.endDateTime.get()

    fun setPreparedDescription(description: String) {
        postConstruct.postModel.preparedDescription = description
    }

    fun getAvailableMediaType(): MediaProvider.Type? {
        return when {
            postConstruct.postMediaList.isEmpty() -> null
            postConstruct.postMediaList[0] is Media.Picture -> MediaProvider.Type.PICTURE
            postConstruct.postMediaList[0] is Media.Video -> MediaProvider.Type.VIDEO
            else -> null
        }
    }

    private fun onError422(errors: List<ErrorResponse>): Boolean {
        val unhandledErrors = mutableListOf<ErrorResponse>()
        val errorFieldsModel = ErrorFieldsModel()
        scrollToError(errorFieldsModel)
        for (error in errors) {
            val field: ValidationField? = fetchField(error.field)
            field?.let {
                errorFieldsModel.addError(field, error.message)
            } ?: run {
                unhandledErrors.add(error)
            }
        }
        errorFieldsLiveData.postValue(errorFieldsModel)
        return unhandledErrors.isNotEmpty()
    }

    private fun scrollToError(errorFieldsModel: ErrorFieldsModel) {
        val fields = errorFieldsModel.errorsMap.keys
        when {
            fields.contains(ValidationField.CATEGORY) -> _scrollToError.value = ValidationField.CATEGORY
            fields.contains(ValidationField.NAME) -> _scrollToError.value = ValidationField.NAME
            fields.contains(ValidationField.ADDRESS) -> _scrollToError.value = ValidationField.ADDRESS
            fields.contains(ValidationField.PRICE) -> _scrollToError.value = ValidationField.PRICE
            fields.contains(ValidationField.START_DATE_TIME) -> _scrollToError.value = ValidationField.START_DATE_TIME
            fields.contains(ValidationField.END_DATE_TIME) -> _scrollToError.value = ValidationField.END_DATE_TIME
            fields.contains(ValidationField.DESCRIPTION) -> _scrollToError.value = ValidationField.DESCRIPTION
        }
    }

    private fun fetchField(field: String?): ValidationField? {
        when (field) {
            "description" -> return ValidationField.DESCRIPTION
            "categories" -> return ValidationField.CATEGORY
            "name" -> return ValidationField.NAME
            "placeId" -> return ValidationField.ADDRESS
            "price" -> return ValidationField.PRICE
            "startDatetime" -> return ValidationField.START_DATE_TIME
            "endDatetime" -> return ValidationField.END_DATE_TIME
        }
        return null
    }

    fun restorePostConstruct(postConstruct1: PostConstruct) {
        postConstruct = postConstruct1
        postMediaListLiveData.value = postConstruct.postMediaList
        postConstruct.postModel.resolveMediaVisibility(postConstruct.postMediaList)
    }


}