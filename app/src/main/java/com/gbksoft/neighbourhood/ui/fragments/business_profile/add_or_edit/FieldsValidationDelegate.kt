package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.content.Context
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils
import com.google.android.libraries.places.api.model.AddressComponents
import java.io.File

class FieldsValidationDelegate(
    private val context: Context,
    private val validationUtils: ValidationUtils
) {

    fun validateName(errorFieldsModel: ErrorFieldsModel, name: String?) {
        validationUtils.validateFieldOnRequired(ValidationField.BUSINESS_NAME, name)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_NAME, it)
                return
            }

        validationUtils.validateFieldOnStringTooShort(
            ValidationField.BUSINESS_NAME, name, Constants.BUSINESS_NAME_MIN_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_NAME, it)
                return
            }

        validationUtils.validateFieldOnStringTooLong(
            ValidationField.BUSINESS_NAME, name, Constants.BUSINESS_NAME_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_NAME, it)
                return
            }
    }

    fun validateDescription(errorFieldsModel: ErrorFieldsModel, description: String?) {
        validationUtils.validateFieldOnRequired(ValidationField.BUSINESS_DESCRIPTION, description)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_DESCRIPTION, it)
                return
            }

        validationUtils.validateFieldOnStringTooShort(
            ValidationField.BUSINESS_DESCRIPTION, description, Constants.BUSINESS_DESCRIPTION_MIN_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_DESCRIPTION, it)
                return
            }

        validationUtils.validateFieldOnStringTooLong(
            ValidationField.BUSINESS_DESCRIPTION, description, Constants.BUSINESS_DESCRIPTION_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_DESCRIPTION, it)
                return
            }
    }


    fun validateAddress(errorFieldsModel: ErrorFieldsModel,
                        addressPlaceId: String?,
                        addressComponents: AddressComponents?) {
        validationUtils.validateFieldOnRequired(ValidationField.BUSINESS_ADDRESS, addressPlaceId)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_ADDRESS, it)
                return
            }

        validationUtils.validateFieldOnAddressCorrect(ValidationField.BUSINESS_ADDRESS, addressComponents)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_ADDRESS, it)
            }
    }

    fun validateAvatar(errorFieldsModel: ErrorFieldsModel,
                       avatar: File?) {
        validationUtils.validateFieldOnRequired(ValidationField.BUSINESS_AVATAR, avatar)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_AVATAR, it)
            }
    }

    fun validateRadius(errorFieldsModel: ErrorFieldsModel,
                       radius: Int?) {
        validationUtils.validateFieldOnRequired(ValidationField.VISIBILITY_RADIUS, radius)
            ?.let {
                errorFieldsModel.addError(ValidationField.VISIBILITY_RADIUS, it)
            }
    }

    fun validateCategories(errorFieldsModel: ErrorFieldsModel,
                           categories: List<Hashtag>?) {
        validationUtils.validateFieldOnRequired(ValidationField.BUSINESS_CATEGORY, categories)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_CATEGORY, it)
                return
            }

        validationUtils.validateCategoriesTooMany(categories!!, Constants.BUSINESS_CATEGORY_MAX_COUNT)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_CATEGORY, it)
                return
            }
    }

    fun validateEmail(errorFieldsModel: ErrorFieldsModel, email: String) {
        validationUtils.validateFieldOnEmail(ValidationField.BUSINESS_EMAIL, email)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_EMAIL, it)
            }
    }

    fun validatePhone(errorFieldsModel: ErrorFieldsModel, phone: String) {
        validationUtils.validateFieldNotEqualsLength(
            ValidationField.BUSINESS_PHONE, phone, Constants.BUSINESS_PHONE_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_PHONE, it)
            }
    }

    fun validateWebSite(errorFieldsModel: ErrorFieldsModel, webSite: String) {
        validationUtils.validateFieldOnWebSite(ValidationField.BUSINESS_SITE, webSite)
            ?.let {
                errorFieldsModel.addError(ValidationField.BUSINESS_SITE, it)
            }
    }
}