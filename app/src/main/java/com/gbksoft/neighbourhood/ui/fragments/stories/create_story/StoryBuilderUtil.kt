package com.gbksoft.neighbourhood.ui.fragments.stories.create_story

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryTextModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StoryBuilderUtil {

    suspend fun trimVideoSegments(videoSegments: List<VideoSegment>): List<Uri>? {
        val trimmedVideoSegments = mutableListOf<Uri>()
        videoSegments.forEachIndexed { i, videoSegment ->
            val res = if (videoSegment.needToCrop) {
                FFmpegUtil.trimVideo(
                        videoSegment.uri,
                        "trimmed_camera_video_${i}_",
                        videoSegment.startTime,
                        videoSegment.endTime)
            } else {
                videoSegment.uri
            }

            if (res != null) {
                trimmedVideoSegments.add(res)
            } else {
                withContext(Dispatchers.Main) {
                    ToastUtils.showToastMessage("Trim Error")
                }
                return null
            }
        }
        return trimmedVideoSegments
    }

    suspend fun resizeVideos(videos: List<Uri>): List<Uri>? {
        val resizedVideoSegments = mutableListOf<Uri>()
        videos.forEach { videoSegmentUri ->
            val res = FFmpegUtil.resize(videoSegmentUri)
            if (res != null) {
                resizedVideoSegments.add(res)
            } else {
                withContext(Dispatchers.Main) {
                    ToastUtils.showToastMessage("Resize Error")
                }
            }
        }
        return resizedVideoSegments
    }

    suspend fun concatenateVideosWithDemuxer(videos: List<Uri>): Uri? {
        val res = FFmpegUtil.concatenate(videos)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Concatenation Error")
            }
        }
        return res
    }

    suspend fun concatenateVideosWithFilter(videos: List<Uri>): Uri? {
        val res = FFmpegUtil.concatVideoFilter(videos)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Concatenation Error")
            }
        }
        return res
    }

    suspend fun trimAudio(context: Context, audio: Uri, startTimeMills: Int): Uri? {
        val duration = getMediaDuration(context, audio)
        val res = FFmpegUtil.trimAudio(audio, startTimeMills, duration)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Trim Audio Error")
            }
        }
        return res
    }

    suspend fun addAudioToVideo(video: Uri, audio: Uri, videoVolume: Float, audioVolume: Float): Uri? {
        val res = FFmpegUtil.addAudioToVideo(video, audio, videoVolume, audioVolume)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Adding Audio Error")
            }
        }
        return res
    }

    suspend fun addSilentAudioToVideo(video: Uri): Uri? {
        val res = FFmpegUtil.addSilentAudio(video)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Adding Audio Error")
            }
        }
        return res
    }

    suspend fun removeAudioInVideo(video: Uri): Uri? {
        val res = FFmpegUtil.removeAudio(video)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Removing Audio Error")
            }
        }
        return res
    }

    suspend fun createVideoFromImage(image: Uri, duration: Int): Uri? {
        val res = FFmpegUtil.createVideoFromImage(image.path!!, duration)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Convert Image Error")
            }
        }
        return res
    }

    suspend fun scaleDuetVideo(video: Uri): Uri? {
        val res = FFmpegUtil.scaleDuetVideo(video)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Scale Error")
            }
        }
        return res
    }

    suspend fun cropDuetVideo(video: Uri): Uri? {
        val res = FFmpegUtil.cropDuetVideo(video)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Scale Error")
            }
        }
        return res
    }

    suspend fun combineDuetVideo(originalVideo: Uri, cameraVideo: Uri): Uri? {
        val res = FFmpegUtil.combineDuetVideo(originalVideo, cameraVideo)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Combine Error")
            }
        }
        return res
    }

    suspend fun addImagesToVideo(video: Uri, storyTextModels: List<StoryTextModel>, screenDimension: Pair<Int, Int>): Uri? {
        val videoDimension = getVideoDimension(video)

        val screenWidth = screenDimension.first
        val screenHeight = screenDimension.second

        val videoWidth = videoDimension.first
        val videoHeight = videoDimension.second

        val screenRatio = screenHeight / screenWidth.toFloat()
        val videoRatio = videoHeight / videoWidth.toFloat()

        var dstScreenWidth = screenWidth
        var dstScreenHeight = screenHeight
        var deltaScreenHeight = 0
        var deltaScreenWidht = 0

        if (screenRatio > videoRatio) {
            dstScreenWidth = (screenHeight / videoRatio).toInt()
            deltaScreenWidht = (dstScreenWidth - screenWidth) / 2
        } else {
            dstScreenHeight = (screenWidth * videoRatio).toInt()
            deltaScreenHeight = (dstScreenHeight - screenHeight) / 2
        }

        storyTextModels.forEach {
            val scale = if (screenRatio > videoRatio) {
                videoHeight / screenHeight.toFloat()
            } else {
                videoWidth / screenWidth.toFloat()
            }

            val scaleMatrix = Matrix().apply { postScale(scale, scale) }
            val scaledBitmap = Bitmap.createBitmap(it.bitmap, 0, 0, it.bitmap.width, it.bitmap.height, scaleMatrix, true)
            val file = MediaUtils.saveBitmapToFile(scaledBitmap)

            val xPosViewToScreenRatio = (it.rect.left + deltaScreenWidht) / dstScreenWidth.toFloat()
            val yPosViewToScreenRatio = (it.rect.top + deltaScreenHeight) / dstScreenHeight.toFloat()

            val x = videoWidth * xPosViewToScreenRatio
            val y = videoHeight * yPosViewToScreenRatio

            it.imagePath = file.path
            it.x = x.toInt()
            it.y = y.toInt()
        }

        val res = FFmpegUtil.addImagesToVideo(video, storyTextModels)
        if (res == null) {
            withContext(Dispatchers.Main) {
                ToastUtils.showToastMessage("Add Text Error")
            }
        }
        return res
    }

    suspend fun addImagesToBitmap(bitmap: Bitmap, storyTextModels: List<StoryTextModel>, screenDimension: Pair<Int, Int>): Bitmap {
        val screenWidth = screenDimension.first
        val screenHeight = screenDimension.second

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val screenRatio = screenHeight / screenWidth.toFloat()
        val bitmapRatio = bitmapHeight / bitmapWidth.toFloat()

        var dstScreenWidth = screenWidth
        var dstScreenHeight = screenHeight
        var deltaScreenHeight = 0
        var deltaScreenWidht = 0

        if (screenRatio > bitmapRatio) {
            dstScreenWidth = (screenHeight / bitmapRatio).toInt()
            deltaScreenWidht = (dstScreenWidth - screenWidth) / 2
        } else {
            dstScreenHeight = (screenWidth * bitmapRatio).toInt()
            deltaScreenHeight = (dstScreenHeight - screenHeight) / 2
        }

        val bitmaps = mutableListOf<Bitmap>()
        val positions = mutableListOf<Pair<Float, Float>>()
        storyTextModels.forEach {
            val scale = if (screenRatio > bitmapRatio) {
                bitmapHeight / screenHeight.toFloat()
            } else {
                bitmapWidth / screenWidth.toFloat()
            }

            val scaleMatrix = Matrix().apply { postScale(scale, scale) }
            val scaledBitmap = Bitmap.createBitmap(it.bitmap, 0, 0, it.bitmap.width, it.bitmap.height, scaleMatrix, true)

            val xPosViewToScreenRatio = (it.rect.left + deltaScreenWidht) / dstScreenWidth.toFloat()
            val yPosViewToScreenRatio = (it.rect.top + deltaScreenHeight) / dstScreenHeight.toFloat()

            val x = bitmapWidth * xPosViewToScreenRatio
            val y = bitmapHeight * yPosViewToScreenRatio

            bitmaps.add(scaledBitmap)
            positions.add(Pair(x, y))
        }

        val resultBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bitmap.config)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        for (i in 0 until bitmaps.size) {
            val curBitmap = bitmaps[i]
            val curPosition = positions[i]
            canvas.drawBitmap(curBitmap, curPosition.first, curPosition.second, null)
        }

        return resultBitmap
    }

    fun combineDuetBitmaps(originalVideoBitmap: Bitmap, cameraVideoBitmap: Bitmap): Bitmap {

        val croppedOriginalVideolBitmap = Bitmap.createBitmap(originalVideoBitmap, 0, originalVideoBitmap.height / 4, originalVideoBitmap.width, originalVideoBitmap.height / 2)
        val croppedCameraVideoBitmap = Bitmap.createBitmap(cameraVideoBitmap, 0, cameraVideoBitmap.height / 4, cameraVideoBitmap.width, cameraVideoBitmap.height / 2)
        val resultBitmap = Bitmap.createBitmap(originalVideoBitmap.width, originalVideoBitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(croppedOriginalVideolBitmap, 0f, 0f, null)
        canvas.drawBitmap(croppedCameraVideoBitmap, 0f, croppedOriginalVideolBitmap.height.toFloat(), null)

        return resultBitmap
    }

    fun checkVideoHasAudioTrack(context: Context, videoUri: Uri): Boolean {
        val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(context, videoUri) }
        val hasAudioStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        val hasAudio = hasAudioStr != null && hasAudioStr == "yes"
        mediaMetadataRetriever.release()
        return hasAudio
    }

    fun getMediaDuration(context: Context, video: Uri): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(context, video)
        }
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toInt().also { mediaMetadataRetriever.release() }
    }

    fun getVideoDimension(video: Uri): Pair<Int, Int> {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(video.path)
        val width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
        val height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        val rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).toInt()

        return if (rotation == 0 || rotation == 180) {
            width to height
        } else {
            height to width
        }
    }

    fun getFrameFromVideo(video: Uri, frameTime: Long): Bitmap {
        val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(video.path) }
        return mediaMetadataRetriever.getFrameAtTime(frameTime * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                .also { mediaMetadataRetriever.release() }

    }
}