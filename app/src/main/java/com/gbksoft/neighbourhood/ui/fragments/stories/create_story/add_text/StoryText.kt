package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text

import android.os.Parcelable
import android.view.Gravity
import com.gbksoft.neighbourhood.ui.widgets.stories.add_text.StoryColor
import com.gbksoft.neighbourhood.ui.widgets.stories.add_text.StoryFont
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoryText(
    var text: String,
    var textColor: StoryColor,
    var backgroundColor: StoryColor,
    var cornerRadiusProgress: Int,
    var cornerRadius: Float,
    var font: StoryFont,
    var textAlignment: TextAlignment,

    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var rotation: Float = 0f,
    var startTime: Int = -1,
    var endTime: Int = -1,
    var posX: Float = 0f,
    var posY: Float = 0f) : Parcelable {

    fun getGravity() = when (textAlignment) {
        TextAlignment.LEFT -> Gravity.LEFT or Gravity.CENTER_VERTICAL
        TextAlignment.RIGHT -> Gravity.RIGHT or Gravity.CENTER_VERTICAL
        TextAlignment.CENTER -> Gravity.CENTER
    }
}