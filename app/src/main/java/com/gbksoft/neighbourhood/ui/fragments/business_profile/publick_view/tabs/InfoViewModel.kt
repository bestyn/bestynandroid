package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.tabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.model.business_profile.PublicBusinessProfile
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import java.util.*


class InfoViewModel : BaseViewModel() {
    private var profile: PublicBusinessProfile? = null

    private val profileInfoLiveData = MutableLiveData<List<PersonalData>>()
    fun profileInfo() = profileInfoLiveData as LiveData<List<PersonalData>>

    private val profileInfoItems: MutableList<PersonalData> = ArrayList()

    fun init(profile: PublicBusinessProfile?) {
        if (profile == null) return
        if (this.profile != null) return

        this.profile = profile

        setInfoItems(profile)
    }

    private fun setInfoItems(profile: PublicBusinessProfile) {
        profileInfoItems.clear()
        addInfoItem(profileInfoItems, profile.address)

        profile.email?.let {
            addInfoItem(profileInfoItems, it)
        }

        profile.webSite?.let {
            addInfoItem(profileInfoItems, it)
        }

        profile.phone?.let {
            addInfoItem(profileInfoItems, it)
        }

        profileInfoLiveData.value = profileInfoItems
    }

    private fun addInfoItem(list: MutableList<PersonalData>, item: PersonalData) {
        list.add(item)
    }

}