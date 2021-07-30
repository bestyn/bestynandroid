package com.gbksoft.neighbourhood.ui.fragments.profile.public_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.models.response.user.ProfileModel
import com.gbksoft.neighbourhood.data.repositories.FollowersRepository
import com.gbksoft.neighbourhood.data.repositories.PostDataRepository
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.profile.PublicBasicProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class PublicProfileViewModel(private val profileId: Long,
                             private val followersRepository: FollowersRepository,
                             private val profileRepository: ProfileRepository,
                             private val postDataRepository: PostDataRepository) : BaseViewModel() {
    private var publicProfile: PublicBasicProfile? = null

    private val profileLiveData = MutableLiveData<Pair<PublicBasicProfile, Boolean>>()
    fun getProfile(): LiveData<Pair<PublicBasicProfile, Boolean>> = profileLiveData

    val followTypeLiveEvent = SingleLiveEvent<FollowType>()

    val removeFollowerLiveEvent = SingleLiveEvent<Boolean>()

    fun loadProfile() {
        if (publicProfile == null) onLoadingStart()

        addDisposable("loadCurrentProfile", Observable.zip<ProfileModel, Paging<List<FeedPost>>, Pair<ProfileModel, Paging<List<FeedPost>>>>(
                profileRepository.getPublicProfile(profileId),
                postDataRepository.loadPosts(paging = null, profileId = profileId, postTypes = listOf(PostType.MEDIA)),
                BiFunction { t1, t2 -> Pair(t1, t2) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEach(Consumer { onLoadingFinish() })
                .subscribe({
                    onProfileLoaded(it.first, it.second.content)
                }, { this.handleError(it) }))
    }

    private fun onProfileLoaded(profileModel: ProfileModel, images: List<FeedPost>) {
        publicProfile = ProfileMapper.toPublicProfile(profileModel)
        profileLiveData.value = Pair(publicProfile!!, images.count() > 0)
    }

    private fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    fun followProfile() {
        addDisposable("followProfile", followersRepository.followProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onProfileFollowed() }, { handleError(it) }))
    }

    private fun onProfileFollowed() {
        publicProfile?.isFollowed = true
        updateFollowType()
    }

    fun unfollowProfile() {
        addDisposable("unfollowProfile", followersRepository.unfollowProfile(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onProfileUnfollowed() }, { handleError(it) }))
    }

    private fun onProfileUnfollowed() {
        publicProfile?.isFollowed = false
        updateFollowType()
    }

    private fun updateFollowType() {
        val isFollowed = publicProfile?.isFollowed ?: return
        val isFollower = publicProfile?.isFollower ?: return
        if (isFollowed) {
            followTypeLiveEvent.value = FollowType.FOLLOWING
        } else {
            if (isFollower) {
                followTypeLiveEvent.value = FollowType.FOLLOW_BACK
            } else {
                followTypeLiveEvent.value = FollowType.FOLLOW
            }
        }
    }

    fun removeFollower(){
        addDisposable("unfollowProfile", followersRepository.removeFollower(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onFollowerRemoved()
                    removeFollowerLiveEvent.value = true
                }, {
                    handleError(it)
                    removeFollowerLiveEvent.value = false
                }))
    }

    private fun onFollowerRemoved() {
        loadProfile()
        updateFollowType()
    }

    private fun onLoadingStart() {
        showLoader()
        changeControlState(R.id.btnSendMessage, false)
    }

    private fun onLoadingFinish() {
        hideLoader()
        changeControlState(R.id.btnSendMessage, true)
    }

}