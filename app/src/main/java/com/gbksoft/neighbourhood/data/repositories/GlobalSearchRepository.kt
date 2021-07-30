package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.models.response.audio.AudioModel
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.models.response.search.ProfileSearchModel
import com.gbksoft.neighbourhood.data.network.api.ApiAudio
import com.gbksoft.neighbourhood.data.network.api.ApiPost
import com.gbksoft.neighbourhood.data.network.api.ApiProfilesSearch
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.data.repositories.utils.RepositoryConstants
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.audio.AudioMapper
import com.gbksoft.neighbourhood.mappers.posts.FeedPostMapper
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class GlobalSearchRepository(
        private val apiProfilesSearch: ApiProfilesSearch,
        private val apiAudio: ApiAudio,
        private val apiPost: ApiPost) : BaseRepository() {

    private val profilesExpand = "avatar.formatted, profile"
    private val sharePrefs = NApplication.sharedStorage
    private val recentSearches = mutableListOf<String>()
    private val recentSearchesSubject = BehaviorSubject.create<List<String>>()
    private val profilesPagingHelper = PagingHelper<ProfileSearchModel, ProfileSearchItem> {
        ProfileMapper.toProfileSearchItem(it)
    }
    private val postsPagingHelper = PagingHelper<FeedPostModel, FeedPost> {
        FeedPostMapper.toFeedPost(it)
    }
    private val audioPagingHelper = PagingHelper<AudioModel, Audio> {
        AudioMapper.toAudio(it)
    }

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        recentSearches.addAll(sharePrefs.getRecentSearches())
        notifyRecentSearchChanged()
    }

    fun findProfiles(query: String? = null,
                     isFollowed: Boolean? = null,
                     isFollower: Boolean? = null,
                     page: Int = 1,
                     sort: String? = null,
                     expand: String = profilesExpand): Observable<Paging<List<ProfileSearchItem>>> {
        return apiProfilesSearch
                .findProfiles(query, isFollowed, isFollower, null, sort, page, Constants.PER_PAGE, expand)
                .map {
                    profilesPagingHelper.getPagingResult(it)
                }
    }

    fun findAudios(query: String,
                   page: Int = 1): Observable<Paging<List<Audio>>> {
        return apiAudio
                .getAudio(
                        searchByDescription = query,
                        expand = profilesExpand,
                        perPage = Constants.PER_PAGE,
                        page = page)
                .map {
                    audioPagingHelper.getPagingResult(it)
                }
    }

    fun findPosts(query: String,
                  page: Int = 1): Observable<Paging<List<FeedPost>>> {
        return apiPost
                .getPosts(
                        search = query,
                        types = RepositoryConstants.postFeedAllTypes,
                        expand = RepositoryConstants.postFeedExpand,
                        page = page,
                        perPage = RepositoryConstants.postFeedPerPage)
                .map { postsPagingHelper.getPagingResult(it) }
    }

    fun subscribeRecentSearches(): Flowable<List<String>> {
        return recentSearchesSubject.toFlowable(BackpressureStrategy.LATEST)
    }

    fun addToRecentSearches(query: String) {
        recentSearches.remove(query)
        recentSearches.add(0, query)
        sharePrefs.saveRecentSearches(recentSearches)
        notifyRecentSearchChanged()
    }

    fun removeFromRecentSearches(query: String) {
        recentSearches.remove(query)
        sharePrefs.saveRecentSearches(recentSearches)
        notifyRecentSearchChanged()
    }

    private fun notifyRecentSearchChanged() {
        Timber.tag("RecentSearches").d("notifyRecentSearchChanged")
        recentSearchesSubject.onNext(ArrayList(recentSearches))
    }


}