package com.gbksoft.neighbourhood.ui.fragments.chat.room

import com.gbksoft.neighbourhood.model.chat.Message

class MessagesData(
    var messages: List<Message>,
    var firstUnreadMessageId: Long? = null
)