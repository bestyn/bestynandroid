package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.FFmpegUtil
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.choose_folder.ChooseFolderBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMedia
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media.MediaPickerTab
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class ImageVideoPickerManager(private val context: Context) {

    private val allMediaList = mutableListOf<StoryMedia>()
    val selectedMedia = mutableListOf<StoryMedia>()
    private val folders = mutableSetOf<String>()

    var currentTab: MediaPickerTab = MediaPickerTab.ALL
        set(value) {
            field = value
            updateCurrentMediaList()
        }
    var currentFolderPath: String? = null
        set(value) {
            field = if (value == ChooseFolderBottomSheet.ALL_PHOTOS) null else value
            updateCurrentMediaList()
        }

    private val mediaSubject = PublishSubject.create<List<StoryMedia>>()
    private val selectedMediaSubject = PublishSubject.create<List<StoryMedia>>()
    private val mediaFoldersSubject = PublishSubject.create<List<String>>()
    private val preparedSelectedMediaSubject = PublishSubject.create<MutableList<VideoSegment>>()

    fun subscribeToMedia(): Observable<List<StoryMedia>> {
        return mediaSubject
    }

    fun subscribeSelectedMedia(): Observable<List<StoryMedia>> {
        return selectedMediaSubject
    }

    fun subscribeToMediaFolders(): Observable<List<String>> {
        return mediaFoldersSubject
    }

    fun subscribeToPreparedSelectedMedia(): Observable<MutableList<VideoSegment>> {
        return preparedSelectedMediaSubject
    }

    fun loadMedia() {
        val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DURATION)

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )

        if (cursor != null) {
            val mediaList = parseCursor(cursor)
            allMediaList.addAll(mediaList)

            mediaList.forEach {
                val path = it.path.substring(0, it.path.lastIndexOf('/'))
                folders.add(path)
            }

            updateCurrentMediaList()
            mediaFoldersSubject.onNext(folders.toList())
        }
    }

    fun selectMedia(storyMedia: StoryMedia) {
        if (selectedMedia.find { it.id == storyMedia.id } != null) {
            return
        }
        selectedMedia.add(storyMedia)
        allMediaList.forEach {
            if (it.id == storyMedia.id) {
                it.number = selectedMedia.size
            }
        }

        selectedMediaSubject.onNext(selectedMedia)
        updateCurrentMediaList()
    }

    fun unselectMedia(storyMedia: StoryMedia) {
        selectedMedia.remove(storyMedia)
        allMediaList.forEach {
            it.number = -1
        }
        selectedMedia.forEachIndexed { selectedMediaPosition, selectedMedia ->
            allMediaList.forEach { media ->
                if (media.id == selectedMedia.id) {
                    media.number = selectedMediaPosition + 1
                    return@forEach
                }
            }
        }

        selectedMediaSubject.onNext(selectedMedia)
        updateCurrentMediaList()
    }

    fun unselectMedia(mediaPaths: List<String>) {
        for (mediaPath in mediaPaths) {
            selectedMedia.removeAll { it.originalFilePath == mediaPath }
        }
        allMediaList.forEach {
            it.number = -1
        }
        selectedMedia.forEachIndexed { selectedMediaPosition, selectedMedia ->
            allMediaList.forEach { media ->
                if (media.id == selectedMedia.id) {
                    media.number = selectedMediaPosition + 1
                    return@forEach
                }
            }
        }

        selectedMediaSubject.onNext(selectedMedia)
        updateCurrentMediaList()
    }


    fun prepareSelectedMedia() {
        CoroutineScope(Dispatchers.Main).launch {
            val rotatedMedia = withContext(Dispatchers.Default) {
                rotateImagesInSelectedMedia()
            }
            val resizedMedia = withContext(Dispatchers.Default) {
                resizeImagesInSelectedMedia(rotatedMedia)
            }
            if (resizedMedia == null) {
                ToastUtils.showToastMessage("Resize Image Error")
                return@launch
            }
            val preparedMedia = withContext(Dispatchers.Default) {
                convertImageMediaToVideos(resizedMedia)
            }
            if (preparedMedia == null) {
                ToastUtils.showToastMessage("Convert Image Error")
                return@launch
            }
            preparedSelectedMediaSubject.onNext(preparedMedia)
        }
    }

    fun rotateImagesInSelectedMedia(): List<StoryMedia> {
        val rotatedImages = mutableListOf<StoryMedia>()
        for (i in 0 until selectedMedia.size) {
            val media = selectedMedia[i]
            if (media.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                rotatedImages.add(media)
            } else {
                Timber.tag("WFT").d("Start checking imeage rotation, image pos = $i")
                val exifInterface = ExifInterface(media.path)
                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                var rotatedBitmap: Bitmap? = null
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(media.path, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(media.path, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(media.path, 270f)
                }

                if (rotatedBitmap == null) {
                    rotatedImages.add(media)
                    continue
                }

                val resFile = MediaUtils.saveBitmapToFile(rotatedBitmap)
                Timber.tag("WFT").d("Finish checking imeage rotation, image pos = $i")
                rotatedImages.add(media.clone().apply { path = resFile.path })
            }
        }
        return rotatedImages
    }

    private fun getBitmap(path: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            //bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
            bitmap = BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun rotateImage(imagePath: String, angle: Float): Bitmap? {
        val source = getBitmap(imagePath) ?: return null
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                matrix, false)
    }

    private fun resizeImagesInSelectedMedia(media: List<StoryMedia>): List<StoryMedia>? {
        val resizedMedia = mutableListOf<StoryMedia>()
        media.forEachIndexed { i, media ->
            if (media.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
//                val resizedVideo = FFmpegUtil.resize(Uri.parse(media.path)) ?: return null
//                resizedMedia.add(media.apply { path = resizedVideo.path!! })
                resizedMedia.add(media)
            } else {
                val resizedImage = FFmpegUtil.resizeImage(media.path) ?: return null
                resizedMedia.add(media.apply { path = resizedImage.path!! })
            }
        }
        return resizedMedia
    }

    fun convertImageMediaToVideos(media: List<StoryMedia>): MutableList<VideoSegment>? {
        val result = mutableListOf<VideoSegment>()
        media.forEachIndexed { i, media ->
            if (media.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                result.add(VideoSegment(Uri.parse(media.path), media.duration, false, media.originalFilePath))
            } else {
                val video = FFmpegUtil.createVideoFromImage(media.path, Constants.VIDEO_FROM_IMAGE_DURATION)
                        ?: return null
                result.add(VideoSegment(video, Constants.VIDEO_FROM_IMAGE_DURATION * 1000, true, media.originalFilePath))
            }
        }
        preparedSelectedMediaSubject.onNext(result)
        return result
    }

    private fun parseCursor(cursor: Cursor): List<StoryMedia> {
        val mediaList = mutableListOf<StoryMedia>()

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            var duration = cursor.getInt(durationColumn)
            val dateAdded = cursor.getLong(dateAddedColumn)
            val mediaType = cursor.getInt(mediaTypeColumn)
            val path = cursor.getString(dataColumn)
            val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO && duration == 0 && File(path).exists()) {
                val mediaMetadataRetriever = MediaMetadataRetriever().apply {
                    setDataSource(path)
                }
                duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
                mediaMetadataRetriever.release()
            }

            mediaList += StoryMedia(id, name, dateAdded, mediaType, contentUri, path, duration, path)
        }

        return mediaList
    }

    private fun updateCurrentMediaList() {
        val curAllMediaList = mutableListOf<StoryMedia>()
        if (currentFolderPath == null) {
            curAllMediaList.addAll(allMediaList.map { it.clone() })
        } else {
            allMediaList.forEach {
                if (it.path.startsWith(currentFolderPath!!)) {
                    curAllMediaList.add(it.clone())
                }
            }
        }

        val result = when (currentTab) {
            MediaPickerTab.ALL -> curAllMediaList
            MediaPickerTab.IMAGES -> curAllMediaList.filter { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE }
            MediaPickerTab.VIDEOS -> curAllMediaList.filter { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO }
        }
        mediaSubject.onNext(result)
    }
}