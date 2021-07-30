package com.gbksoft.neighbourhood.ui.fragments.profile.component

import android.graphics.Bitmap
import android.text.TextUtils
import com.gbksoft.neighbourhood.data.models.request.user.UpdateProfileReq
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile_data.Gender
import com.gbksoft.neighbourhood.model.profile_data.Gender.GenderType
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.AddressComponents
import timber.log.Timber
import java.io.File

class UpdateProfileForm(private val validationDelegate: FieldsValidationDelegate) {
    private var profile: BasicProfile? = null
    private var avatar: File? = null
    private var addressPlaceId: String? = null
    private var addressComponents: AddressComponents? = null
    private var removeAvatar = false
    private var fullName: String? = null

    @GenderType
    private var genderType: Int? = null
    private var dateOfBirth: String? = null
    private var dateOfBirthInMillis: Long = 0
    fun setProfile(profile: BasicProfile) {
        this.profile = profile
        fullName = profile.fullName
        val birthday = profile.birthday
        dateOfBirth = birthday?.value
        val gender = profile.gender
        genderType = gender?.genderType
        Timber.tag("UpdateFormTag").d("setProfile(), genderType: $genderType")
    }

    fun setAvatar(avatar: File?) {
        this.avatar = avatar
        removeAvatar = false
    }

    fun removeAvatar() {
        removeAvatar = true
        avatar = null
    }

    fun setFullName(fullName: CharSequence) {
        this.fullName = fullName.toString()
        Timber.tag("UpdateFormTag").d("setFullName: $fullName")
    }

    fun setAddressPlace(addressPlaceId: String?, components: AddressComponents?) {
        this.addressPlaceId = addressPlaceId
        addressComponents = components
    }

    fun setGenderType(genderType: Int?) {
        this.genderType = genderType
        Timber.tag("UpdateFormTag").d("setGenderType(), genderType: $genderType")
    }

    fun setDateOfBirth(date: String?, timeInMillis: Long) {
        dateOfBirth = date
        dateOfBirthInMillis = timeInMillis
    }

    fun areThereChanges(): Boolean {
        if (avatar != null) return true
        if (profile!!.avatar != null && removeAvatar) return true
        if (isNotEquals(profile!!.fullName, fullName)) return true
        if (addressPlaceId != null) return true
        val profileBirthday = if (profile!!.birthday == null) null else profile!!.birthday!!.value
        if (isNotEquals(profileBirthday, dateOfBirth)) return true
        val profileGenderType = if (profile!!.gender == null) null else profile!!.gender!!.genderType
        return isNotEquals(profileGenderType, genderType)
    }

    private fun isNotEquals(one: String?, two: String?): Boolean {
        return !TextUtils.equals(one, two)
    }

    private fun isNotEquals(one: Int?, two: Int?): Boolean {
        if (one == null && two == null) return false
        return if (one != null && two != null) one != two else true
    }

    fun validateData(): ErrorFieldsModel {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validateFullName(errorFieldsModel, fullName)
        if (addressPlaceId != null) {
            validationDelegate.validateAddress(errorFieldsModel, addressPlaceId, addressComponents)
        }
        val profileBirthday = if (profile!!.birthday == null) null else profile!!.birthday!!.value
        if (isNotEquals(profileBirthday, dateOfBirth)) {
            validationDelegate.validateDateOfBirth(errorFieldsModel, dateOfBirthInMillis)
        }
        return errorFieldsModel
    }

    fun buildUpdateProfileReq(): UpdateProfileReq {
        val req = UpdateProfileReq()
        req.setFullName(fullName!!)
        if (addressPlaceId != null) {
            req.setPlaceId(addressPlaceId!!)
        }
        val profileBirthday = if (profile!!.birthday == null) null else profile!!.birthday!!.value
        if (isNotEquals(profileBirthday, dateOfBirth)) {
            req.setBirthday(TimestampMapper.toServerTimestamp(dateOfBirthInMillis))
        }
        val profileGenderType = if (profile!!.gender == null) null else profile!!.gender!!.genderType
        if (isNotEquals(profileGenderType, genderType)) {
            req.setGender(convertGenderValue(genderType)!!)
        }
        if (avatar != null) {
            req.setAvatar(avatar, Bitmap.CompressFormat.JPEG)
        } else if (removeAvatar) {
            req.setAvatar(null, Bitmap.CompressFormat.JPEG)
        }
        return req
    }

    private fun convertGenderValue(genderType: Int?): String? {
        if (genderType != null) {
            when (genderType) {
                Gender.MALE -> return "Male"
                Gender.FEMALE -> return "Female"
                Gender.OTHER -> return "Other"
            }
        }
        return null
    }

}