package com.gbksoft.neighbourhood.ui.fragments.stories.list.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.repositories.MyPostsRepository
import com.gbksoft.neighbourhood.data.repositories.PostActionsRepository
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.replace
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.koin.core.component.KoinApiExtension
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlin.math.max

abstract class BaseStoryListViewModel(private val postActionsRepository: PostActionsRepository,
                                      private val myPostsRepository: MyPostsRepository,
                                      private val simpleCache: SimpleCache,
                                      private val postDataRepository: PostDataRepository,
                                      private val context: Context) : BaseViewModel() {

    var currentProfile = sharedStorage.getCurrentProfile()
    protected val storyList = mutableListOf<FeedPost>()

    protected val _stories = MutableLiveData<List<FeedPost>>()
    val stories = _stories as LiveData<List<FeedPost>>

    private val _storyDeletedResult = MutableLiveData<FeedPost>()
    val storyDeletedResult = _storyDeletedResult as LiveData<FeedPost>

    val scrollToTop = SingleLiveEvent<Unit>()

    protected var paging: Paging<List<FeedPost>>? = null
    protected var isLoading = false
    protected val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER

    protected var preCacheVideosJob: Job? = null

    init {
        subscribeToCurrentProfile()

        //Need post loadStories() to init child ViewModels
        Handler(Looper.getMainLooper()).post { loadStories(false) }
    }

    private fun subscribeToCurrentProfile() {
        addDisposable("subscribeToCurrentProfile", sharedStorage.subscribeCurrentProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    currentProfile = it
                    onCurrentProfileLoaded(it)
                }) { it.printStackTrace() })
    }

    protected open fun onCurrentProfileLoaded(currentProfile: CurrentProfile) {}

    protected open fun loadStories(isReload: Boolean) {
        if (isLoading) return
        addDisposable("loadStories", getEndPoint()
                .map { paging ->
                    paging.content.forEach { setIsMine(it) }
                    paging
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoading = true }
                .doOnTerminate { isLoading = false }
                .subscribe({
                    val isFirstLoading = paging == null
                    paging = it
                    onStoriesLoaded(it.content, isFirstLoading, isReload)
                }, { onStoriesError(it) }))
    }

    fun reloadStories() {
        paging = null
        loadStories(true)
    }

    protected fun setIsMine(story: FeedPost) {
        story.isMine = story.profile.id == currentProfile?.id
    }

    abstract fun getEndPoint(): Observable<Paging<List<FeedPost>>>

    protected fun onStoriesLoaded(stories: List<FeedPost>, isFirstLoading: Boolean, isReload: Boolean) {
        preCacheVideosJob = preCacheVideos(stories)
        if (isFirstLoading) storyList.clear()
        storyList.addAll(stories)
        _stories.value = storyList

        if (isReload) {
            scrollToTop.call()
        }
    }

    protected fun preCacheVideos(stories: List<FeedPost>) = CoroutineScope(Dispatchers.IO).launch {
        for (story in stories) {
            if (not { isActive }) return@launch

            val video = story.post.media.first() as? Media.Video ?: continue
            val dataSpec = DataSpec(video.origin)
            val dataSource: DataSource = DefaultDataSourceFactory(
                    context,
                    Util.getUserAgent(context, context.getString(R.string.app_name))).createDataSource()

            try {
                CacheUtil.cache(dataSpec, simpleCache, dataSource, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected fun onStoriesError(t: Throwable) {
        t.printStackTrace()
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    open fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= storyList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > storyList.count()
        if (hasMorePages) {
            loadStories(false)
        }
    }

    fun onReactionClick(position: Int, reaction: Reaction) {
        val story = storyList[position]
        val shouldAddReaction = story.myReaction == Reaction.NO_REACTION
        val endpoint = if (shouldAddReaction) {
            postActionsRepository.addPostReaction(story.post.id, reaction)
        } else {
            postActionsRepository.removePostReaction(story.post.id)
        }

        val tag = if (shouldAddReaction) "addReaction" else "removeReaction"
        addDisposable(tag, endpoint
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (shouldAddReaction) {
                        onAddReactionSuccess(story, reaction)
                    } else {
                        onRemoveReactionSuccess(story, story.myReaction)
                    }
                }, { handleError(it) }))
    }

    private fun onAddReactionSuccess(story: FeedPost, reaction: Reaction) {
        val curReactionCount = story.reactions[reaction] ?: 0
        val foundStory = storyList.find { it.post.id == story.post.id } ?: return
        val newStory = foundStory.copy()
        newStory.apply {
            reactions[reaction] = curReactionCount + 1
            myReaction = reaction
        }

        storyList.replace(foundStory, newStory)
        _stories.value = storyList
    }

    private fun onRemoveReactionSuccess(story: FeedPost, prevReaction: Reaction) {
        val curReactionCount = story.reactions[prevReaction] ?: 0
        val foundStory = storyList.find { it.post.id == story.post.id } ?: return
        val newStory = foundStory.copy()
        newStory.apply {
            reactions[prevReaction] = max(curReactionCount - 1, 0)
            myReaction = Reaction.NO_REACTION
        }
        storyList.replace(foundStory, newStory)
        _stories.value = storyList
    }

    fun onFollowClick(position: Int) {
        val feedPost = storyList[position]
        if (feedPost.iFollow) return

        addDisposable("followStory", postActionsRepository.follow(feedPost.post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onFollowSuccess(feedPost)
                }, {
                    handleFollowError(it) { onFollowSuccess(feedPost) }
                }))
    }

    private fun onFollowSuccess(feedPost: FeedPost) {
        Analytics.onFollowedOtherPost(feedPost.post.id)
        val foundStory = storyList.find { it.post.id == feedPost.post.id } ?: return
        val newStory = foundStory.copy()
        newStory.apply {
            followers++
            iFollow = true
        }
        storyList.replace(foundStory, newStory)
        _stories.value = storyList
    }

    fun onUnfollowClick(position: Int) {
        val feedPost = storyList[position]
        if (!feedPost.iFollow) return

        addDisposable("unfollowStory", postActionsRepository.unfollow(feedPost.post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onUnfollowSuccess(feedPost) }, {
                    handleFollowError(it) { onUnfollowSuccess(feedPost) }
                }))
    }

    private fun onUnfollowSuccess(feedPost: FeedPost) {
        val foundStory = storyList.find { it.post.id == feedPost.post.id } ?: return
        val newStory = foundStory.copy()
        newStory.apply {
            followers--
            iFollow = false
        }
        storyList.replace(foundStory, newStory)
        _stories.value = storyList
    }

    private fun handleFollowError(it: Throwable, onForbiddenErrorRunnable: () -> Unit) {
        if (it is HttpException && it.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            onForbiddenErrorRunnable.invoke()
        } else {
            handleError(it)
        }
    }

    protected fun handleError(t: Throwable) {
        t.printStackTrace()
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    fun getStory(position: Int): FeedPost {
        return storyList[position]
    }

    fun updateStoryCommentsCounter(story: FeedPost) {
        val foundStory = storyList.find { it.post.id == story.post.id } ?: return
        val newStory = foundStory.copy()
        newStory.apply {
            messages = story.messages
        }
        storyList.replace(foundStory, newStory)
        _stories.value = storyList
    }

    fun deleteStory(story: FeedPost) {
        addDisposable("deletePost", myPostsRepository.deletePost(story.post.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onStoryDeleted(story) }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    private fun onStoryDeleted(feedPost: FeedPost) {
        ToastUtils.showToastMessage(context.getString(R.string.story_deleted_msg))
        _storyDeletedResult.value = feedPost
    }

    fun removeStoryFromList(id: Long) {
        val foundStory = storyList.find { it.post.id == id } ?: return

        storyList.remove(foundStory)
        _stories.value = storyList
    }

    fun containsStory(storyId: Long): Boolean {
        return storyList.any { it.post.id == storyId }
    }

    fun isStoryListEmpty() = storyList.isEmpty()

    fun refreshStory(storyId: Long) {
        addDisposable("refreshStory", postDataRepository
                .getPostStory(storyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateStoryInList(it) }, { handleError(it) }))
    }

    private fun updateStoryInList(story: FeedPost) {
        val foundStory = storyList.find { it.post.id == story.post.id } ?: return

        setIsMine(story)
        storyList.replace(foundStory, story)
        _stories.value = storyList
    }

    fun getUnAuthorizedStoryId() = sharedStorage.getUnAuthorizedStoryId()

    fun resetUnAuthorizedStoryId() = sharedStorage.setUnAuthorizedStoryId(-1)

    @KoinApiExtension
    override fun onCleared() {
        preCacheVideosJob?.cancel()
        super.onCleared()
    }
}