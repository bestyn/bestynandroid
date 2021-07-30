package com.gbksoft.neighbourhood.model.crop

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CropSize(
    val width: Int,
    val height: Int
) : Parcelable