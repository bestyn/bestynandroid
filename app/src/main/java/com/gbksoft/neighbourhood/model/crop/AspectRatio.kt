package com.gbksoft.neighbourhood.model.crop

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AspectRatio(
    val aspectRatioX: Int,
    val aspectRatioY: Int
) : Parcelable