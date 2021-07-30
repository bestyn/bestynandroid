package com.gbksoft.neighbourhood.ui.fragments.base.chat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.utils.LocalFileFactory
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import java.io.File
import java.io.IOException

class AttachmentProvider(val context: Context, val fragment: Fragment) {
    companion object {
        const val TYPE_PICTURE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_PICTURE_OR_VIDEO = 3
        const val TYPE_FILE = 4

        const val REQUEST_FROM_CAMERA = 1451
        const val REQUEST_FROM_GALLERY = 1452
        const val REQUEST_FILE = 1453
    }


    private var fileFromCamera: File? = null
    private var contentType: Int? = null
    private val localFileFactory = LocalFileFactory(context)

    fun requestPictureFromCamera() {
        contentType = TYPE_PICTURE
        requestCameraPermission()
    }

    fun requestVideoFromCamera() {
        contentType = TYPE_VIDEO
        requestCameraPermission()
    }

    fun requestPictureOrVideoFromGallery() {
        contentType = TYPE_PICTURE_OR_VIDEO
        requestStoragePermission()
    }

    fun requestPictureFromGallery() {
        contentType = TYPE_PICTURE
        requestStoragePermission()
    }

    fun requestVideoFromGallery() {
        contentType = TYPE_VIDEO
        requestStoragePermission()
    }

    fun requestFile() {
        contentType = TYPE_FILE
        requestStoragePermission()
    }

    private fun requestCameraPermission() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = { dispatchCameraIntent() }
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

    private fun dispatchCameraIntent() {
        val takePictureIntent = when (contentType) {
            TYPE_PICTURE -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            TYPE_VIDEO -> Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            else -> return
        }
        if (takePictureIntent.resolveActivity(context.packageManager) == null) {
            //Missing camera app
            return
        }

        val tempFile = createTempFile(contentType)

        if (tempFile != null) {
            val fileUri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID, tempFile)
            fileFromCamera = tempFile
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            fragment.startActivityForResult(takePictureIntent, REQUEST_FROM_CAMERA)
        }
    }

    private fun createTempFile(contentType: Int?): File? {
        return try {
            if (contentType == TYPE_PICTURE) {
                MediaUtils.createTempPhotoFile(context)
            } else {
                MediaUtils.createTempVideoFile(context)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun requestStoragePermission() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = {
            if (contentType == TYPE_FILE) {
                dispatchAnyFileIntent()
            } else {
                dispatchGalleryIntent()
            }
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

    private fun dispatchAnyFileIntent() {
        val intent: Intent = when (contentType) {
            TYPE_FILE -> {
                Intent(Intent.ACTION_GET_CONTENT).apply { this.type = "*/*" }
            }
            else -> return
        }
        fragment.startActivityForResult(intent, REQUEST_FILE)
    }

    private fun dispatchGalleryIntent() {
        val intent: Intent = when (contentType) {
            TYPE_PICTURE -> {
                Intent(Intent.ACTION_GET_CONTENT).apply { this.type = "image/*" }
            }
            TYPE_VIDEO -> {
                Intent(Intent.ACTION_GET_CONTENT).apply { this.type = "video/mp4" }
            }
            TYPE_PICTURE_OR_VIDEO -> {
                Intent(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)).apply {
                    type = "image/* video/*"
                }
            }
            else -> return
        }
        fragment.startActivityForResult(intent, REQUEST_FROM_GALLERY)
    }

    /**
     * LocalFile<Int> type is one of [Attachment] TYPE_..
     */
    fun prepareLocalAttachment(requestCode: Int, resultCode: Int, data: Intent?): LocalFile<Int>? {
        if (resultCode != Activity.RESULT_OK) return null

        return when (requestCode) {
            REQUEST_FROM_CAMERA -> {
                localAttachmentFromCamera(data)
            }
            REQUEST_FROM_GALLERY -> {
                if (data != null) localAttachmentFromGallery(data) else null
            }
            REQUEST_FILE -> {
                if (data != null) localAttachmentFromFile(data) else null
            }
            else -> null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun localAttachmentFromCamera(data: Intent?): LocalFile<Int>? {
        val file = fileFromCamera ?: return null

        val type = when (contentType) {
            TYPE_PICTURE -> Attachment.TYPE_PICTURE
            TYPE_VIDEO -> Attachment.TYPE_VIDEO
            else -> return null
        }
        val fileName = if (type == TYPE_PICTURE) {
            MediaUtils.generatePhotoFileName()
        } else {
            MediaUtils.generateVideoFileName()
        }
        return localFileFactory.fromFile(file, data, type, fileName)
    }

    @Throws(IllegalArgumentException::class)
    private fun localAttachmentFromGallery(data: Intent): LocalFile<Int>? {
        val uri = data.data ?: return null

        val localFile = localFileFactory.fromUri<Int>(uri, null)
        when {
            localFile.mime.startsWith("image/") -> {
                localFile.type = Attachment.TYPE_PICTURE
            }
            localFile.mime.startsWith("video/") -> {
                localFile.type = Attachment.TYPE_VIDEO
            }
        }
        return localFile
    }

    @Throws(IllegalArgumentException::class)
    private fun localAttachmentFromFile(data: Intent): LocalFile<Int>? {
        val uri = data.data ?: return null

        return localFileFactory.fromUri(uri, Attachment.TYPE_FILE)
    }
}