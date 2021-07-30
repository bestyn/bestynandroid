package com.gbksoft.neighbourhood.mappers.media

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.models.response.file.MediaModel
import com.gbksoft.neighbourhood.domain.utils.toUriOrNull
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.media.MediaMapper.getAudioDuration
import com.gbksoft.neighbourhood.model.media.Media
import timber.log.Timber

object MediaMapper {

    private var mediaMetadataRetriever: MediaMetadataRetriever? = null

    @JvmStatic
    fun toMediaList(modelList: List<MediaModel>?): List<Media> {
        val mediaList = mutableListOf<Media>()
        if (modelList == null) return mediaList

        for (model in modelList) {
            when (model.type) {
                "video" -> {
                    mediaList.add(toVideo(model))
                }
                "voice" -> {
                    mediaList.add(toVoice(model))
                }
                else -> {
                    mediaList.add(toPicture(model))
                }
            }
        }
        return mediaList
    }

    @JvmStatic
    fun toPictureList(modelList: List<MediaModel>?): List<Media.Picture> {
        val mediaList = mutableListOf<Media.Picture>()
        if (modelList == null) return mediaList

        for (model in modelList) {
            if (model.type?.equals("video") != true) {
                mediaList.add(toPicture(model))
            }
        }
        return mediaList
    }

    fun toPicture(model: MediaModel): Media.Picture {
        val uri = model.url.toUri()
        val preview = model.formatted?.medium.toUriOrNull() ?: uri
        val created = TimestampMapper.toAppTimestamp(model.createdAt)
        return Media.Picture.remote(model.id, preview, uri, created)
    }

    fun toVideo(model: MediaModel): Media.Video {
        val origin = model.url.toUri()
        val preview = model.formatted?.thumbnail.toUriOrNull() ?: origin
        Timber.tag("FormattedTag").d("preview: $preview")
        val created = TimestampMapper.toAppTimestamp(model.createdAt)
        val views = model.counters?.views ?: 0
        return Media.Video.remote(model.id, preview, origin, created, views)
    }

    fun toVoice(model: MediaModel): Media.Audio {
        val origin = model.url.toUri()
        val preview = model.formatted?.thumbnail.toUriOrNull() ?: origin
        Timber.tag("FormattedTag").d("preview: $preview")
        val created = TimestampMapper.toAppTimestamp(model.createdAt)
        val views = model.counters?.views ?: 0
        return Media.Audio.remote(model.id, preview, origin, created, views)
    }

    fun Uri.getAudioDuration(): Int {
        return if (this.toString().startsWith("http")) {
            mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(this@getAudioDuration.toString(), hashMapOf()) }
            mediaMetadataRetriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
                    ?: 0
        } else {
            mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(NApplication.context, this@getAudioDuration) }
            mediaMetadataRetriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
                    ?: 0
        }
    }

}