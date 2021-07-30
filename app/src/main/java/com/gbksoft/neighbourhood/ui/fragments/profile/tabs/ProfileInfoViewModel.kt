package com.gbksoft.neighbourhood.ui.fragments.profile.tabs

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider.profileRepository
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile_data.Birthday
import com.gbksoft.neighbourhood.model.profile_data.Gender
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProfileInfoViewModel(context: Context) : BaseViewModel() {
    private val profileInfoItems: MutableList<PersonalData> = ArrayList()
    private val profileInfoLiveData = MutableLiveData<List<PersonalData>>()
    val profileInfo: LiveData<List<PersonalData>>
        get() = profileInfoLiveData

    private fun loadProfile() {
        val profileRepository = profileRepository
        addDisposable("getCurrentUser", profileRepository.subscribeCurrentUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userModel: UserModel -> onProfileLoaded(userModel) }) { throwable: Throwable -> onProfileError(throwable) })
    }

    private fun onProfileLoaded(userModel: UserModel) {
        setMyProfile(ProfileMapper.toProfile(userModel))
    }

    private fun onProfileError(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun setMyProfile(profile: BasicProfile) {
        profileInfoItems.clear()
        addInfoItem(profileInfoItems, profile.address)
        addInfoItem(profileInfoItems, profile.email)
        val birthday = if (profile.birthday != null) profile.birthday else Birthday()
        addInfoItem(profileInfoItems, birthday!!)
        val gender = if (profile.gender != null) profile.gender else Gender()
        addInfoItem(profileInfoItems, gender!!)
        profileInfoLiveData.value = profileInfoItems
    }

    private fun addInfoItem(list: MutableList<PersonalData>, item: PersonalData) {
        list.add(item)
    }

    init {
        loadProfile()
    }
}