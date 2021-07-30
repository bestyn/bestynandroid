package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.forms.StoryCreationForm
import com.gbksoft.neighbourhood.data.forms.StoryEditingForm
import com.gbksoft.neighbourhood.data.models.request.story.CreateStoryReq
import com.gbksoft.neighbourhood.data.models.request.story.UpdateStoryReq
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.network.api.ApiPost
import com.gbksoft.neighbourhood.data.network.api.ApiPostStory
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.data.repositories.utils.RepositoryConstants
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.posts.FeedPostMapper
import com.gbksoft.neighbourhood.model.post.FeedPost
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class StoryRepository(
        private val apiPostStory: ApiPostStory,
        private val apiPost: ApiPost
) : BaseRepository() {
    private val expand = "media,profile,counters,myReaction,iFollow,audio"

    private val postsPagingHelper = PagingHelper<FeedPostModel, FeedPost> {
        FeedPostMapper.toFeedPost(it)
    }

    fun loadAllStories(page: Int = 1,
                       idAfter: Long? = null,
                       idBefore: Long? = null,
                       audioId: Long? = null): Observable<Paging<List<FeedPost>>> {
        return apiPost
                .getPosts(
                        idAfter = idAfter,
                        idBefore = idBefore,
                        audioId = audioId,
                        types = RepositoryConstants.postFeedStory,
                        expand = RepositoryConstants.postFeedExpand,
                        page = page,
                        perPage = RepositoryConstants.postFeedPerPage)
                .map {
                    postsPagingHelper.getPagingResult(it)
                }
    }

    fun createStory(form: StoryCreationForm): Completable {
        val createStoryReq = CreateStoryReq()
        createStoryReq.setVideoFile(form.video)
        val posterTimestamp = TimestampMapper.toServerTimestamp(form.posterTimestamp)
        createStoryReq.setPosterTimestamp(posterTimestamp)
        form.description?.let {
            createStoryReq.setDescription(it)
        }
        form.addressPlaceId?.let {
            createStoryReq.setLocation(it)
        }
        form.audioId?.let {
            createStoryReq.setAudioId(it)
        }
        createStoryReq.setAllowedComment(form.isAllowedComment)
        createStoryReq.setAllowedDuet(form.isAllowedDuet)
        return apiPostStory
                .createStory(createStoryReq, expand)
                .ignoreElement()
    }

    fun updateStory(form: StoryEditingForm): Single<FeedPost> {
        val updateStoryReq = UpdateStoryReq()
        form.posterTimestamp?.let {
            val posterTimestamp = TimestampMapper.toServerTimestamp(it)
            updateStoryReq.setPosterTimestamp(posterTimestamp)
        }
        form.description?.let {
            updateStoryReq.setDescription(it)
        }
        form.addressPlaceId?.let {
            updateStoryReq.setLocation(it)
        }
        form.isAllowedComment?.let {
            updateStoryReq.setAllowedComment(it)
        }
        form.isAllowedDuet?.let {
            updateStoryReq.setAllowedDuet(it)
        }
        return apiPostStory
                .updateStory(form.storyId, updateStoryReq, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }
}