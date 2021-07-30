package com.gbksoft.neighbourhood.ui.fragments.stories.list.recommended

import android.content.Context
import com.gbksoft.neighbourhood.data.repositories.MyNeighborhoodRepository
import com.gbksoft.neighbourhood.data.repositories.MyPostsRepository
import com.gbksoft.neighbourhood.data.repositories.PostActionsRepository
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.BaseStoryListViewModel
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import io.reactivex.Observable

class RecommendedStoryListViewModel(private val myNeighborhoodRepository: MyNeighborhoodRepository,
                                    postActionsRepository: PostActionsRepository,
                                    myPostsRepository: MyPostsRepository,
                                    simpleCache: SimpleCache,
                                    postDataRepository: PostDataRepository,
                                    context: Context
) : BaseStoryListViewModel(postActionsRepository, myPostsRepository, simpleCache, postDataRepository, context) {
    private var containsInterest = true

    override fun getEndPoint(): Observable<Paging<List<FeedPost>>> {
        return myNeighborhoodRepository.loadRecommended(paging, listOf(PostType.STORY), false)
    }

    override fun onCurrentProfileLoaded(currentProfile: CurrentProfile) {
        if (isStoryListEmpty() && not { containsInterest } && currentProfile.containsHashtags) {
            reloadStories()
        }
        containsInterest = currentProfile.containsHashtags
    }
}