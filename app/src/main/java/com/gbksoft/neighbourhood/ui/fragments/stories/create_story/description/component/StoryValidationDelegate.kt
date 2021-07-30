package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component

import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils
import com.google.android.libraries.places.api.model.AddressComponents

class StoryValidationDelegate(
    private val validationUtils: ValidationUtils
) {
    fun validateDescription(errorFieldsModel: ErrorFieldsModel, description: String?) {
        if (description.isNullOrEmpty()) return
        validationUtils.validateFieldOnStringTooShort(
            ValidationField.STORY_DESCRIPTION, description, Constants.STORY_DESCRIPTION_MIN_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.STORY_DESCRIPTION, it)
                return
            }

        validationUtils.validateFieldOnStringTooLong(
            ValidationField.STORY_DESCRIPTION, description, Constants.STORY_DESCRIPTION_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.STORY_DESCRIPTION, it)
                return
            }
    }

    fun validateAddress(errorFieldsModel: ErrorFieldsModel,
                        addressPlaceId: String?,
                        addressComponents: AddressComponents?) {
        if (addressPlaceId.isNullOrEmpty()) return
        /*validationUtils.validateFieldOnRequired(ValidationField.STORY_ADDRESS, addressPlaceId)
            ?.let {
                errorFieldsModel.addError(ValidationField.STORY_ADDRESS, it)
                return
            }*/

        validationUtils.validateFieldOnAddressCorrect(ValidationField.STORY_ADDRESS, addressComponents)
            ?.let {
                errorFieldsModel.addError(ValidationField.STORY_ADDRESS, it)
            }
    }
}