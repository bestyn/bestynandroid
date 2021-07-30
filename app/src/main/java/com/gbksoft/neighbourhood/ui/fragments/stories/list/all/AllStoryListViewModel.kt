package com.gbksoft.neighbourhood.ui.fragments.stories.list.all

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

class AllStoryListViewModel(private val storyRepository: StoryRepository,
                            postActionsRepository: PostActionsRepository,
                            myPostsRepository: MyPostsRepository,
                            simpleCache: SimpleCache,
                            postDataRepository: PostDataRepository,
                            context: Context
) : BaseStoryListViewModel(postActionsRepository, myPostsRepository, simpleCache, postDataRepository, context) {

    override fun getEndPoint(): Observable<Paging<List<FeedPost>>> {
        val page = paging?.currentPage
        return if (page != null) storyRepository.loadAllStories(page + 1) else storyRepository.loadAllStories()
    }
}