package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.databinding.ObservableField
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.post.*
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.EditPostModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils
import java.io.File

class ValidationDelegate(val validationUtils: ValidationUtils, val context: Context) {
    private val res: Resources = context.resources

    fun validate(post: Post, postModel: EditPostModel): ErrorFieldsModel {
        val errorFieldsModel = ErrorFieldsModel()
        val postType = getPostType(post)
        when (post) {
            is GeneralPost,
            is NewsPost -> {
                validateDescription(errorFieldsModel, postType, postModel)
            }
            is CrimePost -> {
                validateDescription(errorFieldsModel, postType, postModel)
            }
            is OfferPost -> {
                validateDescription(errorFieldsModel, postType, postModel)
                validatePrice(errorFieldsModel, postType, postModel)
            }
            is EventPost -> {
                validateName(errorFieldsModel, postType, postModel)
                validateDescription(errorFieldsModel, postType, postModel)
                validateStartDateTime(errorFieldsModel, postType, postModel)
                validateEndDateTime(errorFieldsModel, postType, postModel)
                validateEndDateBeforeStart(errorFieldsModel, postType, postModel)
            }
        }
        return errorFieldsModel
    }

    private fun needValidateAddress(originAddress: String, newAddress: String?): Boolean {
        return if (originAddress.isEmpty()) true
        else originAddress != newAddress
    }

    private fun getPostType(post: Post): String = when (post) {
        is GeneralPost -> res.getString(R.string.post_type_general)
        is NewsPost -> res.getString(R.string.post_type_news)
        is CrimePost -> res.getString(R.string.post_type_crime)
        is OfferPost -> res.getString(R.string.post_type_offer)
        is EventPost -> res.getString(R.string.post_type_event)
        else -> throw Exception("Incorrect post type: $post")
    }

    private fun validateDescription(errorFieldsModel: ErrorFieldsModel, postType: String, postModel: EditPostModel) {
        val validationField = String.format(ValidationField.DESCRIPTION.attrSpecName, postType)
        validationUtils.validateFieldOnRequired(validationField, postModel.description.get())
            ?.let {
                errorFieldsModel.addError(ValidationField.DESCRIPTION, it)
                return
            }

        validationUtils.validateFieldOnStringTooShort(validationField, postModel.description.get(),
            Constants.POST_DESCRIPTION_MIN_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.DESCRIPTION, it)
                return
            }

        validationUtils.validateFieldOnStringTooLong(
            validationField, postModel.description.get(), Constants.POST_DESCRIPTION_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.DESCRIPTION, it)
            }
    }

    private fun validatePrice(errorFieldsModel: ErrorFieldsModel, postType: String, postModel: EditPostModel) {
        validationUtils.validateFieldOnRequired(ValidationField.PRICE, postModel.price.get())
            ?.let {
                errorFieldsModel.addError(ValidationField.PRICE, it)
                return
            }

        validationUtils.validateFieldOnNumber(ValidationField.PRICE, postModel.price.get())
            ?.let {
                errorFieldsModel.addError(ValidationField.PRICE, it)
            }
    }

    private fun validateName(errorFieldsModel: ErrorFieldsModel, postType: String, postModel: EditPostModel) {
        val validationField = String.format(ValidationField.NAME.attrSpecName, postType)
        validationUtils.validateFieldOnRequired(validationField, postModel.name.get())
            ?.let {
                errorFieldsModel.addError(ValidationField.NAME, it)
                return
            }

        validationUtils.validateFieldOnStringTooShort(
            validationField, postModel.name.get(), Constants.POST_NAME_MIN_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.NAME, it)
                return
            }

        validationUtils.validateFieldOnStringTooLong(
            validationField, postModel.name.get(), Constants.POST_NAME_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.NAME, it)
            }
    }

    private fun validateStartDateTime(errorFieldsModel: ErrorFieldsModel, postType: String, postModel: EditPostModel) {
        val startDateTime = postModel.startDateTime.getNonZero() ?: return
        validationUtils.validateFieldOnDate(ValidationField.START_DATE_TIME, startDateTime)
            ?.let {
                errorFieldsModel.addError(ValidationField.START_DATE_TIME, it)
                return
            }
    }

    private fun validateEndDateTime(errorFieldsModel: ErrorFieldsModel, postType: String, postModel: EditPostModel) {
        val endDateTime = postModel.endDateTime.getNonZero() ?: return
        validationUtils.validateFieldOnDate(ValidationField.END_DATE_TIME, endDateTime)
            ?.let {
                errorFieldsModel.addError(ValidationField.END_DATE_TIME, it)
                return
            }
    }

    private fun validateEndDateBeforeStart(errorFieldsModel: ErrorFieldsModel, postType: String, postModel: EditPostModel) {
        val startDateTime = postModel.startDateTime.getNonZero() ?: return
        val endDateTime = postModel.endDateTime.getNonZero() ?: return
        validationUtils.validateFieldLess(ValidationField.START_DATE_TIME,
            ValidationField.END_DATE_TIME, startDateTime, endDateTime)
            ?.let {
                errorFieldsModel.addError(ValidationField.START_DATE_TIME, it)
            }
    }

    fun validateVideoFileSize(errorFieldsModel: ErrorFieldsModel, video: File) {
        validationUtils.validateFileTooBig(video, Constants.VIDEO_FILE_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.POST_VIDEO, it)
            }
    }

    fun validateVideoFileSize(errorFieldsModel: ErrorFieldsModel, uri: Uri) {
        validationUtils.validateFileTooBig(context.contentResolver, uri, Constants.VIDEO_FILE_MAX_LENGTH)
            ?.let {
                errorFieldsModel.addError(ValidationField.POST_VIDEO, it)
            }
    }

    private fun ObservableField<Long?>.getNonZero(): Long? {
        val value = get()
        return if (value != null && value > 0) value else null
    }
}