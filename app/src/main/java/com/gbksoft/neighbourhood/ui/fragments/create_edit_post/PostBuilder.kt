package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.StorySource
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import kotlinx.coroutines.*
import timber.log.Timber

class PostBuilder(private val context: Context, post: Post, postModel: EditPostModel) {

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

   /* fun build(onVideoProcessedCallback: ((resultVideo: Uri?) -> Unit)) {
        return when (constructStory.source) {
            StorySource.FROM_CAMERA -> buildCameraStory(onVideoProcessedCallback)
            StorySource.FROM_GALLERY -> buildGalleryStory(onVideoProcessedCallback)
            StorySource.FROM_TEXT_STORY -> buildTextStory(onVideoProcessedCallback)
            StorySource.FROM_DUET -> buildDuetStory(onVideoProcessedCallback)
            else -> return
        }
    }*/
/*
    private fun buildCameraStory(onVideoProcessedCallback: ((resultVideo: Uri?) -> Unit)) {
        coroutineScope.launch {
            val videoSegments = constructStory.videoSegments ?: return@launch
            val trimmedVideoSegments = withContext(Dispatchers.Default) {
                StoryBuilderUtil.trimVideoSegments(videoSegments)
            } ?: return@launch

            val videoSegmentsWithAudio = mutableListOf<Uri>()
            trimmedVideoSegments.forEach {
                if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, it)) {
                    val res = withContext(Dispatchers.Default) {
                        StoryBuilderUtil.addSilentAudioToVideo(it)
                    } ?: return@launch
                    videoSegmentsWithAudio.add(res)
                } else {
                    videoSegmentsWithAudio.add(it)
                }
            }

            var resultVideo: Uri
            val concatenatedVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.concatenateVideosWithDemuxer(videoSegmentsWithAudio)
            } ?: return@launch

            resultVideo = withContext(Dispatchers.Default) {
                addTextToStory(concatenatedVideo)
            } ?: return@launch

            if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, resultVideo)) {
                resultVideo = withContext(Dispatchers.Default) {
                    StoryBuilderUtil.addSilentAudioToVideo(resultVideo)
                } ?: return@launch
            }

            resultVideo = withContext(Dispatchers.Default) {
                addAudioToStory(resultVideo)
            } ?: return@launch

            onVideoProcessedCallback(resultVideo)
            Timber.tag("Creating story").d("DONE")
        }
    }

    private fun buildGalleryStory(onVideoProcessedCallback: ((resultVideo: Uri?) -> Unit)) {
        coroutineScope.launch {
            val videoSegments = constructStory.videoSegments ?: return@launch
            val trimmedVideoSegments = withContext(Dispatchers.Default) {
                StoryBuilderUtil.trimVideoSegments(videoSegments)
            } ?: return@launch

            val resizedVideoSegments = withContext(Dispatchers.Default) {
                StoryBuilderUtil.resizeVideos(trimmedVideoSegments)
            } ?: return@launch

            val videoSegmentsWithAudio = mutableListOf<Uri>()
            resizedVideoSegments.forEach {
                if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, it)) {
                    val res = withContext(Dispatchers.Default) {
                        StoryBuilderUtil.addSilentAudioToVideo(it)
                    } ?: return@launch
                    videoSegmentsWithAudio.add(res)
                } else {
                    videoSegmentsWithAudio.add(it)
                }
            }

            var resultVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.concatenateVideosWithFilter(videoSegmentsWithAudio)
            } ?: return@launch

            resultVideo = withContext(Dispatchers.Default) {
                addTextToStory(resultVideo)
            } ?: return@launch

            if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, resultVideo)) {
                resultVideo = withContext(Dispatchers.Default) {
                    StoryBuilderUtil.addSilentAudioToVideo(resultVideo)
                } ?: return@launch
            }

            resultVideo = withContext(Dispatchers.Default) {
                addAudioToStory(resultVideo)
            } ?: return@launch

            onVideoProcessedCallback(resultVideo)
        }
    }

    private fun buildTextStory(onVideoProcessedCallback: ((resultVideo: Uri?) -> Unit)) {
        coroutineScope.launch {
            val bitmap = BitmapFactory.decodeResource(context.resources, constructStory.background?.backgroundResId!!)
            var image = MediaUtils.saveBitmapToFile(bitmap)

            var resultVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.createVideoFromImage(Uri.parse(image.path), constructStory.duration.millis / 1000)
            } ?: return@launch

            resultVideo = withContext(Dispatchers.Default) {
                addTextToStory(resultVideo)
            } ?: return@launch

            if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, resultVideo)) {
                resultVideo = withContext(Dispatchers.Default) {
                    StoryBuilderUtil.addSilentAudioToVideo(resultVideo)
                } ?: return@launch
            }

            resultVideo = withContext(Dispatchers.Default) {
                addAudioToStory(resultVideo)
            } ?: return@launch

            onVideoProcessedCallback(resultVideo)
        }
    }

    private fun buildDuetStory(onVideoProcessedCallback: ((resultVideo: Uri?) -> Unit)) {
        coroutineScope.launch {
            //Camera video processing
            val videoSegments = constructStory.videoSegments ?: return@launch
            val trimmedVideoSegments = withContext(Dispatchers.Default) {
                StoryBuilderUtil.trimVideoSegments(videoSegments)
            } ?: return@launch

            var cameraVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.concatenateVideosWithDemuxer(trimmedVideoSegments)
            } ?: return@launch

            cameraVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.scaleDuetVideo(cameraVideo)
            } ?: return@launch

            if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, cameraVideo)) {
                cameraVideo = withContext(Dispatchers.Default) {
                    StoryBuilderUtil.addSilentAudioToVideo(cameraVideo)
                } ?: return@launch
            }

            //Original video processing
            var originalVideo = constructStory.duetOriginalVideoUri ?: return@launch

            originalVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.scaleDuetVideo(originalVideo)
            } ?: return@launch

            originalVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.cropDuetVideo(originalVideo)
            } ?: return@launch

            if (!StoryBuilderUtil.checkVideoHasAudioTrack(context, originalVideo)) {
                originalVideo = withContext(Dispatchers.Default) {
                    StoryBuilderUtil.addSilentAudioToVideo(originalVideo)
                } ?: return@launch
            }

            val resultVideo = withContext(Dispatchers.Default) {
                StoryBuilderUtil.combineDuetVideo(originalVideo, cameraVideo)
            } ?: return@launch

            onVideoProcessedCallback(resultVideo)
        }
    }

    private suspend fun addAudioToStory(video: Uri): Uri? {
        if (constructStory.audio?.fileUri == null && constructStory.isAudioEnabled) {
            return video
        }
        if (!constructStory.isAudioEnabled) {
            return StoryBuilderUtil.removeAudioInVideo(video)
        }
        if (constructStory.audio != null) {
            var audio = constructStory.audio ?: return null
            val resAudio = if (audio.startTime == 0) {
                audio.fileUri ?: return null
            } else {
                StoryBuilderUtil.trimAudio(context, audio.fileUri!!, audio.startTime) ?: return null
            }
            return StoryBuilderUtil.addAudioToVideo(
                    video,
                    resAudio,
                    constructStory.videoVolume,
                    constructStory.audioVolume)
        }
        return null
    }

    private suspend fun addTextToStory(video: Uri): Uri? {
        if (constructStory.textModels.isNullOrEmpty()) {
            return video
        }
        return StoryBuilderUtil.addImagesToVideo(video, constructStory.textModels!!, constructStory.screenDimension!!)
    }

    suspend fun getBitmapFromTextStory(bitmap: Bitmap, time: Int): Bitmap {
        val textModels = constructStory.textModels?.filter {
            (it.startTime <= time && time <= it.endTime) || (it.startTime == -1 && it.endTime == -1)
        }

        if (textModels.isNullOrEmpty()) {
            return bitmap
        }
        val screenDimension = constructStory.screenDimension ?: return bitmap

        return StoryBuilderUtil.addImagesToBitmap(bitmap, textModels, screenDimension)
    }*/
}