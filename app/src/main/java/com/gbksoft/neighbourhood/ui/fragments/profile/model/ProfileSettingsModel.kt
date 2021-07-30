package com.gbksoft.neighbourhood.ui.fragments.profile.model

import androidx.databinding.ObservableField
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile_data.Gender
import java.io.File

class ProfileSettingsModel(profile: BasicProfile) {
    @JvmField
    val avatarUrl = ObservableField<String?>()

    @JvmField
    val avatar = ObservableField<File>()

    @JvmField
    val fullName = ObservableField<String>()

    @JvmField
    val address = ObservableField<String>()

    @JvmField
    val dateOfBirth = ObservableField<String>()

    @JvmField
    val gender = ObservableField<Gender>()

    @JvmField
    val email = ObservableField<String>()

    @JvmField
    val businessContentShown = ObservableField<Boolean>()

    init {
        setProfile(profile)
    }

    fun setProfile(profile: BasicProfile) {
        val avatarUrl = profile.avatar?.origin ?: null
        this.avatarUrl.set(avatarUrl)
        fullName.set(profile.fullName)
        address.set(profile.address.value)
        profile.birthday?.let { birthday ->
            dateOfBirth.set(birthday.value)
        }
        profile.gender?.let { gender ->
            this.gender.set(gender)
        }
        email.set(profile.email.value)
        businessContentShown.set(profile.isBusinessContentShown)
    }
}