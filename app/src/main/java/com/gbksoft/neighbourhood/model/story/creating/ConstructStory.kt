package com.gbksoft.neighbourhood.model.story.creating

import android.net.Uri
import android.os.Parcelable
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.post.StoryPost
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryTextModel
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.text_story.StoryBackground
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConstructStory private constructor(
        //common
        val source: StorySource,
        var videoSegments: List<VideoSegment>? = null,

        //adding text
        var textModels: List<StoryTextModel>? = null,
        var screenDimension: Pair<Int, Int>? = null,
        var background: StoryBackground? = null,
        var duration: StoryTime = StoryTime.SEC_60,

        //adding audio
        var audio: Audio? = null,
        var audioVolume: Float = 0.5f,
        var videoVolume: Float = 0.5f,
        var isAudioEnabled: Boolean = true,

        //creating duet
        var duetOriginalVideoUri: Uri? = null,

        //editing story
        val post: StoryPost? = null) : Parcelable {

    fun getCurrentDuration() = videoSegments?.map { it.endTime - it.startTime }?.sum() ?: 0

    companion object {

        fun fromCamera(videoSegments: List<VideoSegment>): ConstructStory {
            return ConstructStory(
                    source = StorySource.FROM_CAMERA,
                    videoSegments = videoSegments)
        }

        fun fromGallery(videoSegments: List<VideoSegment>): ConstructStory {
            return ConstructStory(
                    source = StorySource.FROM_GALLERY,
                    videoSegments = videoSegments)
        }

        fun fromTextStory(storyBackground: StoryBackground, duration: StoryTime): ConstructStory {
            return ConstructStory(
                    source = StorySource.FROM_TEXT_STORY,
                    background = storyBackground,
                    duration = duration)
        }

        fun fromDuet(duetOriginalVideoUri: Uri, videoSegments: List<VideoSegment>): ConstructStory {
            return ConstructStory(
                    source = StorySource.FROM_DUET,
                    duetOriginalVideoUri = duetOriginalVideoUri,
                    videoSegments = videoSegments)
        }

        fun fromPost(post: StoryPost): ConstructStory {
            return ConstructStory(
                    source = StorySource.FROM_POST,
                    post = post)
        }
    }
}

enum class StorySource {
    FROM_CAMERA, FROM_GALLERY, FROM_TEXT_STORY, FROM_DUET, FROM_POST
}