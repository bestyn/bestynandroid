package com.gbksoft.neighbourhood.data.forms

import android.graphics.Bitmap
import java.io.File

class BusinessProfileCreation(
    val image: File,
    val imageFormat: Bitmap.CompressFormat,
    val name: String,
    val description: String,
    val addressPlaceId: String,
    val radius: Int,
    val hashtagIds: List<Long>
)