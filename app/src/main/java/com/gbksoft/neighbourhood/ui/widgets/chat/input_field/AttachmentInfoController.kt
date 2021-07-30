package com.gbksoft.neighbourhood.ui.widgets.chat.input_field

import android.view.View
import android.widget.TextView
import com.gbksoft.neighbourhood.model.chat.Attachment

class AttachmentInfoController(
    private val groupAttachment: View,
    private val iconPicture: View,
    private val iconVideo: View,
    private val iconFile: View,
    private val attachmentName: TextView,
    private val btnRemoveAttachment: View
) {

    private var attachment: Attachment? = null

    fun setAttachment(attachment: Attachment?) {
        this.attachment = attachment
        attachment?.let { setupViews(it) }
        resolveVisibility()
    }

    private fun setupViews(attachment: Attachment) {
        when (attachment.type) {
            Attachment.TYPE_FILE -> {
                iconPicture.visibility = View.GONE
                iconVideo.visibility = View.GONE
                iconFile.visibility = View.VISIBLE
            }
            Attachment.TYPE_PICTURE -> {
                iconVideo.visibility = View.GONE
                iconFile.visibility = View.GONE
                iconPicture.visibility = View.VISIBLE
            }
            Attachment.TYPE_VIDEO -> {
                iconPicture.visibility = View.GONE
                iconFile.visibility = View.GONE
                iconVideo.visibility = View.VISIBLE
            }
        }
        attachmentName.text = attachment.title
    }

    private fun resolveVisibility() {
        if (attachment == null) {
            groupAttachment.visibility = View.GONE
        } else {
            groupAttachment.visibility = View.VISIBLE
        }
    }
}