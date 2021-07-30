package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.response.search.ProfileSearchModel
import com.gbksoft.neighbourhood.data.network.api.ApiFollowers
import com.gbksoft.neighbourhood.data.network.api.ApiProfilesSearch
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Completable
import io.reactivex.Observable

class FollowersRepository(private val apiFollowers: ApiFollowers, private val apiProfilesSearch: ApiProfilesSearch) : BaseRepository() {

    private val profilesExpand = "avatar.formatted,profile,isFollower,isFollowed"
    private val profilesSort = "fullName"
    private val profilesPagingHelper = PagingHelper<ProfileSearchModel, ProfileSearchItem> {
        ProfileMapper.toProfileSearchItem(it)
    }

    fun getFollowerProfiles(query: String?, type: String?, paging: Paging<List<ProfileSearchItem>>?): Observable<Paging<List<ProfileSearchItem>>> {
        paging?.let {
            return getFollowerProfiles(query, type, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return getFollowerProfiles(query, type, 1, Constants.PER_PAGE)
        }
    }

    private fun getFollowerProfiles(query: String?, type: String?, page: Int, perPage: Int): Observable<Paging<List<ProfileSearchItem>>> {
        return apiProfilesSearch.findProfiles(query, null, true, type, profilesSort, page, perPage, profilesExpand)
                .map { profilesPagingHelper.getPagingResult(it) }
    }

    fun getFollowedProfiles(query: String?, type: String?, paging: Paging<List<ProfileSearchItem>>?): Observable<Paging<List<ProfileSearchItem>>> {
        paging?.let {
            return getFollowedProfiles(query, type, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return getFollowedProfiles(query, type, 1, Constants.PER_PAGE)
        }
    }

    private fun getFollowedProfiles(query: String?, type: String?, page: Int, perPage: Int): Observable<Paging<List<ProfileSearchItem>>> {
        return apiProfilesSearch.findProfiles(query, true, null, type, profilesSort, page, perPage, profilesExpand)
                .map { profilesPagingHelper.getPagingResult(it) }
    }

    fun followProfile(profileId: Long): Completable {
        return apiFollowers.followProfile(profileId)
    }

    fun unfollowProfile(profileId: Long): Completable {
        return apiFollowers.unFollowProfile(profileId)
    }

    fun removeFollower(profileId: Long): Completable {
        return apiFollowers.removeFollower(profileId)
    }
}