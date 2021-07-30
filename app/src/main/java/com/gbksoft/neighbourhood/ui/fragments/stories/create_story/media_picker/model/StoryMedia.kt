package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoryMedia(
        val id: Long,
        val name: String,
        val dateAdded: Long,
        val mediaType: Int,
        var uri: Uri,
        var path: String,
        val duration: Int,
        val originalFilePath: String,
        var number: Int = -1) : Parcelable {

    fun clone(): StoryMedia {
        return StoryMedia(id, name, dateAdded, mediaType, uri, path, duration, originalFilePath, number)
    }
}