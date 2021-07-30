package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.graphics.Bitmap
import com.gbksoft.neighbourhood.data.forms.BusinessProfileCreation
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.payment.SkuType
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.AddressComponents
import java.io.File

class CreationFormBuilder(var model: AddEditBusinessProfileModel, private val validationDelegate: FieldsValidationDelegate) {
    private var avatar: File? = null
    private var name: String? = null
    private var description: String? = null
    private var addressPlaceId: String? = null
    private var addressComponents: AddressComponents? = null
    private var radius: Int? = null
    private var hashtags: List<Hashtag>? = null

    private var boughtRadius: Int? = null

    private var isValid = false

    fun validateData(): ErrorFieldsModel {
        prepare()
        val errorFieldsModel = ErrorFieldsModel()

        validationDelegate.validateName(errorFieldsModel, name)
        validationDelegate.validateDescription(errorFieldsModel, description)
        validationDelegate.validateAddress(errorFieldsModel, addressPlaceId, addressComponents)
        validationDelegate.validateAvatar(errorFieldsModel, avatar)
        validationDelegate.validateCategories(errorFieldsModel, hashtags)
        validationDelegate.validateRadius(errorFieldsModel, radius)

        isValid = errorFieldsModel.isValid
        return errorFieldsModel
    }

    private fun prepare() {
        avatar = model.avatar.get()
        name = model.name.get()
        description = model.description.get()
        addressPlaceId = model.addressPlaceId.get()
        addressComponents = model.addressComponents.get()
        radius = model.radius.get()
        hashtags = model.hashtags.get()

        val skuType = model.subscriptionPlan.get()?.let { SkuType.getSkuType(it.id) }
        boughtRadius = skuType?.radiusValue
    }

    fun build(): BusinessProfileCreation {
        if (isValid) {
            return BusinessProfileCreation(
                avatar!!,
                Bitmap.CompressFormat.JPEG,
                name!!,
                description!!,
                addressPlaceId!!,
                radius!!.toVisibilityRadiusReplacingIncrease(boughtRadius),
                hashtags!!.map { it.id }
            )
        } else {
            throw IllegalStateException("Validate data first")
        }
    }
}

fun Int?.toVisibilityRadius(boughtRadius: Int? = null): Int =
    when (this) {
        AddEditBusinessProfileModel.RADIUS_ONLY_ME -> 0
        AddEditBusinessProfileModel.RADIUS_10 -> 50
        AddEditBusinessProfileModel.RADIUS_INCREASE -> -1
        AddEditBusinessProfileModel.BOUGHT_RADIUS -> boughtRadius ?: -1
        else -> -1
    }

fun Int?.toVisibilityRadiusReplacingIncrease(boughtRadius: Int? = null): Int =
    when (this) {
        AddEditBusinessProfileModel.RADIUS_INCREASE -> {
            if (boughtRadius == null) AddEditBusinessProfileModel.RADIUS_10.toVisibilityRadius()
            else AddEditBusinessProfileModel.BOUGHT_RADIUS.toVisibilityRadius(boughtRadius)
        }
        else -> toVisibilityRadius(boughtRadius)
    }

