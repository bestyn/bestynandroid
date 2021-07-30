package com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter

import com.gbksoft.neighbourhood.ui.widgets.chat.message.view.MessageView

class MessageViewMap {
    private val ids = mutableMapOf<Long, MessageView>()
    private val views = mutableMapOf<MessageView, Long>()

    fun get(id: Long): MessageView? {
        return ids[id]
    }

    fun put(id: Long, view: MessageView) {
        views[view]?.let {
            ids.remove(it)
        }
        ids[id] = view
        views[view] = id
    }
}