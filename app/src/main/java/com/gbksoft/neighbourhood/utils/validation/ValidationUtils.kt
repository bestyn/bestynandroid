package com.gbksoft.neighbourhood.utils.validation

import android.content.ContentResolver
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Patterns
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.DateTimeUtils.formatProfileDateOfBirth
import com.gbksoft.neighbourhood.utils.DateTimeUtils.getDateTime
import com.gbksoft.neighbourhood.utils.FileUtils.toHumanReadableFileSize
import com.google.android.libraries.places.api.model.AddressComponents
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.regex.Pattern

class ValidationUtils(private val errorsMessageUtils: ErrorsMessageUtils) {
    fun validateFieldOnRequired(validationField: ValidationField, value: Any?): String? {
        return if (value == null) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.REQUIRED, validationField.attrSpecName)
        } else null
    }

    fun validateFieldOnRequired(validationField: ValidationField, value: String?): String? {
        return validateFieldOnRequired(validationField.attrSpecName, value)
    }

    fun validateFieldOnRequired(validationField: String?, value: String?): String? {
        return if (TextUtils.isEmpty(value)) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.REQUIRED, validationField!!)
        } else null
    }

    fun validateFieldOnRequired(validationField: ValidationField, value: Long?): String? {
        return if (value == null) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.REQUIRED, validationField.attrSpecName)
        } else null
    }

    fun <T> validateFieldOnRequired(validationField: ValidationField, value: List<T>?): String? {
        return validateFieldOnRequired(validationField.attrSpecName, value)
    }

    fun <T> validateFieldOnRequired(validationField: String?, value: List<T>?): String? {
        return if (value == null || value.size == 0) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.REQUIRED, validationField!!)
        } else null
    }

    fun validateFieldOnStringTooShort(validationField: ValidationField, value: String?, count: Int): String? {
        return validateFieldOnStringTooShort(validationField.attrSpecName, value, count)
    }

    fun validateFieldOnStringTooShort(validationField: String?, value: String?, count: Int): String? {
        val fieldValue = value ?: ""
        return if (TextUtils.isEmpty(fieldValue) || fieldValue.trim { it <= ' ' }.length < count) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.STRING_TOO_SHORT, validationField!!, count.toString())
        } else null
    }

    fun validateFieldOnStringTooLong(validationField: ValidationField, value: String?, count: Int): String? {
        return validateFieldOnStringTooLong(validationField.attrSpecName, value, count)
    }

    fun validateFieldOnStringTooLong(validationField: String?, value: String?, count: Int): String? {
        return if (value != null && value.trim { it <= ' ' }.length > count) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.STRING_TOO_LONG, validationField!!, count.toString())
        } else null
    }

    fun validateFieldOnEmail(validationField: ValidationField, value: String): String? {
        return if (!TextUtils.isEmpty(value) && !Patterns.EMAIL_ADDRESS.matcher(value.trim { it <= ' ' }).matches()) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.EMAIL, validationField.attrSpecName)
        } else null
    }

    fun validateFieldOnWebSite(validationField: ValidationField, value: String): String? {
        return if (!TextUtils.isEmpty(value) && !Patterns.WEB_URL.matcher(value.trim { it <= ' ' }).matches()) {
            errorsMessageUtils.getErrorMessageWithCustomString(
                    "{attr} is not a valid Web site.",
                    validationField.attrSpecName)
        } else null
    }

    fun validateFieldNotEqualsLength(validationField: ValidationField, value: String, length: Int): String? {
        return if (TextUtils.isEmpty(value) || value.length != length) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.STRING_NOT_EQUAL, validationField.attrSpecName, length.toString())
        } else null
    }

    fun validateFieldOnPassword(validationField: ValidationField?, value: String?): String? {
        /*Validation password pattern
        if (!TextUtils.isEmpty(value) && !Pattern.compile(Constants.REGEX_PASSWORD).matcher(value).matches()) {
            return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.PASSWORD_FORMAT, validationField.getAttrSpecName());
        }*/
        return null
    }

    fun validateFieldOnCompareEqual(validationField: ValidationField, equalsValueTitle: String?, value1: String, value2: String): String? {
        return if (!TextUtils.isEmpty(value1) && value1 != value2) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.COMPARE_EQUAL, validationField.attrSpecName, equalsValueTitle!!)
        } else null
    }

    fun validateFieldOnCompareEqual(validationField1: ValidationField, validationField2: ValidationField, value1: String, value2: String): String? {
        return if (!TextUtils.isEmpty(value1) && value1 != value2) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.COMPARE_EQUAL, validationField1.attrSpecName, validationField2.attrSpecName)
        } else null
    }

    fun validateFieldOnChecked(validationField: ValidationField, checked: Boolean, value: String?): String? {
        return if (!checked) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.REQUIRED_REQUIRED_VALUE, validationField.attrSpecName, value!!)
        } else null
    }

    fun validateSameCurrentAndNewPassword(currentPassword: String?, newPassword: String?): String? {
        return if (TextUtils.equals(currentPassword, newPassword)) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.SAME_CURRENT_AND_NEW_PASSWORD)
        } else null
    }

    fun validateFieldOnAddressCorrect(validationField: ValidationField, addressComponents: AddressComponents?): String? {
        if (addressComponents == null) {
            return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.ADDRESS_CORRECT, validationField.attrSpecName)
        }

        for (ac in addressComponents.asList())
            for (type in ac.types)
                if (isLocalityType(type)) return null

        return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.ADDRESS_CORRECT, validationField.attrSpecName)
    }

    private fun isLocalityType(type: String): Boolean {
        return when (type) {
            "locality",
            "sublocality",
            "sublocality_level_1",
            "sublocality_level_2",
            "sublocality_level_3",
            "sublocality_level_4" -> true
            else -> false
        }


    }

    fun validateDateOfBirth(validationField: ValidationField, dateInMillis: Long): String? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val todayInMillis = calendar.timeInMillis
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val nextDayInMillis = calendar.timeInMillis
        val today = formatProfileDateOfBirth(todayInMillis)
        return if (dateInMillis >= nextDayInMillis) {
            errorsMessageUtils.getErrorMessageByCode(
                    ErrorCodes.DATE_TOO_BIG,
                    validationField.attrSpecName,
                    today
            )
        } else null
    }

    fun validateFileTooBig(file: File, maxSize: Int): String? {
        if (file.length() >= maxSize) {
            val maxSizeString = toHumanReadableFileSize(maxSize.toLong())
            return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.FILE_TOO_BIG, maxSizeString)
        }
        return null
    }

    fun validateFileTooBig(fileSize: Int, maxSize: Int): String? {
        if (fileSize >= maxSize) {
            val maxSizeString = toHumanReadableFileSize(maxSize.toLong())
            return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.FILE_TOO_BIG, maxSizeString)
        }
        return null
    }

    fun validateFileTooBig(cr: ContentResolver, uri: Uri, maxSize: Int): String? {
        var fileSize: Long? = null
        var fileName: String? = null
        var cursor: Cursor? = null
        try {
            val fd = cr.openFileDescriptor(uri, "r")
            if (fd != null) {
                fileSize = fd.statSize
            }
            cursor = cr.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        if (fileSize == null) return null
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        if (fileSize >= maxSize) {
            val maxSizeString = toHumanReadableFileSize(maxSize.toLong())
            return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.FILE_TOO_BIG, maxSizeString)
        }
        return null
    }

    fun validateFieldOnNumber(validationField: ValidationField, value: String?): String? {
        return if (!TextUtils.isEmpty(value) && !Pattern.compile(Constants.REGEX_NUMBER).matcher(value).matches()) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.NUMBER, validationField.attrSpecName)
        } else null
    }

    fun validateFieldOnDate(validationField: ValidationField, value: Long): String? {
        return if (value <= 0.0) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.DATE, validationField.attrSpecName)
        } else null
    }

    fun validateFieldOnDateTooSmall(validationField: ValidationField, value: Long): String? {
        val dateAndTime = Calendar.getInstance()
        val dateAndTimeWithOffset = dateAndTime.timeInMillis
        if (value > 0.0 && value < dateAndTimeWithOffset) {
            val str = getDateTime(dateAndTimeWithOffset)
            return errorsMessageUtils.getErrorMessageByCode(ErrorCodes.DATE_TOO_SMALL, validationField.attrSpecName, str)
        }
        return null
    }

    fun validateCategoriesTooMany(categories: List<Hashtag?>, count: Int): String? {
        return if (categories.size > count) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.TOO_MANY_CATEGORIES)
        } else null
    }

    fun validateFieldLess(validationField: ValidationField, compareField: ValidationField, value: Long, compareValue: Long): String? {
        return if (value >= compareValue) {
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.COMPARE_LESS_THEN, validationField.attrSpecName, compareField.attrSpecName)
        } else null
    }

    fun validateDurationTooLong(validationField: ValidationField,
                                mediaMetadataRetriever: MediaMetadataRetriever,
                                maxDuration: Int): String? {

        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
        return if (duration > maxDuration) {
            val maxMins = maxDuration / 60 / 1000
            errorsMessageUtils.getErrorMessageByCode(ErrorCodes.OBJECT_DURATION_TOO_LONG, validationField.attrSpecName, "$maxMins min")
        } else {
            null
        }
    }
}