package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMedia
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media.MediaPickerTab
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class StoryMediaPickerViewModel(private val context: Context, private val isPost: Boolean, private val needVideo: Int) : BaseViewModel() {

    private val imageVideoPicker = ImageVideoPickerManager(context)

    private val _media = MutableLiveData<List<StoryMedia>>()
    val storyMedia: LiveData<List<StoryMedia>> = _media

    private val _selectedMedia = MutableLiveData<List<StoryMedia>>()
    val selectedStoryMedia: LiveData<List<StoryMedia>> = _selectedMedia

    private val _folders = MutableLiveData<List<String>>()
    val folders: LiveData<List<String>> = _folders

    private val _preparedSelectedVideoSegments = SingleLiveEvent<List<VideoSegment>>()
    val preparedSelectedVideoSegments: LiveData<List<VideoSegment>> = _preparedSelectedVideoSegments

    private val _preparedSelectedPictures = SingleLiveEvent<List<Media.Picture>>()
    val preparedSelectedPictures: LiveData<List<Media.Picture>> = _preparedSelectedPictures

    private val _preparedVideoAttachment = SingleLiveEvent<Media.Video>()
    val preparedVideoAttachment: LiveData<Media.Video> = _preparedVideoAttachment

    init {
        subscribeToMedia()
        subscribeToSelectedMedia()
        subscribeToMediaFolders()
        subscribeToPreparedSelectedMedia()
        if (needVideo == 1) imageVideoPicker.currentTab = MediaPickerTab.VIDEOS
        imageVideoPicker.loadMedia()
    }

    private fun subscribeToMedia() {
        addDisposable("subscribeToMedia", imageVideoPicker.subscribeToMedia()
                .subscribe {
                    _media.postValue(it)
                })
    }

    private fun subscribeToSelectedMedia() {
        addDisposable("subscribeToSelectedMedia", imageVideoPicker.subscribeSelectedMedia()
                .subscribe {
                    _selectedMedia.postValue(it)
                })
    }

    private fun subscribeToMediaFolders() {
        addDisposable("subscribeToMediaFolders", imageVideoPicker.subscribeToMediaFolders()
                .subscribe { _folders.postValue(it) })
    }

    private fun subscribeToPreparedSelectedMedia() {
        addDisposable("subscribeToPreparedSelectedMedia", imageVideoPicker.subscribeToPreparedSelectedMedia()
                .subscribe { _preparedSelectedVideoSegments.postValue(it) })
    }

    fun updateTab(tab: MediaPickerTab) {
        imageVideoPicker.currentTab = tab
    }

    fun selectMedia(storyMedia: StoryMedia) {
        imageVideoPicker.selectMedia(storyMedia)
    }

    fun unselectMedia(storyMedia: StoryMedia) {
        imageVideoPicker.unselectMedia(storyMedia)
    }

    fun onFolderChanged(folderPath: String) {
        imageVideoPicker.currentFolderPath = folderPath
    }

    fun prepareSelectedMedia(): Boolean {
        if (imageVideoPicker.selectedMedia.size < 1) {
            ToastUtils.showToastMessage(R.string.story_media_picker_no_files_error)
            return false
        } else if (!isPost && imageVideoPicker.selectedMedia.size > 30) {
            ToastUtils.showToastMessage(R.string.story_media_picker_too_many_files_error)
            return false
        } else if (isPost && imageVideoPicker.selectedMedia.size > 6) {
            ToastUtils.showToastMessage(R.string.story_media_picker_too_many_images_error)
        }

        when {
            isPost && needVideo == -1 -> {
                prepareSelectedPictures()
            }
            isPost && needVideo == 1 && imageVideoPicker.selectedMedia.firstOrNull { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} != null -> {
                prepareSelectedPictures()
            }
            isPost && needVideo == 1 && imageVideoPicker.selectedMedia.firstOrNull { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO} != null -> {
                prepareVideoAttachment()
            }
            isPost.not() -> {
                imageVideoPicker.prepareSelectedMedia()
            }
        }
        return true
    }

    private fun prepareSelectedPictures() {
        CoroutineScope(Dispatchers.Default).launch {
            val rotatedMedia = mutableListOf<StoryMedia>()
            imageVideoPicker.selectedMedia.forEach {
                val exifInterface = ExifInterface(it.path)
                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                var rotatedBitmap: Bitmap? = null
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = imageVideoPicker.rotateImage(it.path, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = imageVideoPicker.rotateImage(it.path, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = imageVideoPicker.rotateImage(it.path, 270f)
                }
                if (rotatedBitmap != null) {
                    val resFile = MediaUtils.saveBitmapToFile(rotatedBitmap)
                    rotatedMedia.add(it.clone().apply { path = resFile.path })
                } else {
                    rotatedMedia.add(it)
                }
            }


            val pictures = mutableListOf<Media.Picture>()
            rotatedMedia.forEach {
                val picture = Media.Picture.local(File(it.path))

                var imageOriginWidth = 0
                var imageOriginHeight = 0
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                context.contentResolver.openFileDescriptor(File(it.path).toUri(), "r")?.let { fd ->
                    BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, options)
                    imageOriginWidth = options.outWidth
                    imageOriginHeight = options.outHeight
                }

                val dstRatio = Constants.PIC_CROP_HEIGHT_RATIO / Constants.PIC_CROP_WIDTH_RATIO.toFloat()
                var curRatio = imageOriginHeight / imageOriginWidth.toFloat()

                var dstWidth = imageOriginWidth
                var dstHeight = imageOriginHeight
                if (curRatio > dstRatio) {
                    dstHeight = (imageOriginWidth * dstRatio).toInt()
                } else {
                    dstWidth = (imageOriginHeight * dstRatio).toInt()
                }

                val previewRect = Rect(imageOriginWidth / 2 - dstWidth / 2,
                        imageOriginHeight / 2 - dstHeight / 2,
                        imageOriginWidth / 2 + dstWidth / 2,
                        imageOriginHeight / 2 + dstHeight / 2)

                picture.previewArea = previewRect
                pictures.add(picture)

            }
            _preparedSelectedPictures.postValue(pictures)
        }
    }

    private fun prepareVideoAttachment() {
        imageVideoPicker.selectedMedia.forEach {
            _preparedVideoAttachment.postValue(Media.Video.local(File(it.path)))
        }


    }

    private fun prepareSelectedVideoSegments() {
        CoroutineScope(Dispatchers.Default).launch {
            imageVideoPicker.rotateImagesInSelectedMedia()
        }
    }
}