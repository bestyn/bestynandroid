package com.gbksoft.neighbourhood.ui.fragments.followed

import com.gbksoft.neighbourhood.data.repositories.FollowersRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.ui.fragments.followers.ProfileListViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FollowedProfilesViewModel(followersRepository: FollowersRepository) : ProfileListViewModel(followersRepository) {

    override fun getProfileListEndpoint(): Observable<Paging<List<ProfileSearchItem>>> {
        return followersRepository.getFollowedProfiles(currentSearchQuery, currentProfileType, paging)
    }

    override fun loadProfilesCount() {
        addDisposable("loadProfilesCount", followersRepository.getFollowedProfiles(null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _profilesCount.value = it.totalCount }, { handleError(it) }))
    }

    override fun onProfileFollowed(profileId: Long) {
        super.onProfileFollowed(profileId)
        increaseProfilesCount()
    }

    override fun onProfileUnfollowed(profileId: Long) {
        super.onProfileUnfollowed(profileId)
        decreaseProfilesCount()
    }
}