package com.gbksoft.neighbourhood.ui.fragments.profile.component

import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils
import com.google.android.libraries.places.api.model.AddressComponents

class FieldsValidationDelegate(private val validationUtils: ValidationUtils) {
    fun validateFullName(errorFieldsModel: ErrorFieldsModel,
                         fullName: String?) {
        val requiredError = validationUtils.validateFieldOnRequired(ValidationField.FULL_NAME, fullName)
        if (requiredError != null) {
            errorFieldsModel.addError(ValidationField.FULL_NAME, requiredError)
            return
        }
        val tooShortError = validationUtils.validateFieldOnStringTooShort(ValidationField.FULL_NAME, fullName, Constants.FULL_NAME_MIN_LENGTH)
        if (tooShortError != null) {
            errorFieldsModel.addError(ValidationField.FULL_NAME, tooShortError)
            return
        }
        val tooLongError = validationUtils.validateFieldOnStringTooLong(ValidationField.FULL_NAME, fullName, Constants.FULL_NAME_MAX_LENGTH)
        if (tooLongError != null) {
            errorFieldsModel.addError(ValidationField.FULL_NAME, tooLongError)
        }
    }

    fun validateAddress(errorFieldsModel: ErrorFieldsModel,
                        placeId: String?,
                        addressComponents: AddressComponents?) {
        val addressRequiredError = validationUtils.validateFieldOnRequired(ValidationField.ADDRESS, placeId)
        if (addressRequiredError != null) {
            errorFieldsModel.addError(ValidationField.ADDRESS, addressRequiredError)
            return
        }
        val addressIncorrectError = validationUtils.validateFieldOnAddressCorrect(ValidationField.ADDRESS, addressComponents)
        if (addressIncorrectError != null) {
            errorFieldsModel.addError(ValidationField.ADDRESS, addressIncorrectError)
        }
    }

    fun validateDateOfBirth(errorFieldsModel: ErrorFieldsModel,
                            dateOfBirthInMillis: Long) {
        val tooBigError = validationUtils.validateDateOfBirth(ValidationField.DATE_OF_BIRTH, dateOfBirthInMillis)
        if (tooBigError != null) {
            errorFieldsModel.addError(ValidationField.DATE_OF_BIRTH, tooBigError)
        }
    }

    fun validateChangeEmail(errorFieldsModel: ErrorFieldsModel, newEmail: String) {
        val requiredError = validationUtils.validateFieldOnRequired(ValidationField.NEW_EMAIL, newEmail)
        if (requiredError != null) {
            errorFieldsModel.addError(ValidationField.NEW_EMAIL, requiredError)
            return
        }
        val regexError = validationUtils.validateFieldOnEmail(ValidationField.NEW_EMAIL, newEmail)
        if (regexError != null) {
            errorFieldsModel.addError(ValidationField.NEW_EMAIL, regexError)
        }
    }

    fun validateChangePassword(errorFieldsModel: ErrorFieldsModel,
                               currentPassword: String,
                               newPassword: String,
                               confirmNewPassword: String) {
        errorFieldsModel.addError(ValidationField.CURRENT_PASSWORD,
            validationUtils.validateFieldOnRequired(
                ValidationField.CURRENT_PASSWORD,
                currentPassword))
        errorFieldsModel.addError(ValidationField.NEW_PASSWORD,
            validationUtils.validateFieldOnRequired(
                ValidationField.NEW_PASSWORD,
                newPassword))
        errorFieldsModel.addError(ValidationField.NEW_PASSWORD,
            validationUtils.validateFieldOnStringTooShort(
                ValidationField.NEW_PASSWORD,
                newPassword,
                Constants.MIN_PASSWORD_LENGTH))
        errorFieldsModel.addError(ValidationField.NEW_PASSWORD,
            validationUtils.validateFieldOnStringTooLong(
                ValidationField.NEW_PASSWORD,
                newPassword,
                Constants.MAX_PASSWORD_LENGTH))
        errorFieldsModel.addError(ValidationField.NEW_PASSWORD,
            validationUtils.validateFieldOnPassword(
                ValidationField.NEW_PASSWORD,
                newPassword))
        errorFieldsModel.addError(ValidationField.NEW_PASSWORD,
            validationUtils.validateSameCurrentAndNewPassword(
                currentPassword,
                newPassword))
        errorFieldsModel.addError(ValidationField.CONFIRM_NEW_PASSWORD,
            validationUtils.validateFieldOnRequired(ValidationField.CONFIRM_NEW_PASSWORD, confirmNewPassword))
        errorFieldsModel.addError(ValidationField.CONFIRM_NEW_PASSWORD,
            validationUtils.validateFieldOnCompareEqual(
                ValidationField.CONFIRM_NEW_PASSWORD,
                ValidationField.NEW_PASSWORD.attrSpecName,
                confirmNewPassword,
                newPassword))
    }

}