package com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetAttachmentBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class AttachmentBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetAttachmentBinding
    var onSelectFromGalleryClickListener: (() -> Unit)? = null
    var onTakePhotoClickListener: (() -> Unit)? = null
    var onMakeVideoClickListener: (() -> Unit)? = null
    var onFileClickListener: (() -> Unit)? = null


    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_attachment, container, false)
        layout.tvSelectFromGallery.setOnClickListener {
            onSelectFromGalleryClickListener?.invoke()
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
        layout.tvFile.setOnClickListener {
            onFileClickListener?.invoke()
            dismiss()
        }

        return layout.root
    }

    companion object {
        fun newInstance(): AttachmentBottomSheet {
            return AttachmentBottomSheet()
        }
    }
}