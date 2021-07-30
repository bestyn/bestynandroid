package com.gbksoft.neighbourhood.ui.fragments.base.media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import java.io.File
import java.io.IOException


open class MediaProvider(protected val context: Context, protected val fragment: Fragment) {
    enum class Type { PICTURE, VIDEO, AUDIO_RECORD }

    private var fileFromCamera: File? = null
    private var contentType: Type? = null

    fun requestPictureFromCamera() {
        contentType = Type.PICTURE
        requestCameraPermission()
    }

    fun requestVideoFromCamera() {
        contentType = Type.VIDEO
        requestCameraPermission()
    }

    fun requestPictureOrVideoFromGallery() {
        contentType = null
        requestWriteStoragePermission()
    }

    fun requestPictureFromGallery() {
        contentType = Type.PICTURE
        requestWriteStoragePermission()
    }

    fun requestVideoFromGallery() {
        contentType = Type.VIDEO
        requestWriteStoragePermission()
    }

    fun requestAudioFromRecords() {
        contentType = Type.AUDIO_RECORD
        requestWriteStoragePermission()
    }

    private fun requestCameraPermission() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = { dispatchTakePictureIntent() }
        permissionListener.onPermissionToken = { it.continuePermissionRequest() }
        Dexter.withContext(context)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(permissionListener)
                .withErrorListener { error: DexterError ->
                    ToastUtils.showToastMessage(context, "Error occurred: $error")
                }
                .onSameThread()
                .check()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = when (contentType) {
            Type.PICTURE -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            Type.VIDEO -> Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            else -> return
        }
        if (takePictureIntent.resolveActivity(context.packageManager) == null) {
            //Missing camera app
            return
        }

        var photoFile: File? = null
        try {
            photoFile = MediaUtils.createTempPhotoFile(context)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        if (photoFile != null) {
            val fileUri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID, photoFile)
            fileFromCamera = photoFile
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            fragment.startActivityForResult(takePictureIntent, REQUEST_FROM_CAMERA)
        }
    }

    private fun requestWriteStoragePermission() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = {
            if (contentType == Type.AUDIO_RECORD)
                dispatchAudioRecordsIntent()
            else
                dispatchGalleryPicker()
        }
        permissionListener.onPermissionToken = { it.continuePermissionRequest() }
        Dexter.withContext(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(permissionListener)
                .withErrorListener { error: DexterError ->
                    ToastUtils.showToastMessage(context, "Error occurred: $error")
                }
                .onSameThread()
                .check()
    }

    private fun dispatchAudioRecordsIntent(){
        val intent =  Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "audio/*"
        fragment.startActivityForResult(intent, REQUEST_FROM_SOUNDS)

    }

    private fun dispatchGalleryPicker() {
        val intent: Intent = when (contentType) {
            Type.PICTURE -> {
                Intent(Intent.ACTION_GET_CONTENT).apply { this.type = "image/*" }
            }
            Type.VIDEO -> {
                Intent(Intent.ACTION_GET_CONTENT).apply { this.type = "video/mp4" }
            }
            else -> {
                prepareIntentForAnyType()
            }
        }
        fragment.startActivityForResult(intent, REQUEST_FROM_GALLERY)
    }

    private fun prepareIntentForAnyType(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/mp4"))
        return intent
    }

    fun fetchCameraFile(): File? = fileFromCamera

    fun fetchGalleryUri(data: Intent?): Uri? = data?.data

    fun fetchFileContentType(): Type? {
        return contentType
    }

    fun fetchUriContentType(data: Intent?): Type? {
        data?.data?.let { uri ->
            context.contentResolver.getType(uri)?.let { type ->
                if (type.startsWith("image/")) return Type.PICTURE
                else if (type.startsWith("video/")) return Type.VIDEO
            }
        }
        return null
    }

    companion object {
        const val REQUEST_FROM_CAMERA = 1470
        const val REQUEST_FROM_GALLERY = 1471
        const val REQUEST_FROM_SOUNDS = 1472
    }
}