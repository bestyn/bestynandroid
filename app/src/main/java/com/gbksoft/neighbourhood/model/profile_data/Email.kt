package com.gbksoft.neighbourhood.model.profile_data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Email(override val value: String) : PersonalData(Type.EMAIL, value), Parcelable {
    constructor() : this(EMPTY_VALUE)
}