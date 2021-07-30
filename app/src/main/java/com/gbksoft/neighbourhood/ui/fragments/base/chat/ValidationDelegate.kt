package com.gbksoft.neighbourhood.ui.fragments.base.chat

import android.content.Context
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils

class ValidationDelegate(val validationUtils: ValidationUtils, val context: Context) {


    fun validateVideoFileSize(errorFieldsModel: ErrorFieldsModel, videoFileSize: Int) {
        validationUtils.validateFileTooBig(videoFileSize, Constants.VIDEO_FILE_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.ATTACHMENT_FILE, it)
            }
    }

    fun validateFileSize(errorFieldsModel: ErrorFieldsModel, fileSize: Int) {
        validationUtils.validateFileTooBig(fileSize, Constants.ATTACHMENT_FILE_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.ATTACHMENT_FILE, it)
            }
    }

    fun validatePostMessageTextMaxLength(errorFieldsModel: ErrorFieldsModel, text: String?) {
        validationUtils.validateFieldOnStringTooLong(
            ValidationField.POST_MESSAGE,
            text ?: "",
            Constants.POST_MESSAGE_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.POST_MESSAGE, it)
            }
    }

    fun validatePrivateMessageTextMaxLength(errorFieldsModel: ErrorFieldsModel, text: String?) {
        validationUtils.validateFieldOnStringTooLong(
            ValidationField.PRIVATE_MESSAGE,
            text ?: "",
            Constants.PRIVATE_MESSAGE_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.PRIVATE_MESSAGE, it)
            }
    }

}