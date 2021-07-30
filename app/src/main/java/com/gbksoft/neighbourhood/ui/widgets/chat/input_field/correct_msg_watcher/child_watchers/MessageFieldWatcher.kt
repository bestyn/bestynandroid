package com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.child_watchers

import android.text.Editable
import android.text.TextWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.ChildWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.ParentWatcher

class MessageFieldWatcher : TextWatcher, ChildWatcher {
    private var isTextEmpty = true
    private var parentWatcher: ParentWatcher? = null

    override fun setParent(parentWatcher: ParentWatcher) {
        this.parentWatcher = parentWatcher
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        val isEmpty = s?.length ?: 0 == 0
        if (isTextEmpty != isEmpty) {
            isTextEmpty = isEmpty
            parentWatcher?.setChildState(this, !isTextEmpty)
        }
    }

}