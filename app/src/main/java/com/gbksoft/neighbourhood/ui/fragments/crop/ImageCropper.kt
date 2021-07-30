package com.gbksoft.neighbourhood.ui.fragments.crop

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.theartofdev.edmodo.cropper.CropImageView

class ImageCropper(
    private val cropImageView: CropImageView
) {
    private var imageOriginWidth = 0
    private var imageOriginHeight = 0
    private var imageOrientation: Int? = null

    init {
        cropImageView.setOnSetImageUriCompleteListener { view, _, _ ->
            fixImageRotation(view)
        }
    }

    fun loadImage(context: Context, uri: Uri) {
        fetchImageOriginSize(context.contentResolver, uri)
        fetchRotationDegrees(context.contentResolver, uri)
        cropImageView.setImageUriAsync(uri)
    }

    private fun fetchImageOriginSize(contentResolver: ContentResolver, uri: Uri) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        contentResolver.openFileDescriptor(uri, "r")?.let { fd ->
            BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, options)
            imageOriginWidth = options.outWidth
            imageOriginHeight = options.outHeight
        }
    }

    private fun fetchRotationDegrees(contentResolver: ContentResolver, uri: Uri) {
        contentResolver.openInputStream(uri)?.use {
            val exif = ExifInterface(it)
            imageOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }


    private fun fixImageRotation(cropImageView: CropImageView) {
        when (imageOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> cropImageView.flipImageHorizontally()
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> cropImageView.flipImageVertically()
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                cropImageView.rotatedDegrees = 90
                cropImageView.flipImageHorizontally()
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                cropImageView.rotatedDegrees = -90
                cropImageView.flipImageHorizontally()
            }
        }
    }

    fun getCropRect(): Rect {
        val cropRect = cropImageView.cropRect
        val imageRect = cropImageView.wholeImageRect
        checkRectSize(cropRect, imageRect)
        checkRectRotation(cropRect, imageRect)
        return cropRect
    }

    private fun checkRectSize(cropRect: Rect, wholeImageRect: Rect) {
        if (imageOriginWidth == wholeImageRect.width() &&
            imageOriginHeight == wholeImageRect.height()) return

        val diffWidth = imageOriginWidth.toDouble() / wholeImageRect.width().toDouble()
        val diffHeight = imageOriginHeight.toDouble() / wholeImageRect.height().toDouble()
        val diff = if (diffWidth < diffHeight) diffWidth else diffHeight
        cropRect.left = (diff * cropRect.left).toInt()
        cropRect.right = (diff * cropRect.right).toInt()
        cropRect.top = (diff * cropRect.top).toInt()
        cropRect.bottom = (diff * cropRect.bottom).toInt()
    }

    private fun checkRectRotation(cropRect: Rect, imageRect: Rect) {
        when (imageOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                cropRect.flipHorizontally(imageRect.exactCenterX())
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                cropRect.flipVertically(imageRect.exactCenterY())
                cropRect.flipHorizontally(imageRect.exactCenterX())
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                cropRect.flipVertically(imageRect.exactCenterY())
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                cropRect.left = cropRect.top.apply { cropRect.top = cropRect.left }
                cropRect.right = cropRect.bottom.apply { cropRect.bottom = cropRect.right }
                imageRect.left = imageRect.top.apply { imageRect.top = imageRect.left }
                imageRect.right = imageRect.bottom.apply { imageRect.bottom = imageRect.right }
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                val px = imageRect.exactCenterX()
                val py = imageRect.exactCenterY()
                imageRect.rotate(90f, px, py)
                cropRect.rotate(90f, px, py)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                cropRect.flipHorizontally(imageRect.exactCenterX())
                val px = imageRect.exactCenterX()
                val py = imageRect.exactCenterY()
                imageRect.rotate(90f, px, py)
                cropRect.rotate(90f, px, py)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                val px = imageRect.exactCenterX()
                val py = imageRect.exactCenterY()
                imageRect.rotate(270f, px, py)
                cropRect.rotate(270f, px, py)
            }
        }
    }
}