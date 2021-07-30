package com.gbksoft.neighbourhood.ui.fragments.post_details

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.forms.BusinessProfileEditing
import com.gbksoft.neighbourhood.data.models.request.user.UpdateProfileReq
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageEvent
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.base.chat.ValidationDelegate
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.DownloadProgressCallback
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.FileUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.download.AppDownloader
import com.gbksoft.neighbourhood.utils.media.BitmapResizeResult
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import kotlin.math.max

class PostDetailsViewModel(val context: Context) : BaseViewModel() {
    private val myPostsRepository = RepositoryProvider.myPostsRepository
    private val postActionsRepository = RepositoryProvider.postActionsRepository
    private val postDataRepository = RepositoryProvider.postDataRepository
    private val postChatReportRepository = RepositoryProvider.postChatRepository
    private val profileRepository = RepositoryProvider.profileRepository
    private val globalSearchRepository = RepositoryProvider.globalSearchRepository
    private val validationDelegate = ValidationDelegate(validationUtils, context)

    private val _feedPost = MutableLiveData<FeedPost>()
    val feedPost = _feedPost as LiveData<FeedPost>

    private val _openAuthor = SingleLiveEvent<ProfileData>()
    val openAuthor = _openAuthor as LiveData<ProfileData>

    private val _finishWithResult = SingleLiveEvent<ResultData<PostResult>>()
    val finishWithResult = _finishWithResult as LiveData<ResultData<PostResult>>

    private val _messageAttachment = SingleLiveEvent<Attachment?>()
    val messageAttachment = _messageAttachment as LiveData<Attachment?>

    private val _messageSendingProcess = MutableLiveData<Boolean>()
    val messageSendingProcess = _messageSendingProcess as LiveData<Boolean>

    private val _clearMessageForm = SingleLiveEvent<Boolean>()
    val clearMessageForm = _clearMessageForm as LiveData<Boolean>

    private val _postComments = MutableLiveData<List<Message>>()
    val postComments = _postComments as LiveData<List<Message>>

    private val _foundMentions = SingleLiveEvent<List<ProfileSearchItem>>()
    val foundMentions = _foundMentions as LiveData<List<ProfileSearchItem>>
    private var searchMentionsDisposable: Disposable? = null

    private val _navigateToMyProfile = SingleLiveEvent<Unit>()
    val navigateToMyProfile = _navigateToMyProfile as LiveData<Unit>

    private val _navigateToMyBusinessProfile = SingleLiveEvent<Unit>()
    val navigateToMyBusinessProfile = _navigateToMyBusinessProfile as LiveData<Unit>

    private val _navigateToPublicProfile = SingleLiveEvent<Long>()
    val navigateToPublicProfile = _navigateToPublicProfile as LiveData<Long>

    private val _navigateToPublicBusinessProfile = SingleLiveEvent<Long>()
    val navigateToPublicBusinessProfile = _navigateToPublicBusinessProfile as LiveData<Long>

    private lateinit var originFeedPost: FeedPost
    private lateinit var currentFeedPost: FeedPost
    private val comments = MessageList()
    private val paginationBuffer = Constants.POST_CHAT_PAGINATION_BUFFER

    private var followRequest = false
    private var isCommentsLoading = false
    private var lastLoadedCommentsCount = 0
    private var isSubscribedToMessages = false
    private var editingTextMessage: Message.Text? = null
    private val appDownloader by lazy { AppDownloader(context) }

    var isDescriptionExpanded = false
    var currentMediaPage = -1
    val currentProfile = sharedStorage.requireCurrentProfile()

    /** LocalFile<Int> type is one of [Attachment] TYPE_.. */
    private var messageAttachmentFile: LocalFile<Int>? = null

    fun setFeedPost(feedPost: FeedPost) {
        originFeedPost = feedPost
        currentFeedPost = originFeedPost.copy()
        currentFeedPost.isMine = sharedStorage.getCurrentProfile()?.id == feedPost.profile.id
        _feedPost.value = currentFeedPost
        checkPostUpdates(feedPost.type, feedPost.post.id)
        loadPostMessages(originFeedPost.post.id, null)
    }

    fun setPostId(postId: Long) {
        refreshPost(null, postId)
        loadPostMessages(postId, null)
    }

    private fun checkPostUpdates(type: PostType, id: Long) {
        addDisposable("refreshPost", getRefreshEndpoint(type, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (currentFeedPost != it) updateFeedPost(it)
                }, {
                    handleError(it)
                }))
    }

    fun getCurrentProfileId(): Long {
        return sharedStorage.getCurrentProfile()?.id
                ?: throw NullPointerException("currentProfile = null")
    }

    fun onAuthorClick() {
        sharedStorage.getCurrentProfile()?.let {
            if (it.id == currentFeedPost.profile.id) {
                _openAuthor.value = ProfileData(currentFeedPost.profile, true)
            } else {
                _openAuthor.value = ProfileData(currentFeedPost.profile, false)
            }
        }
    }

    fun onReactionClick(reaction: Reaction) {
        val shouldAddReaction = currentFeedPost.myReaction == Reaction.NO_REACTION
        val endpoint = if (shouldAddReaction) {
            postActionsRepository.addPostReaction(currentFeedPost.post.id, reaction)
        } else {
            postActionsRepository.removePostReaction(currentFeedPost.post.id)
        }

        addDisposable("AddReaction", endpoint
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (shouldAddReaction) {
                        onAddReactionSuccess(reaction)
                    } else {
                        onRemoveReactionSuccess(currentFeedPost.myReaction)
                    }
                }, { handleError(it) }))
    }

    private fun onAddReactionSuccess(reaction: Reaction) {
        val curReactionCount = currentFeedPost.reactions[reaction] ?: 0
        currentFeedPost.reactions[reaction] = curReactionCount + 1
        currentFeedPost.myReaction = reaction
        _feedPost.value = currentFeedPost
    }

    private fun onRemoveReactionSuccess(prevReaction: Reaction) {
        val curReactionCount = currentFeedPost.reactions[prevReaction] ?: 0
        currentFeedPost.reactions[prevReaction] = max(curReactionCount - 1, 0)
        currentFeedPost.myReaction = Reaction.NO_REACTION
        _feedPost.value = currentFeedPost
    }

    fun onFollowClick() {
        if (followRequest) return
        followRequest = true
        val feedPost = currentFeedPost
        addDisposable("FollowPost", postActionsRepository.follow(feedPost.post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { followRequest = false }
                .subscribe({ onFollowSuccess(feedPost) }, {
                    handleFollowError(it) { onFollowSuccess(feedPost) }
                }))
    }


    private fun onFollowSuccess(feedPost: FeedPost) {
        Analytics.onFollowedOtherPost(feedPost.post.id)
        currentFeedPost.followers++
        currentFeedPost.iFollow = true
        _feedPost.value = currentFeedPost
    }

    fun onUnfollowClick() {
        if (followRequest) return
        followRequest = true
        addDisposable("FollowPost", postActionsRepository.unfollow(currentFeedPost.post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { followRequest = false }
                .subscribe({ onUnfollowSuccess() }, {
                    handleFollowError(it) { onUnfollowSuccess() }
                }))
    }

    private fun onUnfollowSuccess() {
        currentFeedPost.followers--
        currentFeedPost.iFollow = false
        _feedPost.value = currentFeedPost
    }

    private fun handleFollowError(it: Throwable, onForbiddenErrorRunnable: () -> Unit) {
        if (it is HttpException && it.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            onForbiddenErrorRunnable.invoke()
        } else {
            handleError(it)
        }
    }

    fun deletePost() {
        addDisposable("deletePost", myPostsRepository.deletePost(currentFeedPost.post.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onPostDeleted() }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    private fun onPostDeleted() {
        when (currentFeedPost.type) {
            PostType.EVENT -> ToastUtils.showToastMessage(context.getString(R.string.event_deleted_msg))
            PostType.MEDIA -> ToastUtils.showToastMessage(context.getString(R.string.image_deleted_msg))
            else -> ToastUtils.showToastMessage(context.getString(R.string.post_deleted_msg))
        }
        _finishWithResult.value = ResultData(PostResult.onDeleted(currentFeedPost))
    }

    fun refreshPost(type: PostType?, id: Long) {
        addDisposable("refreshPost", getRefreshEndpoint(type, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateFeedPost(it) }, { handleError(it) }))
    }

    private fun updateFeedPost(feedPost: FeedPost) {
        currentFeedPost = feedPost
        _feedPost.value = currentFeedPost
    }

    private fun getRefreshEndpoint(postType: PostType?, postId: Long): Observable<FeedPost> {
        return when (postType) {
            PostType.GENERAL -> postDataRepository.getPostGeneral(postId)
            PostType.NEWS -> postDataRepository.getPostNews(postId)
            PostType.CRIME -> postDataRepository.getPostCrime(postId)
            PostType.OFFER -> postDataRepository.getPostOffer(postId)
            PostType.EVENT -> postDataRepository.getPostEvent(postId)
            PostType.MEDIA -> postDataRepository.getPostMedia(postId)
            PostType.STORY -> postDataRepository.getPostStory(postId)
            null -> postDataRepository.getPost(postId)
        }
    }

    fun onBackPressed() {
        _finishWithResult.value = ResultData(PostResult.onChanged(currentFeedPost))
    }

    private fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    private fun loadPostMessages(postId: Long, lastMessageId: Long?) {
        isCommentsLoading = true
        addDisposable("loadPostChat", postChatReportRepository
                .getPostMessages(postId, lastMessageId)
                .doOnTerminate { isCommentsLoading = false }
                .map { it.sortedByDescending { message -> message.createdAt } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onNextMessagesLoaded(it)
                    checkMessageSubscription(postId)
                }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    private fun onNextMessagesLoaded(messages: List<Message>) {
        lastLoadedCommentsCount = messages.size
        comments.add(messages)
        _postComments.value = comments.asList()
    }

    private fun checkMessageSubscription(postId: Long) {
        if (isSubscribedToMessages) return
        isSubscribedToMessages = true
        addDisposable("MessageSubscription", postChatReportRepository
                .subscribeToMessageEvents(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    onMessageListEvent(event)
                }, {
                    it.printStackTrace()
                    isSubscribedToMessages = false
                }))
    }

    private fun onMessageListEvent(event: MessageEvent) {
        val changed: Boolean = when (event.eventType) {
            MessageEvent.CREATE -> {
                comments.addSingle(event.message)
                messageCounterIncrement()
                true
            }
            MessageEvent.UPDATE -> {
                comments.update(event.message)
                true
            }
            MessageEvent.DELETE -> {
                val deleted = comments.delete(event.messageId)
                if (deleted) messageCounterDecrement()
                deleted
            }
            else -> false
        }
        if (changed) {
            _postComments.value = comments.asList()
        }
    }

    private fun messageCounterIncrement() {
        currentFeedPost.messages++
        currentFeedPost.followers++
        currentFeedPost.iFollow = true
        _feedPost.value = currentFeedPost
    }

    private fun messageCounterDecrement() {
        currentFeedPost.messages--
        _feedPost.value = currentFeedPost
    }

    fun onVisibleCommentChanged(position: Int) {
        if (isCommentsLoading) return
        if (lastLoadedCommentsCount == 0) return
        val needLoadMore: Boolean = position + paginationBuffer >= comments.size()
        if (!needLoadMore) return
        loadPostMessages(currentFeedPost.post.id, comments.last().id)
    }

    fun addMessageLocalAttachment(localFile: LocalFile<Int>) {
        val attachmentType = localFile.type ?: return

        when (attachmentType) {
            Attachment.TYPE_PICTURE -> {
            }
            Attachment.TYPE_VIDEO -> {
                val errorFieldsModel = ErrorFieldsModel()
                validationDelegate.validateVideoFileSize(errorFieldsModel, localFile.size.toInt())
                if (!errorFieldsModel.isValid) {
                    val error = errorFieldsModel.errorsMap[ValidationField.ATTACHMENT_FILE]
                    ToastUtils.showToastMessage(error)
                    return
                }
            }
            Attachment.TYPE_FILE -> {
                val errorFieldsModel = ErrorFieldsModel()
                validationDelegate.validateFileSize(errorFieldsModel, localFile.size.toInt())
                if (!errorFieldsModel.isValid) {
                    val error = errorFieldsModel.errorsMap[ValidationField.ATTACHMENT_FILE]
                    ToastUtils.showToastMessage(error)
                    return
                }
            }
        }

        _messageAttachment.value = Attachment(0, attachmentType,
                localFile.name, "", "")
        messageAttachmentFile = localFile
    }

    fun removeMessageAttachment() {
        _messageAttachment.value = null
        messageAttachmentFile = null
    }

    fun sendPostTextMessage(text: String?, attachment: Attachment?) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validatePostMessageTextMaxLength(errorFieldsModel, text)
        if (!errorFieldsModel.isValid) {
            val error = errorFieldsModel.errorsMap[ValidationField.POST_MESSAGE]
            ToastUtils.showToastMessage(error)
            return
        }

        val postId = originFeedPost.post.id
        onMessageSendingStart()
        addDisposable("sendPostMessage", postChatReportRepository
                .sendPostMessage(postId, text, messageAttachmentFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { onMessageSendingFinish() }
                .subscribe({
                    Analytics.onAddedCommentOnPost(postId)
                    clearMessageForm()
                }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    fun updatePostTextMessage(oldMessage: Message.Text, text: String?, attachment: Attachment?) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validatePostMessageTextMaxLength(errorFieldsModel, text)
        if (!errorFieldsModel.isValid) {
            val error = errorFieldsModel.errorsMap[ValidationField.POST_MESSAGE]
            ToastUtils.showToastMessage(error)
            return
        }

        if (oldMessage.text == text && oldMessage.attachment == attachment) {
            clearMessageForm()
            return
        }

        val updateRequest = messageAttachmentFile?.let {
            postChatReportRepository.updatePostMessage(oldMessage, text, it)
        } ?: run {
            val deleteAttachment = oldMessage.attachment != null && attachment == null
            postChatReportRepository.updatePostMessage(oldMessage, text, deleteAttachment)
        }
        onMessageSendingStart()
        addDisposable("updatePostTextMessage", updateRequest
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { onMessageSendingFinish() }
                .subscribe({
                    clearMessageForm()
                }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    private fun onMessageSendingStart() {
        showLoader()
        _messageSendingProcess.value = true
    }

    private fun onMessageSendingFinish() {
        hideLoader()
        _messageSendingProcess.value = false
    }

    fun setEditingTextMessage(textMessage: Message.Text) {
        editingTextMessage = textMessage
    }

    private fun clearMessageForm() {
        editingTextMessage = null
        messageAttachmentFile = null
        _clearMessageForm.value = true
    }

    fun deleteMessage(message: Message) {
        Timber.tag("UpdateTag").d("deleteMessage, before id: ${message.id}")
        addDisposable("deleteMessage_${message.id}", postChatReportRepository.deletePostMessage(message.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onMessageDeleted(message.id)
                }, {
                    handleError(it)
                }))
    }

    private fun onMessageDeleted(messageId: Long) {
        editingTextMessage?.let { if (it.id == messageId) clearMessageForm() }
        if (comments.delete(messageId)) {
            _postComments.value = comments.asList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        hideLoader()
    }

    fun downloadFile(progressCallback: DownloadProgressCallback, attachment: Attachment) {
        val fileName = FileUtils.removeInvalidCharacters(attachment.title)
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(attachment.originUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val downloadId = appDownloader.download(attachment.originUrl)
        appDownloader.observeDownloadProgress(downloadId) { total, current ->
            progressCallback.onProgressChanged(total, current)
        }
    }

    fun setAsAvatar() {
        val uri = currentFeedPost.post.media[0].origin
        val pictureDecodeDisposable = MediaUtils.decodeFromUri(context, uri)
                .map { bitmap -> MediaUtils.adjustBitmapSize(bitmap) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoader() }
                .subscribe({ res: BitmapResizeResult ->
                    val req = UpdateProfileReq()
                    req.setAvatar(res.file, Bitmap.CompressFormat.JPEG)

                    addDisposable("updateAvatar", profileRepository.updateProfile(req)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { hideLoader() }
                            .subscribe({
                                currentFeedPost.profile.avatar = it.avatar
                                _feedPost.value = currentFeedPost
                                sharedStorage.setCurrentProfile(it)
                                ToastUtils.showToastMessage(R.string.album_list_avatar_updated_message)
                            }, {
                                handleError(it)
                            }))
                }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    fun setBusinessAvatar() {
        val uri = currentFeedPost.post.media[0].origin
        val pictureDecodeDisposable = MediaUtils.decodeFromUri(context, uri)
                .map { bitmap -> MediaUtils.adjustBitmapSize(bitmap) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoader() }
                .subscribe({ res: BitmapResizeResult ->
                    val form = BusinessProfileEditing(currentFeedPost.profile.id)
                    form.setImage(res.file, Bitmap.CompressFormat.JPEG)

                    addDisposable("updateBusinessAvatar", profileRepository
                            .updateBusinessProfile(form)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { hideLoader() }
                            .subscribe({
                                currentFeedPost.profile.avatar = it.avatar
                                _feedPost.value = currentFeedPost
                                sharedStorage.setCurrentProfile(it)
                                ToastUtils.showToastMessage(R.string.album_list_avatar_updated_message)
                            }, { handleError(it) }))
                }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    fun searchMentions(query: String?) {
        searchMentionsDisposable = globalSearchRepository
                .findProfiles(query, sort = "-isFollowed,fullName")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _foundMentions.value = it.content
                }, {
                    it.printStackTrace()
                })

    }

    fun cancelMentionsSearching() {
        searchMentionsDisposable?.dispose()
    }

    fun onMentionClicked(profileId: Long) {
        checkIsMyProfile(profileId)
        checkPublicProfile(profileId)
        checkIsPublicBusinessProfile(profileId)
    }

    private fun checkIsMyProfile(profileId: Long) {
        val currentProfile = sharedStorage.getCurrentProfile() ?: return
        if (currentProfile.id != profileId) {
            return
        }
        if (currentProfile.isBusiness) {
            _navigateToMyBusinessProfile.call()
        } else {
            _navigateToMyProfile.call()
        }
    }

    private fun checkPublicProfile(profileId: Long) {
        addDisposable("getPublicProfile", profileRepository.getPublicProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    _navigateToPublicProfile.value = it.id
                })
    }

    private fun checkIsPublicBusinessProfile(profileId: Long) {
        addDisposable("getPublicBusinessProfile", profileRepository.getPublicBusinessProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    _navigateToPublicBusinessProfile.value = it.id
                })
    }

    fun addAudioCounter(mediaId: Int) {
        addDisposable("postMediaView", myPostsRepository.addAudioCounter(mediaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //reloadPosts()
                    refreshPost(null, currentFeedPost.post.id)
                }, {
                    refreshPost(null, currentFeedPost.post.id)
                   // reloadPosts()
                }))
    }

    class ProfileData(
            var profile: PublicProfile,
            var isMine: Boolean
    )
}