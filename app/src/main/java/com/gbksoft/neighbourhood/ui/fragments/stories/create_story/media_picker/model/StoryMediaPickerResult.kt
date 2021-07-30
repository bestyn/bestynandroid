package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model

import android.os.Parcelable
import com.gbksoft.neighbourhood.model.media.Media
import kotlinx.android.parcel.Parcelize

@Parcelize
class StoryMediaPickerResult(val media: List<Media.Picture>) : Parcelable

@Parcelize
class PostVideoMediaPickerResult(val video: Media.Video) : Parcelable