package com.gbksoft.neighbourhood.model.profile_data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Phone(override val value: String) : PersonalData(Type.PHONE, value), Parcelable {
    constructor() : this(EMPTY_VALUE)
}