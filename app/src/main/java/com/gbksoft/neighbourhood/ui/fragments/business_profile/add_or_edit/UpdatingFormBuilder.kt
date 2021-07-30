package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.graphics.Bitmap
import com.gbksoft.neighbourhood.data.forms.BusinessProfileEditing
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.payment.SkuType
import com.gbksoft.neighbourhood.utils.PhoneFormatter.getOnlyDigits
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.AddressComponents
import java.io.File

class UpdatingFormBuilder(var model: AddEditBusinessProfileModel, val validationDelegate: FieldsValidationDelegate) {
    private lateinit var profile: BusinessProfile
    private var avatarUrl: String? = null
    private var avatar: File? = null
    private var name: String? = null
    private var description: String? = null
    private var addressPlaceId: String? = null
    private var addressComponents: AddressComponents? = null
    private var radius: Int? = null
    private var hashtags: List<Hashtag>? = null
    private var webSite: String? = null
    private var email: String? = null
    private var phone: String? = null

    private var boughtRadius: Int? = null

    private var isValid = false

    fun setProfile(profile: BusinessProfile) {
        this.profile = profile
    }

    fun validateData(): ErrorFieldsModel {
        prepare()
        val errorFieldsModel = ErrorFieldsModel()

        if (avatarUrl == null) {
            validationDelegate.validateAvatar(errorFieldsModel, avatar)
        }
        if (profile.name != name) {
            validationDelegate.validateName(errorFieldsModel, name)
        }
        if (profile.description != description) {
            validationDelegate.validateDescription(errorFieldsModel, description)
        }
        if (addressPlaceId != null) {
            validationDelegate.validateAddress(errorFieldsModel, addressPlaceId, addressComponents)
        }

        if (profile.visibilityRadius.radius != radius.toVisibilityRadius(boughtRadius)) {
            validationDelegate.validateRadius(errorFieldsModel, radius)
        }
        if (isCategoriesNotEquals(profile.hashtags, model.hashtags.get())) {
            validationDelegate.validateCategories(errorFieldsModel, hashtags)
        }
        webSite?.let {
            validationDelegate.validateWebSite(errorFieldsModel, it)
        }
        email?.let {
            validationDelegate.validateEmail(errorFieldsModel, it)
        }
        phone?.let {
            if (it.isNotEmpty()) validationDelegate.validatePhone(errorFieldsModel, it)
        }

        isValid = errorFieldsModel.isValid
        return errorFieldsModel
    }

    private fun prepare() {
        avatarUrl = model.avatarUrl.get()
        avatar = model.avatar.get()
        name = model.name.get()
        description = model.description.get()
        addressPlaceId = model.addressPlaceId.get()
        addressComponents = model.addressComponents.get()
        radius = model.radius.get()
        hashtags = model.hashtags.get()
        webSite = model.webSite.get()
        email = model.email.get()
        phone = model.phone.get().getOnlyDigits()

        val skuType = model.subscriptionPlan.get()?.let { SkuType.getSkuType(it.id) }
        boughtRadius = skuType?.radiusValue
    }


    private fun isCategoriesNotEquals(first: List<Hashtag>, second: List<Hashtag>?): Boolean {
        return !isCategoriesEquals(first, second)
    }

    private fun isCategoriesEquals(first: List<Hashtag>, second: List<Hashtag>?): Boolean {
        if (second == null) return false
        if (first.size != second.size) return false
        for (i in first.indices) {
            if (first[i].id != second[i].id) return false
        }
        return true
    }

    fun build(): BusinessProfileEditing? {
        if (!isValid) throw IllegalStateException("Validate data first")

        val form = BusinessProfileEditing(profile.id)
        var editing = false

        avatar?.let {
            form.setImage(it, Bitmap.CompressFormat.JPEG)
            editing = true
        }
        if (profile.name != name) {
            form.name = name
            editing = true
        }
        if (profile.description != description) {
            form.description = description
            editing = true
        }
        addressPlaceId?.let {
            form.setAddressPlaceId(it)
            editing = true
        }
        if (profile.visibilityRadius.radius != radius.toVisibilityRadius(boughtRadius)) {
            form.radius = radius.toVisibilityRadiusReplacingIncrease(boughtRadius)
            editing = true
        }
        if (isCategoriesNotEquals(profile.hashtags, model.hashtags.get())) {
            form.hashtagIds = hashtags!!.map { it.id }
            editing = true
        }
        if (profile.webSite?.value != webSite) {
            form.webSite = webSite
            editing = true
        }
        if (profile.email?.value != email) {
            form.email = email
            editing = true
        }
        if (profile.phone?.value != phone) {
            form.phone = phone
            editing = true
        }
        return if (editing) form else null
    }
}