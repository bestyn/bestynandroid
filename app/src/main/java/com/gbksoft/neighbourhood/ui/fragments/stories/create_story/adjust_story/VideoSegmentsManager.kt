package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LongSparseArray
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import timber.log.Timber
import java.util.*
import kotlin.math.ceil

class VideoSegmentsManager {

    private val videoSegments = mutableListOf<VideoSegment>()
    private var videoSegmentBitmaps = LongSparseArray<LongSparseArray<Bitmap>>()
    var calculatingTotalVideoBitmaps = false

    fun setVideoSegments(videoSegments: List<VideoSegment>) {
        this.videoSegments.clear()
        this.videoSegments.addAll(videoSegments)
    }

    fun dragVideoSegment(dragFrom: Int, dragTo: Int) {
        Collections.swap(videoSegments, dragFrom, dragTo)
    }

    fun deleteVideoSegment(position: Int) {
        videoSegments.removeAt(position)
        videoSegmentBitmaps.remove(position.toLong())
    }

    fun setVideoSegmentBounds(videoSegmentPosition: Int, startTimeMills: Int, endTimeMills: Int) {
        videoSegments[videoSegmentPosition].apply {
            startTime = startTimeMills
            endTime = endTimeMills
        }
    }

    fun getVideoSegments(): List<VideoSegment> = videoSegments

    fun getVideoSegmentBitmaps(videoSegmentPos: Int, viewWidth: Int, thumbWidth: Int, thumbHeight: Int): LongSparseArray<Bitmap> {
        val videoPath = videoSegments[videoSegmentPos].uri.path!!
        val bitmaps = videoSegmentBitmaps[videoSegmentPos.toLong()] ?: MediaUtils.fetchVideoBitmaps(videoPath, viewWidth, thumbWidth, thumbHeight)
        videoSegmentBitmaps.put(videoSegmentPos.toLong(), bitmaps)
        return bitmaps
    }

    fun getTotalVideoBitmaps(viewWidth: Int, thumbWidth: Int, thumbHeight: Int): LongSparseArray<Bitmap> {
        val mediaMetadataRetrievers = prepareMediaMetadataRetrievers()
        val thumbnailList = LongSparseArray<Bitmap>()
        val duration = getTotalVideoDuration()
        val numThumbs = ceil(viewWidth.toFloat() / thumbWidth.toDouble()).toInt()
        val interval = duration / numThumbs
        for (i in 0 until numThumbs) {
            if (!calculatingTotalVideoBitmaps) {
                return LongSparseArray()
            }
            val frameTime = i * interval
            val videoSegmentPos = getVideoSegmentPositionByTotalTime(frameTime)
            val videoSegmentFrameTime = getTimeInVideoSegmentByTotalTime(videoSegmentPos, frameTime)

            var bitmap: Bitmap = mediaMetadataRetrievers[videoSegmentPos].getFrameAtTime(videoSegmentFrameTime * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            try {
                bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            thumbnailList.put(i.toLong(), bitmap)
        }
        mediaMetadataRetrievers.forEach { it.release() }

        return thumbnailList
    }

    private fun prepareMediaMetadataRetrievers(): List<MediaMetadataRetriever> {
        val res = mutableListOf<MediaMetadataRetriever>()
        videoSegments.forEach {
            val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(it.uri.path) }
            res.add(mediaMetadataRetriever)
        }
        return res
    }

    fun getVideoSegmentPositionByTotalTime(totalTimeInMs: Int): Int {
        var curSegmentsTime = 0L
        videoSegments.forEachIndexed { position, videoSegment ->
            curSegmentsTime += videoSegment.endTime - videoSegment.startTime
            if (curSegmentsTime >= totalTimeInMs) {
                return position
            }
        }
        return -1
    }

    fun getTimeInVideoSegmentByTotalTime(videoSegmentPos: Int, totalTimeInMs: Int): Int {
        return if (videoSegmentPos > 0) {
            val segmentStartTime = getFirstSegmentsDuration(videoSegmentPos)
            totalTimeInMs - segmentStartTime
        } else {
            totalTimeInMs
        }
    }

    fun getResultVideoSegments(minTime: Int, maxTime: Int): List<VideoSegment> {
        val resultVideoSegments = mutableListOf<VideoSegment>()
        var curTime = 0
        videoSegments.forEachIndexed { pos, videoSegment ->
            val curDuration = videoSegment.endTime - videoSegment.startTime
            if (curTime < minTime && curTime + curDuration >= minTime && curTime + curDuration < maxTime) {
                resultVideoSegments.add(videoSegment.clone().apply { startTime = minTime - curTime })
            } else if (curTime > minTime && curTime < maxTime && curTime + curDuration > maxTime) {
                resultVideoSegments.add(videoSegment.clone().apply { endTime = maxTime - curTime })
            } else if (curTime <= minTime && curTime + curDuration > maxTime) {
                resultVideoSegments.add(videoSegment.clone().apply {
                    startTime = minTime - curTime
                    endTime = maxTime - curTime
                })
            } else if (curTime >= minTime && curTime + curDuration <= maxTime) {
                resultVideoSegments.add(videoSegment)
            }
            curTime += curDuration
        }
        return resultVideoSegments
    }

    private fun getFirstSegmentsDuration(segmentsCount: Int): Int {
        var duration = 0
        for (i in 0 until segmentsCount) {
            duration += videoSegments[i].endTime - videoSegments[i].startTime
        }
        return duration
    }

    fun getTotalVideoDuration() = videoSegments.sumBy { it.endTime - it.startTime }
}