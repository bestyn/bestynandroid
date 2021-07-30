package com.gbksoft.neighbourhood.mappers.chat

import com.gbksoft.neighbourhood.data.models.response.chat.ConversationModel
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.profile.ProfileTypeMapper
import com.gbksoft.neighbourhood.model.chat.Conversation

object ConversationMapper {

    fun toConversation(model: ConversationModel): Conversation {
        val opponent = Conversation.Opponent(
            model.profile.id,
            model.profile.fullName,
            model.profile.avatar?.formatted?.small,
            ProfileTypeMapper.isBusiness(model.profile.type)
        )
        val lastMessage = Conversation.LastMessage(
            model.lastMessage.attachment?.type == "voice",
            model.lastMessage.text,
            model.lastMessage.attachment?.title,
            TimestampMapper.toAppTimestamp(model.lastMessage.updatedAt),
            model.lastMessage.senderProfileId != model.profile.id,
            model.lastMessage.isRead
        )
        return Conversation(
            model.id,
            opponent,
            model.unreadTotal,
            model.firstUnreadMessageId,
            lastMessage, model.profile.isOnline
        )
    }

}