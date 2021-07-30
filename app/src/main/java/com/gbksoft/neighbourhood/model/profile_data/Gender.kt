package com.gbksoft.neighbourhood.model.profile_data

import android.os.Parcelable
import androidx.annotation.IntDef
import kotlinx.android.parcel.Parcelize

@Parcelize
class Gender(
    override val value: String, @GenderType
    var genderType: Int?) : PersonalData(Type.GENDER, value), Parcelable {

    constructor() : this(EMPTY_VALUE, null)

    @IntDef(MALE, FEMALE, OTHER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class GenderType

    companion object {
        const val MALE = 0
        const val FEMALE = 1
        const val OTHER = 2
    }
}