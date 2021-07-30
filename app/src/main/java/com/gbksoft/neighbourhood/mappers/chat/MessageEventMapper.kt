package com.gbksoft.neighbourhood.mappers.chat

import com.gbksoft.neighbourhood.data.models.response.chat.ChatMessageEventModel
import com.gbksoft.neighbourhood.data.models.response.chat.PostMessageEventModel
import com.gbksoft.neighbourhood.model.chat.MessageEvent

object MessageEventMapper {
    fun toMessageEvent(model: PostMessageEventModel): MessageEvent {
        return when (model.action) {
            "create" -> {
                MessageEvent.create(MessageMapper.toMessage(model.message))
            }
            "update" -> {
                MessageEvent.update(MessageMapper.toMessage(model.message))
            }
            "delete" -> {
                MessageEvent.delete(MessageMapper.toMessage(model.message))
            }
            else -> {
                throw IllegalArgumentException("Undefined message action: ${model.action}")
            }
        }
    }

    fun toMessageEvent(model: ChatMessageEventModel): MessageEvent {
        val extraData = model.extraData?.let {
            return@let MessageEvent.ExtraData(it.hasUnreadMessages ?: false)
        }
        return when (model.action) {
            "create" -> {
                MessageEvent.create(MessageMapper.toMessage(model.message), extraData)
            }
            "update" -> {
                MessageEvent.update(MessageMapper.toMessage(model.message), extraData)
            }
            "delete" -> {
                MessageEvent.delete(MessageMapper.toMessage(model.message), extraData)
            }
            else -> {
                throw IllegalArgumentException("Undefined message action: ${model.action}")
            }
        }
    }
}