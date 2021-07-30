package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetPostMediaBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.base.media.MediaProvider

class PostMediaBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetPostMediaBinding
    private var mediaType: MediaProvider.Type? = null
    var onSelectVideoFromGalleryClickListener: (() -> Unit)? = null
    var onSelectImageFromGalleryClickListener: (() -> Unit)? = null
    var onTakePhotoClickListener: (() -> Unit)? = null
    var onMakeVideoClickListener: (() -> Unit)? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_post_media, container, false)
        setClickListeners()
        resolveOptionsVisibility()
        return layout.root
    }

    private fun setClickListeners() {
        layout.tvSelectImageFromGallery.setOnClickListener {
            onSelectImageFromGalleryClickListener?.invoke()
            dismiss()
        }
        layout.tvSelectVideoFromGallery.setOnClickListener {
            onSelectVideoFromGalleryClickListener?.invoke()
            dismiss()
        }
        layout.tvTakePhoto.setOnClickListener {
            onTakePhotoClickListener?.invoke()
            dismiss()
        }
        layout.tvMakeVideo.setOnClickListener {
            onMakeVideoClickListener?.invoke()
            dismiss()
        }
    }

    fun setAvailableMediaType(type: MediaProvider.Type?) {
        mediaType = type
        if (this::layout.isInitialized) resolveOptionsVisibility()
    }

    private fun resolveOptionsVisibility() {
        when (mediaType) {
            MediaProvider.Type.VIDEO -> {
                layout.tvSelectImageFromGallery.visibility = View.GONE
                layout.tvTakePhoto.visibility = View.GONE
                layout.takePhotoDivider.visibility = View.GONE
                layout.tvMakeVideo.visibility = View.VISIBLE
                layout.makeVideoDivider.visibility = View.VISIBLE
                layout.tvSelectVideoFromGallery.visibility = View.VISIBLE
                layout.selectVideoDivider.visibility = View.VISIBLE

            }
            MediaProvider.Type.PICTURE -> {
                layout.tvSelectVideoFromGallery.visibility = View.GONE
                layout.selectVideoDivider.visibility = View.GONE
                layout.tvMakeVideo.visibility = View.GONE
                layout.makeVideoDivider.visibility = View.GONE
                layout.tvSelectImageFromGallery.visibility = View.VISIBLE
                layout.tvTakePhoto.visibility = View.VISIBLE
                layout.takePhotoDivider.visibility = View.VISIBLE
            }
            else -> {
                layout.tvTakePhoto.visibility = View.VISIBLE
                layout.takePhotoDivider.visibility = View.VISIBLE
                layout.tvMakeVideo.visibility = View.VISIBLE
                layout.makeVideoDivider.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        fun newInstance(): PostMediaBottomSheet {
            return PostMediaBottomSheet()
        }
    }
}