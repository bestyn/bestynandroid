package com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher

interface ParentWatcher {
    fun setChildState(childWatcher: ChildWatcher, isSuitableState: Boolean)
}