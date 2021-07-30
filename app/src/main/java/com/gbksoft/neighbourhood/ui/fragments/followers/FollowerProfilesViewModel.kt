package com.gbksoft.neighbourhood.ui.fragments.followers

import com.gbksoft.neighbourhood.data.repositories.FollowersRepository
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FollowerProfilesViewModel(followersRepository: FollowersRepository)
    : ProfileListViewModel(followersRepository) {

    override fun getProfileListEndpoint(): Observable<Paging<List<ProfileSearchItem>>> {
        return followersRepository.getFollowerProfiles(currentSearchQuery, currentProfileType, paging)
    }

    override fun loadProfilesCount() {
        addDisposable("loadProfilesCount", followersRepository.getFollowerProfiles(null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _profilesCount.value = it.totalCount }, { handleError(it) }))
    }
}