package com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.child_watchers

import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.ChildWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.ParentWatcher

class AttachmentWatcher : ChildWatcher {
    private var containsAttachment = false
    private var parentWatcher: ParentWatcher? = null
    var attachment: Attachment? = null
        private set

    override fun setParent(parentWatcher: ParentWatcher) {
        this.parentWatcher = parentWatcher
    }

    fun setAttachment(attachment: Attachment?) {
        this.attachment = attachment
        val isNotNull = attachment != null
        if (containsAttachment != isNotNull) {
            containsAttachment = isNotNull
            parentWatcher?.setChildState(this, containsAttachment)
        }
    }

}