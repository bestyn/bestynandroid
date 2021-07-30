package com.gbksoft.neighbourhood.model.profile_data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Address(override val value: String,
              val latitude: Double,
              val longitude: Double)
    : PersonalData(Type.ADDRESS, value), Parcelable