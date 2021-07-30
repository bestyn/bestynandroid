package com.gbksoft.neighbourhood.ui.fragments.stories.audio.add

import android.content.Context
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils

class AudioValidationDelegate(val validationUtils: ValidationUtils, val context: Context) {

    fun validateDescription(errorFieldsModel: ErrorFieldsModel, description: String?) {
        validationUtils.validateFieldOnRequired(ValidationField.AUDIO_DESCRIPTION, description)?.let {
            errorFieldsModel.addError(ValidationField.AUDIO_DESCRIPTION, it)
        }
        validationUtils.validateFieldOnStringTooLong(
                ValidationField.AUDIO_DESCRIPTION, description, Constants.AUDIO_DESCRIPTION_MAX_LENGTH)
                ?.let {
                    errorFieldsModel.addError(ValidationField.AUDIO_DESCRIPTION, it)
                }
    }
}