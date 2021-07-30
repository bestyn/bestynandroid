package com.gbksoft.neighbourhood.ui.activities.main

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.data.repositories.PrivateChatRepository
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.repositories.StoryRepository
import com.gbksoft.neighbourhood.data.repositories.payments.PaymentRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.AppType
import com.gbksoft.neighbourhood.model.chat.MessageEvent
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.MyProfile
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.activities.main.FloatingMenuDelegate.Companion.postIsPublishing
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostConstruct
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.RepositoryEndpointProvider
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.posts.MyNeighbourhoodFeedFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilder
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.StoryCreationFormBuilder
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File

class MainActivityViewModel(
        private val context: Context,
        private val paymentRepository: PaymentRepository,
        private val privateChatRepository: PrivateChatRepository,
        private val storyRepository: StoryRepository,
        private val postDataRepository: PostDataRepository
) : BaseViewModel() {
    private val profileRepository = RepositoryProvider.profileRepository

    private var repositoryEndpointProvider = RepositoryEndpointProvider()

    private val res: Resources = context.resources

    private var lastProfileId: Long = -1

    private val _hasNewChatMessages = MutableLiveData<Boolean>()
    val hasNewChatMessages = _hasNewChatMessages as LiveData<Boolean>

    private val _hasUnreadNotifications = MutableLiveData<Boolean>()
    val hasUnreadNotifications = _hasUnreadNotifications as LiveData<Boolean>

    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>

    private val _navigateToPostDetails = SingleLiveEvent<Long>()
    val navigateToPostDetails: LiveData<Long> = _navigateToPostDetails

    private var checkSwitchProfileDisposable: Disposable? = null
    val navigateToStory = SingleLiveEvent<Long>()
    val switchProfile = SingleLiveEvent<Unit>()

    private val _storyCreated = SingleLiveEvent<Unit>()
    val storyCreated = _storyCreated as LiveData<Unit>

    val postCreatedEdited = SingleLiveEvent<Pair<PostConstruct, Boolean>>()

    private var unreadMessagesDisposable: Disposable? = null
    var isCreatingStory = false

    init {
        subscribeToUnreadNotifications()
        subscribeCurrentProfile()
        subscribeToChatActions()
    }

    private fun subscribeCurrentProfile() {
        addDisposable("subscribeCurrentProfile", sharedStorage.subscribeCurrentProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _currentProfile.value = it
                    if (lastProfileId != it.id) {
                        lastProfileId = it.id
                        subscribeToUnreadMessages(it.id)
                    }
                }) { it.printStackTrace() })
    }

    private fun subscribeToUnreadMessages(profileId: Long) {
        unreadMessagesDisposable?.dispose()
        val disposable = profileRepository
                .subscribeHasUnreadMessages(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _hasNewChatMessages.value = it
                }, {
                    it.printStackTrace()
                })
        unreadMessagesDisposable = disposable
        addDisposable("subscribeToUnreadMessages", disposable)
    }

    private fun subscribeToUnreadNotifications() {
        addDisposable("subscribeToUnreadNotifications", profileRepository
                .subscribeHasUserUnreadMessages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _hasUnreadNotifications.value = it
                }, {
                    it.printStackTrace()
                }))
    }

    private fun subscribeToChatActions() {
        addDisposable("subscribeToChatActions", privateChatRepository
                .subscribeToChatActions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    checkHasUnreadMessages(it)
                }) { it.printStackTrace() })
    }

    private fun checkHasUnreadMessages(msgEvent: MessageEvent) {
        val extraData = msgEvent.extraData ?: return
        val recipientId = msgEvent.message.recipient?.id ?: return

        profileRepository.setHasUnreadMessages(recipientId, extraData.hasProfileUnreadMessages)
    }

    fun checkSubscriptionPurchase() {
        addDisposable("restorePurchase", paymentRepository.restorePurchase().ignoreElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, { it.printStackTrace() }))
    }

    fun checkPostType(postId: Long) {
        if (postId < 0) return
        addDisposable("checkPostType", postDataRepository.getPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.type == PostType.STORY) {
                        navigateToStory.value = postId
                    } else {
                        _navigateToPostDetails.value = postId
                    }
                }, { it.printStackTrace() }))
    }

    fun getUnAuthorizedStoryId() = sharedStorage.getUnAuthorizedStoryId()

    fun checkShouldSwitchProfile(profileId: Long) {
        checkSwitchProfileDisposable = profileRepository.subscribeCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onProfileLoaded(profileId, it) }) { it.printStackTrace() }
    }

    private fun onProfileLoaded(profileId: Long, userModel: UserModel) {
        checkSwitchProfileDisposable?.dispose()
        val profiles = ProfileMapper.toMyProfiles(userModel)
        val destinationProfile = profiles.find { it.id == profileId } ?: return

        sharedStorage.getCurrentProfile()?.let {
            if (it.id != destinationProfile.id) {
                switchProfile(destinationProfile)
            } else {
                switchProfile.call()
            }
        } ?: kotlin.run {
            switchProfile(destinationProfile)
        }
    }

    private fun switchProfile(profile: MyProfile) {
        sharedStorage.setCurrentProfile(profile)
        val msg = context.getString(R.string.msg_profile_switched, profile.title)
        ToastUtils.showToastMessage(msg)
        switchProfile.call()
    }

    fun createStory(constructStory: ConstructStory, creationFormBuilder: StoryCreationFormBuilder) {
        isCreatingStory = true
        val storyBuilder = StoryBuilder(context, constructStory)
        storyBuilder.build { video ->
            creationFormBuilder.model.video.set(video)
            creationFormBuilder.prepare()
            addDisposable("createStory", storyRepository
                    .createStory(creationFormBuilder.build())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onStoryCreated() }, { it.printStackTrace() }))
        }
    }

    fun onStoryCreated() {
        isCreatingStory = false
        _storyCreated.call()
    }

    fun createPost(postConstruct: PostConstruct) {
        addDisposable("createPost", repositoryEndpointProvider
                .provideCreatePostEndpoint(postConstruct.post, postConstruct.postModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    postConstruct.feedPost = it
                    handlePostMediaList(postConstruct)
                    Log.d("post_create_edit", "Post created ")
                }, {
                    handleError(it)
                    Log.d("post_create_edit", "Post created error $it")
                }))
    }

    fun updatePost(postConstruct: PostConstruct) {
        addDisposable("updatePost", repositoryEndpointProvider
                .provideUpdatePostEndpoint(postConstruct.post, postConstruct.postModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    postConstruct.feedPost = it
                    handlePostMediaList(postConstruct)
                    Log.d("post_create_edit", "Post edited ")
                }, {
                    handleError(it)
                    MyNeighbourhoodFeedFragment.lastPostDescription = ""
                    postIsPublishing = false
                    Log.d("post_create_edit", "Post edited error $it")
                }))
    }


    private fun handlePostMediaList(postConstruct: PostConstruct) {
        postConstruct.mediaChangesResolver.resolve(postConstruct.postMediaList)
        checkMediaToDelete(postConstruct)
    }

    private fun checkMediaToDelete(postConstruct: PostConstruct) {
        if (postConstruct.mediaChangesResolver.containsMediaToDelete()) {
            deletePostMedia(postConstruct.mediaChangesResolver.getMediaForDelete(), postConstruct)
        } else {
            checkMediaToUpload(postConstruct)
        }
    }

    private fun deletePostMedia(mediaList: List<Media>, postConstruct: PostConstruct) {
        addDisposable("deletePostMedia", RepositoryProvider.postDataRepository
                .deletePostMedia(mediaList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ checkMediaToUpload(postConstruct) }, { handleError(it) }))
    }

    private fun checkMediaToUpload(postConstruct: PostConstruct) {
        if (postConstruct.mediaChangesResolver.containsMediaToUpload()) {
            val postId = postConstruct.feedPost!!.post.id
            uploadPostMedia(postId, postConstruct.mediaChangesResolver.getMediaForUpload(), postConstruct)
        } else {
            onUploadingFinished(postConstruct)
        }
    }

    private fun uploadPostMedia(postId: Long, mediaList: List<Media>, postConstruct: PostConstruct) {
        Thread(Runnable {
            addDisposable("uploadPostMedia", RepositoryProvider.postDataRepository
                    .uploadPostMedia(postId, mediaList)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        onUploadingFinished(postConstruct)
                        Log.d("post_create_edit", "onUploadingFinished")
                    }, {
                        handleError(it)
                        MyNeighbourhoodFeedFragment.lastPostDescription = ""
                        postIsPublishing = false
                        Log.d("post_create_edit", "Post upload error $it")
                    }))
        }).start()
    }

    fun uploadPostMedia2(postId: Long, mediaList: List<Media>, postConstruct: PostConstruct) {
        CoroutineScope(Dispatchers.IO).launch {

            mediaList.forEach { media ->
                val file = File(media.origin.path.toString())
                val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()

                val content_type =  mimeTypeMap.getExtensionFromMimeType(NApplication.context.contentResolver.getType(media.origin)) ?: ""

                val file_path = file.absolutePath

                val okHttpClient = OkHttpClient()
                val file_body = RequestBody.create(content_type.toMediaTypeOrNull(), file)

                val req = postDataRepository.createUploadMediaReq(media)

                val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", content_type)
                        .addFormDataPart("uploaded_file", file_path.substringAfterLast('/'))
                        /*.addPart(req)*/
                        .build()

                val request = Request.Builder()
                        .url("${String.format("%s%s", BuildConfig.API_BASE_URL, BuildConfig.API_BASE_PATH)}v1/posts/${postId}/media")
                        .addHeader("Authorization", "Bearer 8adddc08caa6c39680a7237f6a00c31fe76ebca3862f1f98b86a6884f626320c226_xKQvZ0rqOyt7hIjcUhQysltYgCSW3xt2wJt7Bm7_WXtzVC3VabqtXpNBnXbX")
                        .post(requestBody)
                        .build()

                val response = okHttpClient.newCall(request).execute()

                val body = response.body

                Log.d("file_to_server", response.toString())
                Log.d("file_to_server", response.body.toString())

            }
        }
    }


    private fun onUploadingFinished(postConstruct: PostConstruct) {
        hideLoader()
        changeControlState(R.id.btnPost, true)
        if (postConstruct.post.isCreation()) onPostCreated(postConstruct.feedPost!!, postConstruct)
        else onPostEdited(postConstruct.feedPost!!, postConstruct)
    }

    private fun onPostCreated(feedPost: FeedPost, postConstruct: PostConstruct) {
        logToAnalyticsPostCreated(feedPost)
        postCreatedEdited.postValue(Pair(postConstruct, true))
        sharedStorage.saveCurrentAppType(AppType.STORIES)
    }


    private fun onPostEdited(feedPost: FeedPost, postConstruct: PostConstruct) {
        postCreatedEdited.postValue(Pair(postConstruct, false))
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        ParseErrorUtils.parseError(t, errorsFuncs)
    }

    private fun logToAnalyticsPostCreated(feedPost: FeedPost) {
        when (feedPost.type) {
            PostType.GENERAL -> Analytics.onCreatedGeneralPost(feedPost.post.id)
            PostType.NEWS -> Analytics.onCreatedNewsPost(feedPost.post.id)
            PostType.CRIME -> Analytics.onCreatedCrimePost(feedPost.post.id)
            PostType.OFFER -> Analytics.onCreatedOfferPost(feedPost.post.id)
            PostType.EVENT -> Analytics.onCreatedEvent(feedPost.post.id)
        }
    }

}