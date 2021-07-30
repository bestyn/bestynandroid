package com.gbksoft.neighbourhood.model.media

import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.app.NApplication.Companion.context
import kotlinx.android.parcel.Parcelize
import java.io.File

sealed class Media(
        open val id: Long,
        open val preview: Uri,
        open val origin: Uri,
        open val created: Long,
        open var views: Int = 0,
        open val type: String = "",
        var length: Int = 0
) : Parcelable {

    fun isLocal() = id < 0

    val metaRetriever = MediaMetadataRetriever()

    @Parcelize
    data class Picture(
            override val id: Long,
            override var preview: Uri,
            override var origin: Uri,
            override var created: Long,
            override var views: Int = 0,
            override var type: String = "image"
    ) : Media(id, preview, origin, created, views) {
        var previewArea: Rect? = null

        companion object {
            fun local(uri: Uri) = Picture(-System.currentTimeMillis(), uri, uri, System.currentTimeMillis())
            fun local(file: File): Picture {
                val uri = file.toUri()
                return Picture(-System.currentTimeMillis(), uri, uri, System.currentTimeMillis())
            }

            fun remote(id: Long, previewUri: Uri, originUri: Uri, created: Long) =
                    Picture(id, previewUri, originUri, created)
        }
    }

    @Parcelize
    data class Video(
            override val id: Long,
            override var preview: Uri,
            override var origin: Uri,
            override var created: Long,
            override var views: Int = 0,
            override var type: String = "video",
            var width: Int = 0,
            var height: Int = 0,
            var proportion: Double = 0.0
    ) : Media(id, preview, origin, created, views) {

        private suspend fun initProportion() {
            if (origin.toString().startsWith("http")) {
                metaRetriever.setDataSource(origin.toString(), mapOf())
            } else {
                metaRetriever.setDataSource(context, origin)
            }
            height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()

            proportion = NApplication.screenWidth / width.toDouble()
        }

        companion object {
            fun local(uri: Uri) = Video(-System.currentTimeMillis(), uri, uri, System.currentTimeMillis())
            fun local(file: File): Video {
                val uri = file.toUri()
                return Video(-System.currentTimeMillis(), uri, uri, System.currentTimeMillis())
            }

            fun remote(id: Long, previewUri: Uri, originUri: Uri, created: Long, views: Int) =
                    Video(id, previewUri, originUri, created, views)
        }
    }

    @Parcelize
    data class Audio(
            override val id: Long,
            override var preview: Uri,
            override var origin: Uri,
            override var created: Long,
            override var views: Int = 0,
            override var type: String = "voice"
    ) : Media(id, preview, origin, created, views) {


        companion object {
            fun local(uri: Uri) = Audio(-System.currentTimeMillis(), uri, uri, System.currentTimeMillis())
            fun local(file: File): Audio {
                val uri = file.toUri()
                return Audio(-System.currentTimeMillis(), uri, uri, System.currentTimeMillis())
            }

            fun remote(id: Long, previewUri: Uri, originUri: Uri, created: Long, views: Int) =
                    Audio(id, previewUri, originUri, created, views)
        }
    }

}
