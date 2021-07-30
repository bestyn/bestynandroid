package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.posts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.repositories.*
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.news.News
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.post_feed.PostFilter
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostFeedSettings
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsManager
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlin.math.max

class MyNeighbourhoodFeedViewModel(private val context: Context,
                                   private val myNeighborhoodRepository: MyNeighborhoodRepository,
                                   private val postActionsRepository: PostActionsRepository,
                                   private val postDataRepository: PostDataRepository,
                                   private val myPostsRepository: MyPostsRepository,
                                   private val profileRepository: ProfileRepository) : BaseViewModel() {
    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    private val newsPaginationBuffer = Constants.NEWS_LIST_PAGINATION_BUFFER

    private val postsManager = PostsManager()
    val posts = postsManager.liveData as LiveData<List<FeedPost>>

    private val _news = MutableLiveData<List<News>>()
    val news = _news as LiveData<List<News>>

    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>

    private val _navigateToMyProfile = SingleLiveEvent<Unit>()
    val navigateToMyProfile = _navigateToMyProfile as LiveData<Unit>

    private val _navigateToMyBusinessProfile = SingleLiveEvent<Unit>()
    val navigateToMyBusinessProfile = _navigateToMyBusinessProfile as LiveData<Unit>

    private val _navigateToPublicProfile = SingleLiveEvent<Long>()
    val navigateToPublicProfile = _navigateToPublicProfile as LiveData<Long>

    private val _navigateToPublicBusinessProfile = SingleLiveEvent<Long>()
    val navigateToPublicBusinessProfile = _navigateToPublicBusinessProfile as LiveData<Long>

    var postFilter: PostFilter? = null
        private set

    private var paging: Paging<List<FeedPost>>? = null
    private var postFeedSettings = PostFeedSettings()
    private var isLoading = false
    private val disposableBag = CompositeDisposable()
    private val followRequests = mutableSetOf<Long>()

    private val newsList = mutableListOf<News>()
    private var isNewsLoading = false
    private var newsPaging: Paging<List<News>>? = null

    private var currentProfileId: Long? = null

    init {
        subscribeCurrentProfileId()
        loadPosts()
    }

    private fun subscribeCurrentProfileId() {
        addDisposable("subscribeCurrentProfileId", sharedStorage.subscribeCurrentProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    _currentProfile.value = it
                    checkNeedNewsReload(it.id)
                })
    }

    private fun checkNeedNewsReload(profileId: Long) {
        if (currentProfileId != profileId) {
            currentProfileId = profileId
            reloadNews()
            reloadPosts()
        }
    }

    fun reloadPosts() {
        onPostFilterChanged(postFilter)
    }

    fun onPostFilterChanged(filter: PostFilter?) {
        this.postFilter = filter
        postFeedSettings.setPostFilter(filter)
        paging = null
        loadPosts()
    }

    fun onNewsVisibleItemChanged(position: Int) {
        if (isNewsLoading) return
        val needLoadMore: Boolean = position + newsPaginationBuffer >= newsList.size
        if (!needLoadMore) return
        val hasMorePages = newsPaging != null && newsPaging!!.totalCount > newsList.size
        if (hasMorePages) {
            loadNews()
        }
    }

    fun reloadNews() {
        newsPaging = null
        loadNews()
    }

    private fun loadNews() {
        isNewsLoading = true
        var page = newsPaging?.currentPage ?: 0
        page++
        addDisposable("loadNews", RepositoryProvider.newsRepository
                .loadNews(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { isNewsLoading = false }
                .subscribe({
                    if (newsPaging == null) onFirstNewsLoaded(it.content)
                    else onNextNewsLoaded(it.content)
                    newsPaging = it
                }, { it.printStackTrace() }))
    }

    private fun onFirstNewsLoaded(data: List<News>) {
        newsList.clear()
        newsList.addAll(data)
        _news.value = newsList
    }

    private fun onNextNewsLoaded(data: List<News>) {
        newsList.addAll(data)
        _news.value = newsList
    }

    private fun loadPosts() {
        isLoading = true
        addDisposable("loadPosts", getEndpoint()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { isLoading = false }
                .map { paging ->
                    paging.content.forEach {
                        it.isMine = it.profile.id == sharedStorage.getCurrentProfile()?.id
                    }
                    paging
                }
                .subscribe({
                    if (paging == null) onFirstPostsLoaded(it.content)
                    else onNextPostsLoaded(it.content)
                    paging = it
                }, { handleError(it) }))
    }

    private fun getEndpoint(): Observable<Paging<List<FeedPost>>> {
        val isBusinessContent = postFeedSettings.isShowBusinessContent()
        val postsTypeList = postFeedSettings.getPostsTypeList()
        return when {
            postFeedSettings.isShowRecommended() -> {
                myNeighborhoodRepository.loadRecommended(paging, postsTypeList, isBusinessContent)
            }
            postFeedSettings.isShowCreated() -> {
                myPostsRepository.loadCreated(paging, postFeedSettings.getPostsTypeList())
            }
            postFeedSettings.isShowFollowed() -> {
                myPostsRepository.loadFollowed(paging, postFeedSettings.getPostsTypeList())
            }
            else -> {
                myNeighborhoodRepository.loadAll(paging, postsTypeList, isBusinessContent)
            }
        }
    }

    private fun onFirstPostsLoaded(data: List<FeedPost>) {
        viewModelScope.launch {
            postsManager.onFirstPostsLoaded(data)
        }
    }

    private fun onNextPostsLoaded(data: List<FeedPost>) {
        viewModelScope.launch {
            postsManager.onNextPostsLoaded(data)
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

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= postsManager.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > postsManager.count()
        if (hasMorePages) {
            loadPosts()
        }
    }

    fun onReactionClick(feedPost: FeedPost, reaction: Reaction) {
        val shouldAddReaction = feedPost.myReaction == Reaction.NO_REACTION
        val endpoint = if (shouldAddReaction) {
            postActionsRepository.addPostReaction(feedPost.post.id, reaction)
        } else {
            postActionsRepository.removePostReaction(feedPost.post.id)
        }

        disposableBag.add(endpoint
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (shouldAddReaction) {
                        onAddReactionSuccess(feedPost, reaction)
                    } else {
                        onRemoveReactionSuccess(feedPost, feedPost.myReaction)
                    }
                }, { handleError(it) }))
    }

    private fun onAddReactionSuccess(feedPost: FeedPost, reaction: Reaction) {
        val curReactionCount = feedPost.reactions[reaction] ?: 0
        val updatedFeedPost = feedPost.copy()
        updatedFeedPost.reactions[reaction] = curReactionCount + 1
        updatedFeedPost.myReaction = reaction
        postsManager.update(updatedFeedPost)
    }

    private fun onRemoveReactionSuccess(feedPost: FeedPost, prevReaction: Reaction) {
        val curReactionCount = feedPost.reactions[prevReaction] ?: 0
        val updatedFeedPost = feedPost.copy()
        updatedFeedPost.reactions[prevReaction] = max(curReactionCount - 1, 0)
        updatedFeedPost.myReaction = Reaction.NO_REACTION
        postsManager.update(updatedFeedPost)
    }

    fun onFollowClick(feedPost: FeedPost) {
        if (followRequests.contains(feedPost.post.id)) return
        followRequests.add(feedPost.post.id)
        val doFollow = !feedPost.iFollow
        val endpoint = if (doFollow) {
            postActionsRepository.follow(feedPost.post)
        } else {
            postActionsRepository.unfollow(feedPost.post)
        }
        disposableBag.add(endpoint
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { followRequests.remove(feedPost.post.id) }
                .subscribe({
                    if (doFollow) onFollowSuccess(feedPost)
                }, {
                    handleFollowError(it) { onFollowSuccess(feedPost) }
                }))
    }

    private fun onFollowSuccess(feedPost: FeedPost) {
        Analytics.onFollowedOtherPost(feedPost.post.id)
        val updatedFeedPost = feedPost.copy()
        updatedFeedPost.followers++
        updatedFeedPost.iFollow = true
        postsManager.update(updatedFeedPost)
    }

    fun onUnfollowClick(feedPost: FeedPost) {
        if (followRequests.contains(feedPost.post.id)) return
        followRequests.add(feedPost.post.id)
        disposableBag.add(postActionsRepository.unfollow(feedPost.post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { followRequests.remove(feedPost.post.id) }
                .subscribe({ onUnfollowSuccess(feedPost) }, {
                    handleFollowError(it) { onUnfollowSuccess(feedPost) }
                }))
    }

    private fun onUnfollowSuccess(feedPost: FeedPost) {
        val updatedFeedPost = feedPost.copy()
        updatedFeedPost.followers--
        updatedFeedPost.iFollow = false
        postsManager.update(updatedFeedPost)
    }

    private fun handleFollowError(it: Throwable, onForbiddenErrorRunnable: () -> Unit) {
        if (it is HttpException && it.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            onForbiddenErrorRunnable.invoke()
        } else {
            handleError(it)
        }
    }

    fun refreshPost(type: PostType, id: Long) {
        postsManager.findById(id)?.let {
            loadPost(type, id)
        }
    }

    private fun loadPost(type: PostType, id: Long) {
        addDisposable("refreshPost", getRefreshEndpoint(type, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateFeedPost(it) }, { handleError(it) }))
    }

    private fun updateFeedPost(feedPost: FeedPost) {
        postsManager.update(feedPost)
    }

    private fun getRefreshEndpoint(postType: PostType, postId: Long): Observable<FeedPost> {
        return when (postType) {
            PostType.GENERAL -> postDataRepository.getPostGeneral(postId)
            PostType.NEWS -> postDataRepository.getPostNews(postId)
            PostType.CRIME -> postDataRepository.getPostCrime(postId)
            PostType.OFFER -> postDataRepository.getPostOffer(postId)
            PostType.EVENT -> postDataRepository.getPostEvent(postId)
            PostType.MEDIA -> postDataRepository.getPostMedia(postId)
            PostType.STORY -> postDataRepository.getPostStory(postId)
        }
    }

    fun removePostFromList(id: Long) {
        postsManager.removeById(id)
    }

    override fun onCleared() {
        disposableBag.clear()
        super.onCleared()
    }

    fun deletePost(feedPost: FeedPost) {
        addDisposable("deletePost", myPostsRepository.deletePost(feedPost.post.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onPostDeleted(feedPost)
                }, {
                    it.printStackTrace()
                    handleError(it)
                }))
    }

    private fun onPostDeleted(feedPost: FeedPost) {
        postsManager.remove(feedPost)
        val message = when (feedPost.type) {
            PostType.EVENT -> context.getString(R.string.event_deleted_msg)
            PostType.MEDIA -> context.getString(R.string.image_deleted_msg)
            else -> context.getString(R.string.post_deleted_msg)
        }
        ToastUtils.showToastMessage(message)
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
        isLoading = true
        addDisposable("postMediaView", myPostsRepository.addAudioCounter(mediaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { isLoading = false }
                .subscribe({
                    reloadPosts()
                }, {
                    reloadPosts()
                }))
    }

}