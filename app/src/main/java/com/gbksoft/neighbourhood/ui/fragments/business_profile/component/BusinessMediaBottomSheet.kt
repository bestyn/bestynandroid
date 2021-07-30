package com.gbksoft.neighbourhood.ui.fragments.business_profile.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetBusinessMediaBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class BusinessMediaBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetBusinessMediaBinding
    var onSelectFromGalleryClickListener: (() -> Unit)? = null
    var onTakePhotoClickListener: (() -> Unit)? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_business_media, container, false)
        setClickListeners()
        return layout.root
    }

    private fun setClickListeners() {
        layout.tvSelectFromGallery.setOnClickListener {
            onSelectFromGalleryClickListener?.invoke()
            dismiss()
        }
        layout.tvTakePhoto.setOnClickListener {
            onTakePhotoClickListener?.invoke()
            dismiss()
        }
    }

    companion object {
        fun newInstance(): BusinessMediaBottomSheet {
            return BusinessMediaBottomSheet()
        }
    }
}