package com.gbksoft.neighbourhood.model.crop

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CropOptions(
    val source: Uri,
    val aspectRatio: AspectRatio? = null,
    val minCropSize: CropSize? = null
) : Parcelable