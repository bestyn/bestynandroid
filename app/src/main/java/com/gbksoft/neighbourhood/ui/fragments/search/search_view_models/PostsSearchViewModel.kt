package com.gbksoft.neighbourhood.ui.fragments.search.search_view_models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.data.repositories.MyPostsRepository
import com.gbksoft.neighbourhood.data.repositories.PostActionsRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.replace
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostChangedCallback
import com.gbksoft.neighbourhood.ui.fragments.search.search_engine.PostsSearchEngine
import com.gbksoft.neighbourhood.ui.fragments.search.search_engine.SearchEngine
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlin.math.max

class PostsSearchViewModel(
    private val context: Context,
    private val globalSearchRepository: GlobalSearchRepository,
    private val postActionsRepository: PostActionsRepository,
    private val myPostsRepository: MyPostsRepository
) : BaseViewModel() {

    private val posts = mutableListOf<FeedPost>()

    private val _searchResult = MutableLiveData<List<FeedPost>>()
    val searchResult = _searchResult as LiveData<List<FeedPost>>
    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>
    private val _progressBarVisibility = MutableLiveData<Boolean>()
    val progressBarVisibility = _progressBarVisibility as LiveData<Boolean>

    private val followRequests = mutableSetOf<Long>()

    private val searchEngine: SearchEngine = PostsSearchEngine(globalSearchRepository).apply {
        disposableCallback = ::addDisposable
        successCallback = ::onPostsSearchSuccess
        failureCallback = ::onSearchFailure
    }

    init {
        subscribeCurrentProfile()
    }

    private fun subscribeCurrentProfile() {
        addDisposable("loadCurrentProfile", sharedStorage
            .subscribeCurrentProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }) { handleError(it) })
    }

    private fun onProfileLoaded(currentProfile: CurrentProfile) {
        _currentProfile.value = currentProfile
    }

    fun findPosts(query: String) {
        _progressBarVisibility.value = true
        searchEngine.search(query)
    }

    fun onVisibleItemChanged(position: Int) {
        searchEngine.onVisibleItemChanged(position)
    }

    fun cancelSearch() {
        _progressBarVisibility.value = false
        searchEngine.cancel()
        posts.clear()
        _searchResult.value = posts
    }

    private fun onPostsSearchSuccess(data: List<FeedPost>) {
        _progressBarVisibility.value = false
        val currentProfileId = sharedStorage.requireCurrentProfile().id
        data.forEach { it.isMine = it.profile.id == currentProfileId }
        posts.clear()
        posts.addAll(data)
        _searchResult.value = posts
    }

    private fun onSearchFailure(t: Throwable) {
        _progressBarVisibility.value = false
        handleError(t)
    }

    fun onReactionClick(feedPost: FeedPost, reaction: Reaction, reactionCallback: PostListAdapter.ReactionCallback) {
        val shouldAddReaction = feedPost.myReaction == Reaction.NO_REACTION
        val endpoint = if (shouldAddReaction) {
            postActionsRepository.addPostReaction(feedPost.post.id, reaction)
        } else {
            postActionsRepository.removePostReaction(feedPost.post.id)
        }

        addDisposable("ReactionClick", endpoint
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
        refreshPostsSearchResult()
    }

    private fun onRemoveReactionSuccess(feedPost: FeedPost, prevReaction: Reaction, reactionCallback: PostListAdapter.ReactionCallback) {
        val curReactionCount = feedPost.reactions[prevReaction] ?: 0
        feedPost.reactions[prevReaction] = max(curReactionCount - 1, 0)
        feedPost.myReaction = Reaction.NO_REACTION
        reactionCallback.updateReaction(feedPost.myReaction, feedPost.reactions)
        refreshPostsSearchResult()
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
        addDisposable("FollowClick", endpoint
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
        followCallback.updateFollowers(feedPost.followers, feedPost.iFollow)
        refreshPostsSearchResult()
    }

    fun onUnfollowClick(feedPost: FeedPost, postChangedCallback: PostChangedCallback) {
        if (followRequests.contains(feedPost.post.id)) return
        followRequests.add(feedPost.post.id)
        addDisposable("UnfollowClick", postActionsRepository.unfollow(feedPost.post)
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
        postChangedCallback.onChanged()
        refreshPostsSearchResult()
    }

    private fun handleFollowError(it: Throwable, onForbiddenErrorRunnable: () -> Unit) {
        if (it is HttpException && it.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            onForbiddenErrorRunnable.invoke()
        } else {
            handleError(it)
        }
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
            .subscribe({ onPostDeletedSuccess(feedPost) }, {
                it.printStackTrace()
                handleError(it)
            }))
    }

    private fun onPostDeletedSuccess(feedPost: FeedPost) {
        removeFromPostsSearchResult(feedPost)
        if (feedPost.type == PostType.EVENT) {
            ToastUtils.showToastMessage(context.getString(R.string.event_deleted_msg))
        } else {
            ToastUtils.showToastMessage(context.getString(R.string.post_deleted_msg))
        }
    }

    fun postDeleted(feedPost: FeedPost) {
        removeFromPostsSearchResult(feedPost)
    }

    private fun removeFromPostsSearchResult(feedPost: FeedPost) {
        posts.find { it.post.id == feedPost.post.id }?.let {
            posts.remove(it)
            refreshPostsSearchResult()
        }
    }

    fun postChanged(feedPost: FeedPost) {
        replaceInPostsSearchResult(feedPost)
    }

    private fun refreshPostsSearchResult() {
        _searchResult.value = posts
    }

    private fun replaceInPostsSearchResult(feedPost: FeedPost) {
        posts.find { it.post.id == feedPost.post.id }?.let {
            posts.replace(it, feedPost)
            refreshPostsSearchResult()
        }
    }
}