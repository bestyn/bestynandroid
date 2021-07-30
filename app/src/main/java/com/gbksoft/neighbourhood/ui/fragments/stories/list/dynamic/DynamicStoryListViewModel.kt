package com.gbksoft.neighbourhood.ui.fragments.stories.list.dynamic

import android.content.Context
import com.gbksoft.neighbourhood.data.repositories.MyPostsRepository
import com.gbksoft.neighbourhood.data.repositories.PostActionsRepository
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.data.repositories.StoryRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.BaseStoryListViewModel
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DynamicStoryListViewModel(private val initialStoryId: Long,
                                private val audioId: Long?,
                                private val storyRepository: StoryRepository,
                                private val postDataRepository: PostDataRepository,
                                postActionsRepository: PostActionsRepository,
                                myPostsRepository: MyPostsRepository,
                                simpleCache: SimpleCache,
                                context: Context
) : BaseStoryListViewModel(postActionsRepository, myPostsRepository, simpleCache, postDataRepository, context) {

    private var initalStory: FeedPost? = null
    private val storyListAfter = mutableListOf<FeedPost>()
    private val storyListBefore = mutableListOf<FeedPost>()

    private var pagingAfter: Paging<List<FeedPost>>? = null
    private var pagingBefore: Paging<List<FeedPost>>? = null

    private var isLoadingAfter: Boolean = false
    private var isLoadingBefore: Boolean = false

    override fun getEndPoint(): Observable<Paging<List<FeedPost>>> {
        TODO("STUB") // This method is never called (normally)
    }

    override fun loadStories(isReload: Boolean) {
        if (isLoading) return
        addDisposable("loadInitialStory", postDataRepository.getPostStoryWithAudio(initialStoryId)
                .map { story ->
                    setIsMine(story)
                    story
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoading = true }
                .doOnTerminate { isLoading = false }
                .subscribe({ onInitialStoryLoaded(it) }, { onStoriesError(it) }))
    }

    private fun loadStoriesAfter() {
        if (isLoadingAfter) return
        val page = pagingAfter?.currentPage ?: 0
        addDisposable("loadStoriesAfter", storyRepository.loadAllStories(page = page + 1, audioId = audioId, idAfter = initialStoryId)
                .map { paging ->
                    paging.content.forEach { setIsMine(it) }
                    paging
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoadingAfter = true }
                .doOnTerminate { isLoadingAfter = false }
                .subscribe({
                    pagingAfter = it
                    onStoriesAfterLoaded(it.content)
                }, { onStoriesError(it) }))
    }

    private fun loadStoriesBefore() {
        if (isLoadingBefore) return
        val page = pagingBefore?.currentPage ?: 0
        addDisposable("loadStoriesBefore", storyRepository.loadAllStories(page = page + 1, audioId = audioId, idBefore = initialStoryId)
                .map { paging ->
                    paging.content.forEach { setIsMine(it) }
                    paging
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoadingBefore = true }
                .doOnTerminate { isLoadingBefore = false }
                .subscribe({
                    pagingBefore = it
                    onStoriesBeforeLoaded(it.content)
                }, { onStoriesError(it) }))
    }

    private fun onInitialStoryLoaded(story: FeedPost) {
        initalStory = story
        storyListAfter.add(story)
        loadStoriesBefore()
        loadStoriesAfter()
        updateStoriesList()
    }

    private fun onStoriesAfterLoaded(stories: List<FeedPost>) {
        preCacheVideosJob = preCacheVideos(stories)
        storyListAfter.addAll(stories)
        updateStoriesList()
    }

    private fun onStoriesBeforeLoaded(stories: List<FeedPost>) {
        preCacheVideosJob = preCacheVideos(stories)
        storyListBefore.addAll(stories)
        updateStoriesList()
    }

    private fun updateStoriesList() {
        storyList.clear()
        storyList.addAll(storyListBefore.reversed())
        storyList.addAll(storyListAfter)
        _stories.value = storyList
    }

    override fun onVisibleItemChanged(position: Int) {
        val needLoadAfter = checkStoriesAfter(position)
        if (!needLoadAfter) {
            checkStoriesBefore(position)
        }
    }

    private fun checkStoriesAfter(position: Int): Boolean {
        if (isLoadingAfter) {
            return false
        }

        val needLoadMore = position + paginationBuffer >= storyListAfter.count()
        if (!needLoadMore) return false

        val hasMorePages = pagingAfter != null && pagingAfter!!.totalCount > storyListAfter.count()
        if (hasMorePages) {
            loadStoriesAfter()
            return true
        }
        return false
    }

    private fun checkStoriesBefore(position: Int): Boolean {
        if (isLoadingBefore) {
            return false
        }

        val needLoadMore = position + paginationBuffer >= storyListBefore.count()
        if (!needLoadMore) return false

        val hasMorePages = pagingAfter != null && pagingAfter!!.totalCount > storyListBefore.count()
        if (hasMorePages) {
            loadStoriesBefore()
            return true
        }
        return false
    }
}