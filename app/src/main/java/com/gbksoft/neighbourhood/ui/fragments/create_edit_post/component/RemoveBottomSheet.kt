package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetRemoveBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class RemoveBottomSheet<T> : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetRemoveBinding
    var onRemoveClickListener: ((T) -> Unit)? = null
    var item: T? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_remove, container, false)
        setClickListeners()
        return layout.root
    }

    private fun setClickListeners() {
        layout.tvRemove.setOnClickListener {
            item?.let { onRemoveClickListener?.invoke(it) }
            dismiss()
        }
    }

    companion object {
        fun <T> newInstance(): RemoveBottomSheet<T> {
            return RemoveBottomSheet()
        }
    }
}