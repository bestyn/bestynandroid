package com.gbksoft.neighbourhood.ui.fragments.stories.list.created

import android.content.Context
import com.gbksoft.neighbourhood.data.repositories.MyPostsRepository
import com.gbksoft.neighbourhood.data.repositories.PostActionsRepository
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.BaseStoryListViewModel
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import io.reactivex.Observable

class CreatedStoryListViewModel(private val myPostsRepository: MyPostsRepository,
                                postActionsRepository: PostActionsRepository,
                                simpleCache: SimpleCache,
                                postDataRepository: PostDataRepository,
                                context: Context
) : BaseStoryListViewModel(postActionsRepository, myPostsRepository, simpleCache, postDataRepository, context) {

    override fun getEndPoint(): Observable<Paging<List<FeedPost>>> {
        return myPostsRepository.loadCreated(paging, listOf(PostType.STORY))
    }
}