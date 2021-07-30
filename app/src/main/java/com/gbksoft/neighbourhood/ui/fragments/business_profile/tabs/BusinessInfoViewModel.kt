package com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider.profileRepository
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.profile_data.Email
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import com.gbksoft.neighbourhood.model.profile_data.Phone
import com.gbksoft.neighbourhood.model.profile_data.WebSite
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class BusinessInfoViewModel(context: Context) : BaseViewModel() {
    private val profileInfoItems: MutableList<PersonalData> = ArrayList()

    private val profileInfoLiveData = MutableLiveData<List<PersonalData>>()
    fun profileInfo() = profileInfoLiveData as LiveData<List<PersonalData>>

    init {
        loadProfile(sharedStorage.requireCurrentProfile().id)
    }

    private fun loadProfile(id: Long) {
        val profileRepository = profileRepository
        addDisposable("loadProfile", profileRepository.getMyBusinessProfile(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }, { onProfileError(it) }))
    }

    private fun onProfileLoaded(profile: BusinessProfile) {
        setInfoItems(profile)
    }

    private fun setInfoItems(profile: BusinessProfile) {
        profileInfoItems.clear()
        addInfoItem(profileInfoItems, profile.address)

        val email = if (profile.email != null) profile.email!! else Email()
        addInfoItem(profileInfoItems, email)

        val webSite = profile.webSite ?: WebSite()
        addInfoItem(profileInfoItems, webSite)

        val phone = if (profile.phone != null) profile.phone!! else Phone()
        addInfoItem(profileInfoItems, phone)

        addInfoItem(profileInfoItems, profile.visibilityRadius)
        profileInfoLiveData.value = profileInfoItems
    }

    private fun addInfoItem(list: MutableList<PersonalData>, item: PersonalData) {
        list.add(item)
    }

    private fun onProfileError(throwable: Throwable) {
        throwable.printStackTrace()
    }
}