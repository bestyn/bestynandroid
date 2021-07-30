package com.gbksoft.neighbourhood.ui.fragments.profile.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetAvatarBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class AvatarBottomSheet : CancelableBottomSheet() {
    companion object {
        @JvmStatic
        fun newInstance(): AvatarBottomSheet {
            return AvatarBottomSheet()
        }
    }

    private lateinit var layout: BottomSheetAvatarBinding
    private var isShowRemove = false

    var onTakePhotoClickListener: (() -> Unit)? = null
    var onSelectFromGalleryClickListener: (() -> Unit)? = null
    var onRemoveClickListener: (() -> Unit)? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_avatar, container, false)

        setClickListeners()

        return layout.root
    }

    private fun setClickListeners() {
        layout.tvTakePhoto.setOnClickListener {
            onTakePhotoClickListener?.invoke()
            dismiss()
        }
        layout.tvSelectFromGallery.setOnClickListener {
            onSelectFromGalleryClickListener?.invoke()
            dismiss()
        }
        layout.tvRemove.setOnClickListener {
            onRemoveClickListener?.invoke()
            dismiss()
        }
    }

    fun setShowRemove(showRemove: Boolean) {
        isShowRemove = showRemove
    }

    override fun onResume() {
        super.onResume()
        if (isShowRemove) {
            showRemove()
        } else {
            hideRemove()
        }
    }

    private fun showRemove() {
        layout.removeDivider.visibility = View.VISIBLE
        layout.tvRemove.visibility = View.VISIBLE
    }

    private fun hideRemove() {
        layout.removeDivider.visibility = View.GONE
        layout.tvRemove.visibility = View.GONE
    }
}