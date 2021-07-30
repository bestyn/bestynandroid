package com.gbksoft.neighbourhood.ui.widgets.chat.input_field

import android.view.View
import android.widget.EditText
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.chat.Attachment
import timber.log.Timber

class AttachmentButtonController(
    private val btnAddAttachment: View,
    private val messageField: EditText
) {
    private var attachment: Attachment? = null
    private var iconWidth: Int
    private var iconPadding: Int
    private var fieldLeftPadding: Int

    init {
        val res = btnAddAttachment.resources
        iconWidth = res.getDimensionPixelSize(R.dimen.input_message_add_attachment_control_size)
        iconPadding = res.getDimensionPixelSize(R.dimen.input_message_add_attachment_control_spacing)
        fieldLeftPadding = res.getDimensionPixelSize(R.dimen.input_message_field_v_padding)

        resolveButtonVisibility()
    }

    fun setAttachment(attachment: Attachment?) {
        this.attachment = attachment
        resolveButtonVisibility()
    }

    private fun resolveButtonVisibility() {
        if (attachment == null) showAttachmentIcon()
        else hideAttachmentIcon()
    }

    private fun showAttachmentIcon() {
        btnAddAttachment.visibility = View.VISIBLE
        val left = fieldLeftPadding + iconWidth + iconPadding
        val top = messageField.paddingTop
        val right = messageField.paddingRight
        val bottom = messageField.paddingBottom
        Timber.tag("InputTag").d("showAttachmentIcon() left: $left")
        messageField.setPadding(left, top, right, bottom)
    }

    private fun hideAttachmentIcon() {
        btnAddAttachment.visibility = View.GONE
        val left = fieldLeftPadding
        val top = messageField.paddingTop
        val right = messageField.paddingRight
        val bottom = messageField.paddingBottom
        Timber.tag("InputTag").d("hideAttachmentIcon() left: $left")
        messageField.setPadding(left, top, right, bottom)
    }
}