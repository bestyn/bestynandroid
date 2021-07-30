package com.gbksoft.neighbourhood.ui.fragments.album

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.forms.BusinessProfileEditing
import com.gbksoft.neighbourhood.data.models.request.user.UpdateProfileReq
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.replace
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostChangedCallback
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.BitmapResizeResult
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlin.math.max

class AlbumListViewModel(val context: Context,
                         private val profile: PublicProfile,
                         titleData: TitleData) : BaseViewModel() {
    private val postActionsRepository = RepositoryProvider.postActionsRepository
    private val myPostsRepository = RepositoryProvider.myPostsRepository
    private val profileRepository = RepositoryProvider.profileRepository
    private val postList = mutableListOf<FeedPost>()
    private val followRequests = mutableSetOf<Long>()

    private var paging: Paging<List<FeedPost>>? = null
    private var isLoading = false

    private val _posts = MutableLiveData<List<FeedPost>>()
    val posts = _posts as LiveData<List<FeedPost>>

    private val _title = MutableLiveData<TitleData>()
    val title = _title as LiveData<TitleData>

    private val _avatarUpdated = SingleLiveEvent<Unit>()
    val avatarUpdated = _avatarUpdated as LiveData<Unit>

    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    val currentProfile = sharedStorage.getCurrentProfile()
    private var isMyAlbum: Boolean = profile.id == sharedStorage.requireCurrentProfile().id

    init {
        _title.value = titleData
        loadAlbum()
    }

    private fun loadAlbum() {
        addDisposable("loadAlbums", RepositoryProvider.postDataRepository
                .loadPosts(paging = paging, profileId = profile.id, postTypes = listOf(PostType.MEDIA))
                .map { paging ->
                    paging.content.forEach {
                        it.isMine = it.profile.id == currentProfile?.id
                    }
                    paging
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { isLoading = false }
                .subscribe({
                    paging = it
                    onAlbumLoaded(it.content)
                }, { onAlbumError(it) }))
    }

    private fun onAlbumLoaded(album: List<FeedPost>) {
        postList.addAll(album)
        _posts.value = album
    }

    private fun onAlbumError(t: Throwable) {
        t.printStackTrace()
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= postList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > postList.count()
        if (hasMorePages) {
            loadAlbum()
        }
    }

    fun onReactionClick(feedPost: FeedPost, reaction: Reaction, reactionCallback: PostListAdapter.ReactionCallback) {
        val shouldAddReaction = feedPost.myReaction == Reaction.NO_REACTION
        val endpoint = if (shouldAddReaction) {
            postActionsRepository.addPostReaction(feedPost.post.id, reaction)
        } else {
            postActionsRepository.removePostReaction(feedPost.post.id)
        }

        addDisposable("postReaction", endpoint
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (shouldAddReaction) {
                        onAddReactionSuccess(feedPost, reaction, reactionCallback)
                    } else {
                        onRemoveReactionSuccess(feedPost, feedPost.myReaction, reactionCallback)
                    }
                }, { handleError(it) }))
    }

    private fun onAddReactionSuccess(feedPost: FeedPost, reaction: Reaction, reactionCallback: PostListAdapter.ReactionCallback) {
        val curReactionCount = feedPost.reactions[reaction] ?: 0
        feedPost.reactions[reaction] = curReactionCount + 1
        feedPost.myReaction = reaction
        reactionCallback.updateReaction(feedPost.myReaction, feedPost.reactions)


    }

    private fun onRemoveReactionSuccess(feedPost: FeedPost, prevReaction: Reaction, reactionCallback: PostListAdapter.ReactionCallback) {
        val curReactionCount = feedPost.reactions[prevReaction] ?: 0
        feedPost.reactions[prevReaction] = max(curReactionCount - 1, 0)
        feedPost.myReaction = Reaction.NO_REACTION
        reactionCallback.updateReaction(feedPost.myReaction, feedPost.reactions)
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

    fun deletePost(feedPost: FeedPost) {
        addDisposable("deletePost", myPostsRepository.deletePost(feedPost.post.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onPostDeleted(feedPost) }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    private fun onPostDeleted(feedPost: FeedPost) {
        ToastUtils.showToastMessage(context.getString(R.string.image_deleted_msg))
        postList.remove(feedPost)
        _posts.value = postList
    }

    fun deletePostLocally(feedPost: FeedPost) {
        postList.remove(feedPost)
        _posts.value = postList
    }

    fun updatePostLocally(feedPost: FeedPost) {
        postList.forEachIndexed { podition, curFeedPost ->
            if (curFeedPost.post.id == feedPost.post.id) {
                postList.replace(curFeedPost, feedPost)
                _posts.value = postList
                return
            }
        }
    }

    fun onFollowClick(feedPost: FeedPost, followCallback: PostListAdapter.FollowCallback) {
        if (followRequests.contains(feedPost.post.id)) return
        followRequests.add(feedPost.post.id)
        val doFollow = !feedPost.iFollow
        val endpoint = if (doFollow) {
            postActionsRepository.follow(feedPost.post)
        } else {
            postActionsRepository.unfollow(feedPost.post)
        }
        addDisposable("followPost", endpoint
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { followRequests.remove(feedPost.post.id) }
                .subscribe({
                    if (doFollow) onFollowSuccess(feedPost, followCallback)
                }, {
                    handleFollowError(it) { onFollowSuccess(feedPost, followCallback) }
                }))
    }

    private fun onFollowSuccess(feedPost: FeedPost, followCallback: PostListAdapter.FollowCallback) {
        Analytics.onFollowedOtherPost(feedPost.post.id)
        feedPost.followers++
        feedPost.iFollow = true
//        followCallback.updateFollowers(feedPost.followers, feedPost.iFollow)

        val oldPost = postList.find { it.post.id == feedPost.post.id }
        oldPost?.let {
            postList.replace(oldPost, feedPost)
            _posts.value = postList
        }
    }

    fun onUnfollowClick(feedPost: FeedPost, postChangedCallback: PostChangedCallback) {
        if (followRequests.contains(feedPost.post.id)) return
        followRequests.add(feedPost.post.id)
        addDisposable("unfollowPost", postActionsRepository.unfollow(feedPost.post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { followRequests.remove(feedPost.post.id) }
                .subscribe({ onUnfollowSuccess(feedPost, postChangedCallback) }, {
                    handleFollowError(it) { onUnfollowSuccess(feedPost, postChangedCallback) }
                }))
    }

    private fun onUnfollowSuccess(feedPost: FeedPost, postChangedCallback: PostChangedCallback) {
        feedPost.followers--
        feedPost.iFollow = false
//        postChangedCallback.onChanged()

        val oldPost = postList.find { it.post.id == feedPost.post.id }
        oldPost?.let {
            postList.replace(oldPost, feedPost)
            _posts.value = postList
        }
    }

    private fun handleFollowError(it: Throwable, onForbiddenErrorRunnable: () -> Unit) {
        if (it is HttpException && it.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            onForbiddenErrorRunnable.invoke()
        } else {
            handleError(it)
        }
    }

    fun setAsAvatar(feedPost: FeedPost) {
        val uri = feedPost.post.media[0].origin
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
                                onAvatarUpdated(it)
                            }, {
                                handleError(it)
                            }))
                }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    private fun onAvatarUpdated(currentProfile: CurrentProfile) {
        if (isMyAlbum) {
            _title.value?.let {
                it.avatarUrl = currentProfile.avatar?.getSmall()
                _title.value = it
            }
        }
        _avatarUpdated.call()
        sharedStorage.setCurrentProfile(currentProfile)
    }

    fun setBusinessAvatar(feedPost: FeedPost) {
        val uri = feedPost.post.media[0].origin
        val pictureDecodeDisposable = MediaUtils.decodeFromUri(context, uri)
                .map { bitmap -> MediaUtils.adjustBitmapSize(bitmap) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoader() }
                .subscribe({ res: BitmapResizeResult ->
                    val form = BusinessProfileEditing(feedPost.profile.id)
                    form.setImage(res.file, Bitmap.CompressFormat.JPEG)

                    addDisposable("updateBusinessAvatar", profileRepository
                            .updateBusinessProfile(form)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { hideLoader() }
                            .subscribe({ onBusinessAvatarUpdated(it) }, { handleError(it) }))
                }) { handleError(it) }
        addDisposable("pictureDecodeDisposable", pictureDecodeDisposable)
    }

    private fun onBusinessAvatarUpdated(businessProfile: BusinessProfile) {
        if (isMyAlbum) {
            _title.value?.let {
                it.avatarUrl = businessProfile.avatar?.getSmall()
                _title.value = it
            }
        }
        _avatarUpdated.call()
        sharedStorage.setCurrentProfile(businessProfile)
    }
}