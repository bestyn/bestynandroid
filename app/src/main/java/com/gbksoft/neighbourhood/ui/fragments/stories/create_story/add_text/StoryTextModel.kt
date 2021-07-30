package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class StoryTextModel(
        val bitmap: Bitmap,
        val rect: Rect,
        val startTime: Int,
        val endTime: Int,
        var imagePath: String = "",
        var x: Int = 0,
        var y: Int = 0) : Parcelable