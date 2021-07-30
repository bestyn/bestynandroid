package com.gbksoft.neighbourhood.ui.dialogs.switch_profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.ProfileRepository
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.MyProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class SwitchProfileViewModel(val context: Context) : BaseViewModel() {
    private val profileRepository: ProfileRepository = RepositoryProvider.profileRepository

    private val myProfilesLiveData = MutableLiveData<List<MyProfile>>()
    fun getMyProfiles() = myProfilesLiveData as LiveData<List<MyProfile>>

    private val myProfiles: MutableList<MyProfile> = ArrayList()

    init {
        loadData()
    }

    private fun loadData() {
        addDisposable("getCurrentUser", profileRepository.subscribeCurrentUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }) { onProfileError(it) })
    }

    private fun onProfileLoaded(userModel: UserModel) {
        myProfiles.clear()
        myProfiles.addAll(ProfileMapper.toMyProfiles(userModel))
        makeCheckedCurrentProfile(sharedStorage.getCurrentProfile())
        myProfilesLiveData.value = myProfiles
    }

    private fun makeCheckedCurrentProfile(currentProfile: CurrentProfile?) {
        if (currentProfile == null) {
            for (profile in myProfiles) {
                profile.isCurrent = !profile.isBusiness
            }
        } else {
            for (profile in myProfiles) {
                profile.isCurrent = currentProfile.id == profile.id
            }
        }
    }

    private fun onProfileError(t: Throwable) {
        //TODO show error to user
        t.printStackTrace()
    }

    fun onProfileSelected(profile: MyProfile) {
        sharedStorage.getCurrentProfile()?.let {
            if (it.id != profile.id) {
                switchProfile(profile)
            }
        } ?: kotlin.run {
            switchProfile(profile)
        }
    }

    private fun switchProfile(profile: MyProfile) {
        sharedStorage.setCurrentProfile(profile)
        val msg = context.getString(R.string.msg_profile_switched, profile.title)
        ToastUtils.showToastMessage(msg)
    }
}