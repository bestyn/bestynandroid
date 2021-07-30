package com.gbksoft.neighbourhood.utils.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.LongSparseArray
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ceil

object MediaUtils {
    private const val photoPrefix = "Photo_"
    private const val videoPrefix = "Video_"
    private const val photoExtension = ".jpg"
    private const val videoExtension = ".mp4"

    @Throws(IOException::class)
    fun createTempPhotoFile(context: Context): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(photoPrefix, photoExtension, storageDir)
    }

    @Throws(IOException::class)
    fun createTempVideoFile(context: Context): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(videoPrefix, videoExtension, storageDir)
    }

    fun generatePhotoFileName(): String {
        return generateFileName(photoPrefix) + photoExtension
    }

    fun generateVideoFileName(): String {
        return generateFileName(videoPrefix) + videoExtension
    }

    fun generateFileName(prefix: String): String {
        val time = DateTimeUtils.timeForTempFile
        return prefix + time
    }

    fun decodeFromFile(context: Context, file: File): Observable<Bitmap> {
        val decoder = AtomicReference<PictureDecoder>()
        return Observable.create { emitter: ObservableEmitter<Bitmap> ->
            decoder.set(PictureDecoder(context))
            try {
                val pic = decoder.get().load(file)
                if (pic != null) {
                    emitter.onNext(pic)
                    emitter.onComplete()
                }
            } catch (t: Throwable) {
                emitter.onError(t)
                emitter.onComplete()
            }
        }.doOnDispose { decoder.get().cancel() }
    }

    fun decodeFromUri(context: Context, uri: Uri): Observable<Bitmap> {
        val decoder = AtomicReference<PictureDecoder>()
        return Observable.create { emitter: ObservableEmitter<Bitmap> ->
            decoder.set(PictureDecoder(context))
            try {
                val pic = decoder.get().load(uri)
                if (pic != null) {
                    emitter.onNext(pic)
                    emitter.onComplete()
                }
            } catch (t: Throwable) {
                emitter.onError(t)
                emitter.onComplete()
            }
        }.doOnDispose { decoder.get().cancel() }
    }

    @Throws(IOException::class)
    fun adjustBitmapSize(bitmap: Bitmap): BitmapResizeResult {
        val quality = 100
        var fos: FileOutputStream? = null
        try {
            val tempFile = File.createTempFile("tmp", "jpg")
            fos = FileOutputStream(tempFile)
            for (i in 0..99) {
                fos.channel.truncate(0)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality - i, fos)
                Timber.w("adjustBitmapSize: %d", tempFile.length())
                if (tempFile.length() <= Constants.PICTURE_FILE_MAX_LENGTH) {
                    return BitmapResizeResult(tempFile, quality - i, tempFile.length())
                }
            }
            throw IllegalArgumentException("Picture size too big")
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap): File {
        val root = File(NApplication.context.filesDir, "story")
        if (!root.exists()) {
            root.mkdirs()
        }

        val resPath = "${root.path}/${generateFileName("with_text")}.png"
        val resFile = File(resPath)
        val fos = FileOutputStream(resFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

        fos.flush()
        fos.close()

        return resFile
    }

    fun downloadAndSaveToFile(url: String, filePath: String) {
        URL(url).openStream().use { input ->
            FileOutputStream(File(filePath)).use { output ->
                input.copyTo(output)
            }
        }

    }


    fun fetchVideoBitmaps(filePath: String, viewWidth: Int, thumbWidth: Int, thumbHeight: Int): LongSparseArray<Bitmap> {
        val thumbnailList = LongSparseArray<Bitmap>()
        val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(filePath) }
        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()

        val numThumbs = ceil(viewWidth.toFloat() / thumbWidth.toDouble()).toInt()
        val interval = duration / numThumbs

        for (i in 0 until numThumbs) {
            val frameTime = i * interval * 1000
            var bitmap: Bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            try {
                bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            thumbnailList.put(i.toLong(), bitmap)
        }
        mediaMetadataRetriever.release()
        return thumbnailList
    }

    fun fetchBitmapsFromSingleBitmap(bitmap: Bitmap, viewWidth: Int, thumbWidth: Int, thumbHeight: Int): LongSparseArray<Bitmap> {
        val thumbnailList = LongSparseArray<Bitmap>()
        val numThumbs = ceil(viewWidth.toFloat() / thumbWidth.toDouble()).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false)
        for (i in 0 until numThumbs) {
            thumbnailList.put(i.toLong(), scaledBitmap)
        }
        return thumbnailList
    }

    fun getTotalProgress(videoSegments: List<VideoSegment>, curWindow: Int, curProgress: Long): Long {
        var totalProgress = 0L
        for (i in 0..curWindow) {
            if (i == curWindow) {
                totalProgress += curProgress - videoSegments[curWindow].startTime.toLong()
            } else {
                totalProgress += videoSegments[i].endTime.toLong() - videoSegments[i].startTime.toLong()
            }
        }
        return totalProgress

    }
}