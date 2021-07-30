package com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetStrangerMessageBinding
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class StrangerMessageBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetStrangerMessageBinding
    var onCopyClickListener: ((Message.Text) -> Unit)? = null
    private var textMessage: Message.Text? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_stranger_message, container, false)

        setClickListeners()

        return layout.root
    }

    private fun setClickListeners() {
        layout.tvCopy.setOnClickListener {
            textMessage?.let {
                onCopyClickListener?.invoke(it)
            }
            dismiss()
        }
    }

    fun setMessage(textMessage: Message.Text) {
        this.textMessage = textMessage
    }

    fun show(childFragmentManager: FragmentManager, textMessage: Message.Text) {
        setMessage(textMessage)
        super.show(childFragmentManager, "StrangerMessageBottomSheet")
    }

    companion object {
        fun newInstance(): StrangerMessageBottomSheet {
            return StrangerMessageBottomSheet()
        }
    }
}