package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableField
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.payment.SubscriptionPlan
import com.gbksoft.neighbourhood.utils.AddressFormatter
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import java.io.File

class AddEditBusinessProfileModel {
    val avatarUrl = ObservableField<String>()
    val avatar = ObservableField<File>()
    val addAvatarVisibility = ObservableField<Int>()
    val avatarVisibility = ObservableField<Int>()
    val name = ObservableField<String>()
    val description = ObservableField<String>()
    val addressPlaceId = ObservableField<String>()
    val address = ObservableField<String>()
    val addressComponents = ObservableField<AddressComponents>()
    val webSite = ObservableField<String>()
    val email = ObservableField<String>()
    val phone = ObservableField<String>()
    val radius = ObservableField<Int>()
    val hashtags = ObservableField<List<Hashtag>>()

    val subscriptionPlan = ObservableField<SubscriptionPlan>()

    private val hashtagList = mutableListOf<Hashtag>()
    private val hashtagSet = mutableSetOf<Hashtag>()

    init {
        addAvatarVisibility.set(View.INVISIBLE)
        avatarVisibility.set(View.GONE)
        hashtags.set(hashtagList)
        radius.set(RADIUS_10)
    }

    fun setNullProfile() {
        addAvatarVisibility.set(View.VISIBLE)
        avatarVisibility.set(View.GONE)
    }

    fun setAvatar(avatar: File) {
        this.avatar.set(avatar)
        avatarVisibility.set(View.VISIBLE)
        addAvatarVisibility.set(View.GONE)
    }

    fun removeAvatar() {
        this.avatarUrl.set(null)
        this.avatar.set(null)
        addAvatarVisibility.set(View.VISIBLE)
        avatarVisibility.set(View.GONE)
    }

    fun setProfile(profile: BusinessProfile) {
        this.avatarUrl.set(profile.avatar?.origin)
        if (TextUtils.isEmpty(profile.avatar?.origin)) {
            addAvatarVisibility.set(View.VISIBLE)
            avatarVisibility.set(View.GONE)
        } else {
            addAvatarVisibility.set(View.GONE)
            avatarVisibility.set(View.VISIBLE)
        }

        this.name.set(profile.name)
        this.description.set(profile.description)
        this.address.set(profile.address.value)
        profile.webSite?.let {
            this.webSite.set(it.value)
        }
        profile.email?.let {
            this.email.set(it.value)
        }
        profile.phone?.let {
            this.phone.set(it.value)
        }
        profile.visibilityRadius.let {
            when (it.radius) {
                0 -> radius.set(RADIUS_ONLY_ME)
                10 -> radius.set(RADIUS_10)
                else -> radius.set(BOUGHT_RADIUS)
            }
        }

        setHashtags(profile.hashtags)
    }

    fun setActiveSubscriptionPlan(subscriptionPlan: SubscriptionPlan) {
        this.subscriptionPlan.set(subscriptionPlan)
    }

    private fun setHashtags(categories: List<Hashtag>) {
        hashtagSet.clear()
        hashtagSet.addAll(categories)
        hashtagList.clear()
        hashtagList.addAll(categories)
        this.hashtags.notifyChange()
    }

    fun addHashtag(hashtag: Hashtag): Boolean {
        val added = hashtagSet.add(hashtag)
        if (added) {
            hashtagList.add(hashtag)
            this.hashtags.notifyChange()
        }
        return added
    }

    fun removeHashtag(hashtag: Hashtag) {
        val removed = hashtagSet.remove(hashtag)
        if (removed) {
            hashtagList.remove(hashtag)
            this.hashtags.notifyChange()
        }
    }

    fun hashtagCount() = hashtagList.size

    fun setAddress(place: Place) {
        this.address.set(AddressFormatter.format(place.addressComponents))
        this.addressComponents.set(place.addressComponents)
        this.addressPlaceId.set(place.id)
    }

    companion object {
        const val RADIUS_ONLY_ME = 1
        const val RADIUS_10 = 2
        const val RADIUS_INCREASE = 3
        const val BOUGHT_RADIUS = 4
    }
}
