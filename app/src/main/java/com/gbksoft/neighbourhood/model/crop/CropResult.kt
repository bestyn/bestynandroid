package com.gbksoft.neighbourhood.model.crop

import android.graphics.Rect
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CropResult(
    val cropPicture: Uri,
    val cropArea: Rect
) : Parcelable