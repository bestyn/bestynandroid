package com.gbksoft.neighbourhood.ui.fragments.followers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.FollowersRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class ProfileListViewModel(protected val followersRepository: FollowersRepository) : BaseViewModel() {

    private val followerProfilesList = mutableListOf<ProfileSearchItem>()
    private val followerProfilesLiveData = MutableLiveData<List<ProfileSearchItem>>()
    val followerProfiles: LiveData<List<ProfileSearchItem>> = followerProfilesLiveData

    protected val _profilesCount = MutableLiveData<Int>()
    val profilesCount: LiveData<Int> = _profilesCount

    protected var paging: Paging<List<ProfileSearchItem>>? = null
    protected val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    protected var isLoading = false

    protected var currentSearchQuery: String? = null
    protected var currentProfileType: String? = null

    protected var isLoadingFollowRequest = false

    init {
//        loadFollowerInfo()
    }

    fun loadFollowerInfo(){
        loadFollowerProfiles()
        loadProfilesCount()
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        followerProfilesList.clear()
        loadFollowerProfiles()
    }

    fun setProfileType(type: String?) {
        currentProfileType = type
        followerProfilesList.clear()
        loadFollowerProfiles()
    }

    protected abstract fun getProfileListEndpoint(): Observable<Paging<List<ProfileSearchItem>>>

    private fun loadFollowerProfiles() {
        addDisposable("loadFollowedProfiles",
                getProfileListEndpoint()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            paging = it
                            onFollowerProfilesLoaded(it.content)
                        }, {
                            handleError(it)
                        }))
    }

    private fun onFollowerProfilesLoaded(profiles: List<ProfileSearchItem>) {
        followerProfilesList.clear()
        followerProfilesList.addAll(profiles)
        updateFollowers()
    }

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= followerProfilesList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > followerProfilesList.count()
        if (hasMorePages) {
            loadFollowerProfiles()
        }
    }

    protected abstract fun loadProfilesCount()

    fun followProfile(profileId: Long) {
        if (isLoadingFollowRequest) return
        addDisposable("followProfile", followersRepository.followProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoadingFollowRequest = true }
                .doOnComplete { isLoadingFollowRequest = false }
                .subscribe({ onProfileFollowed(profileId) }, { handleError(it) }))
    }

    protected open fun onProfileFollowed(profileId: Long) {
        followerProfilesList.find { it.id == profileId }?.let {
            it.followType = FollowType.FOLLOWING
            it.isFollowed = true
        }
        updateFollowers()
    }

    fun unfollowProfile(profileId: Long) {
        if (isLoadingFollowRequest) return
        addDisposable("unfollowProfile", followersRepository.unfollowProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoadingFollowRequest = true }
                .doOnComplete { isLoadingFollowRequest = false }
                .subscribe({ onProfileUnfollowed(profileId) }, { handleError(it) }))
    }

    protected open fun onProfileUnfollowed(profileId: Long) {
        followerProfilesList.find { it.id == profileId }?.let {
            it.followType = FollowType.FOLLOW_BACK
            it.isFollowed = false
        }
        updateFollowers()
    }

    fun removeFollower(profileId: Long) {
        if (isLoadingFollowRequest) return
        addDisposable("unfollowProfile", followersRepository.removeFollower(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoadingFollowRequest = true }
                .doOnComplete { isLoadingFollowRequest = false }
                .subscribe({ onFollowerRemoved(profileId) }, { handleError(it) }))
    }

    private fun onFollowerRemoved(profileId: Long) {
        followerProfilesList.removeAll { it.id == profileId }
        updateFollowers()
        decreaseProfilesCount()
    }

    protected fun increaseProfilesCount() {
        _profilesCount.value = profilesCount.value!! +1
    }

    protected fun decreaseProfilesCount() {
        _profilesCount.value = profilesCount.value!! - 1
    }

    private fun updateFollowers() {
        followerProfilesLiveData.value = followerProfilesList.map { it.clone() }
    }

    protected fun handleError(t: Throwable) {
        t.printStackTrace()
//        try {
//            ParseErrorUtils.parseError(t, errorsFuncs)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ToastUtils.showToastMessage(e.message)
//        }
    }

    companion object {
        const val PROFILE_TYPE_BUSINESS = "business"
        const val PROFILE_TYPE_BASIC = "basic"
    }

}