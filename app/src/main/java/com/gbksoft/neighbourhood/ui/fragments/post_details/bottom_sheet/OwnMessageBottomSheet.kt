package com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetOwnMessageBinding
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class OwnMessageBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetOwnMessageBinding
    var onCopyClickListener: ((Message.Text) -> Unit)? = null
    var onEditClickListener: ((Message) -> Unit)? = null
    var onDeleteClickListener: ((Message) -> Unit)? = null
    private var message: Message? = null
    private var textMessage: Message.Text? = null
    private var isCopyEnabled: Boolean = false
    var isEditingEnabled: Boolean = true
        set(value) {
            field = value
            if (this::layout.isInitialized) resolveEditVisibility()
        }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_own_message, container, false)

        resolveEditVisibility()
        resolveCopyVisibility()
        setClickListeners()

        return layout.root
    }

    private fun resolveEditVisibility() {
        if (isEditingEnabled) {
            layout.tvEdit.visibility = View.VISIBLE
            layout.divider.visibility = View.VISIBLE
        } else {
            layout.tvEdit.visibility = View.GONE
            layout.divider.visibility = View.GONE
        }
    }

    private fun resolveCopyVisibility() {
        if (isCopyEnabled) {
            layout.tvCopy.visibility = View.VISIBLE
            layout.dividerCopy.visibility = View.VISIBLE
        } else {
            layout.tvCopy.visibility = View.GONE
            layout.dividerCopy.visibility = View.GONE
        }
    }

    private fun setClickListeners() {
        layout.tvCopy.setOnClickListener {
            textMessage?.let {
                onCopyClickListener?.invoke(it)
            }
            dismiss()
        }
        layout.tvEdit.setOnClickListener {
            message?.let {
                onEditClickListener?.invoke(it)
            }
            dismiss()
        }
        layout.tvDelete.setOnClickListener {
            message?.let {
                onDeleteClickListener?.invoke(it)
            }
            dismiss()
        }
    }

    private fun setMessage(message: Message) {
        this.message = message
        if (message is Message.Text && message.text.isNotEmpty()) {
            textMessage = message
            isCopyEnabled = true
        } else {
            textMessage = null
            isCopyEnabled = false
        }
        if (this::layout.isInitialized) resolveCopyVisibility()
    }

    fun show(childFragmentManager: FragmentManager, message: Message) {
        setMessage(message)
        super.show(childFragmentManager, "OwnMessageBottomSheet")
    }

    companion object {
        fun newInstance(): OwnMessageBottomSheet {
            return OwnMessageBottomSheet()
        }
    }
}