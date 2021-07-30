package com.gbksoft.neighbourhood.mappers.audio

import com.gbksoft.neighbourhood.data.models.response.audio.AudioModel
import com.gbksoft.neighbourhood.data.models.response.search.AudioSearchResult
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.audio.AudioStories

object AudioMapper {

    fun toAudio(audioModel: AudioModel): Audio {
        return Audio(
                audioModel.id,
                audioModel.url,
                audioModel.description,
                getDurationSrt(audioModel.duration),
                audioModel.isFavorite,
                audioModel.profile?.fullName)
    }

    fun toAudio(audioModel: AudioSearchResult): Audio {
        return Audio(
                audioModel.id.toLong(),
                audioModel.url,
                audioModel.description,
                getDurationSrt(audioModel.duration),
                audioModel.isFavorite,
                audioModel.profile.fullName)
    }

    fun toAudio(audioModel: AudioStories): Audio {
        return Audio(
                audioModel.id!!.toLong(),
                audioModel.url.toString(),
                audioModel.description.toString(),
                getDurationSrt(audioModel.duration!!),
                false,
                audioModel.profileFullName)
    }

    private fun getDurationSrt(durationSeconds: Int): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60

        if (minutes > 0) {
            return "${minutes}m ${seconds}s"
        } else {
            return "${seconds}s"
        }
    }
}