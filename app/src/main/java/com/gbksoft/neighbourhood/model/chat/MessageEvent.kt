package com.gbksoft.neighbourhood.model.chat

class MessageEvent private constructor(
    val eventType: Int,
    val messageId: Long,
    val message: Message,
    val extraData: ExtraData?
) {
    companion object {
        const val CREATE = 1
        const val UPDATE = 2
        const val DELETE = 3
        fun create(message: Message, extraData: ExtraData? = null) = MessageEvent(CREATE, message.id, message, extraData)
        fun update(message: Message, extraData: ExtraData? = null) = MessageEvent(UPDATE, message.id, message, extraData)
        fun delete(message: Message, extraData: ExtraData? = null) = MessageEvent(DELETE, message.id, message, extraData)
    }

    class ExtraData(
        val hasProfileUnreadMessages: Boolean
    )
}