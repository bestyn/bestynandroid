package com.gbksoft.neighbourhood.mappers.chat

import com.gbksoft.neighbourhood.data.models.response.chat.AttachmentModel
import com.gbksoft.neighbourhood.data.models.response.chat.AuthorModel
import com.gbksoft.neighbourhood.data.models.response.chat.ChatMessageModel
import com.gbksoft.neighbourhood.data.models.response.chat.PostMessageModel
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.profile.ProfileTypeMapper
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageAuthor
import java.net.URLDecoder

object MessageMapper {

    fun fromPostMessageModels(models: List<PostMessageModel>): MutableList<Message> {
        val messages = mutableListOf<Message>()
        for (model in models) {
            messages.add(toMessage(model))
        }
        return messages
    }

    fun fromChatMessageModels(models: List<ChatMessageModel>): MutableList<Message> {
        val messages = mutableListOf<Message>()
        for (model in models) {
            messages.add(toMessage(model))
        }
        return messages
    }

    fun toMessage(model: PostMessageModel): Message {
        val author = toMessageAuthor(model.authorModel)
        val attachment = model.attachment?.let {
            toMessageAttachment(it)
        }
        return Message.Text(
            model.id,
            author,
            null,
            TimestampMapper.toAppTimestamp(model.createdAt),
            TimestampMapper.toAppTimestamp(model.updatedAt),
            model.text ?: "",
            attachment
        )
    }

    fun toMessage(model: ChatMessageModel): Message {
        val author = toMessageAuthor(model.senderProfile)
        val recipient = toMessageAuthor(model.recipientProfile)
        val attachment = model.attachment?.let {
            toMessageAttachment(it)
        }

        val message = if (attachment != null && attachment.type == Attachment.TYPE_AUDIO) {
            Message.Audio(
                model.id,
                author,
                recipient,
                TimestampMapper.toAppTimestamp(model.createdAt),
                TimestampMapper.toAppTimestamp(model.updatedAt),
                attachment
            )
        } else {
            Message.Text(
                model.id,
                author,
                recipient,
                TimestampMapper.toAppTimestamp(model.createdAt),
                TimestampMapper.toAppTimestamp(model.updatedAt),
                model.text ?: "",
                attachment
            )
        }
        message.isRead = model.isRead ?: false
        return message
    }

    fun toMessageAuthor(authorModel: AuthorModel): MessageAuthor {
        val isBusiness = ProfileTypeMapper.isBusiness(authorModel.type)
        return MessageAuthor(
            authorModel.id,
            authorModel.avatar?.origin,
            authorModel.fullName,
            isBusiness
        )
    }

    fun toMessageAttachment(attachmentModel: AttachmentModel): Attachment {
        val title = decodeAttachmentTitle(attachmentModel.title)
            ?: attachmentModel.origin.substringAfterLast('/')

        val type = when (attachmentModel.type) {
            "image" -> Attachment.TYPE_PICTURE
            "video" -> Attachment.TYPE_VIDEO
            "voice" -> Attachment.TYPE_AUDIO
            else -> Attachment.TYPE_FILE
        }
        val preview = when (type) {
            Attachment.TYPE_VIDEO -> {
                attachmentModel.formatted?.thumbnail ?: attachmentModel.origin
            }
            Attachment.TYPE_PICTURE -> {
                attachmentModel.formatted?.medium ?: attachmentModel.origin
            }
            else -> attachmentModel.origin
        }
        val created = TimestampMapper.toAppTimestamp(attachmentModel.createdAt)
        val updated = attachmentModel.updatedAt?.let { TimestampMapper.toAppTimestamp(it) }
        return Attachment(
            attachmentModel.id,
            type,
            title,
            attachmentModel.origin,
            preview,
            created,
            updated
        )
    }


    /**
     * @return one of: image, video, other, voice or null
     */
    fun getTypeName(type: Int?): String? {
        return when (type) {
            Attachment.TYPE_PICTURE -> "image"
            Attachment.TYPE_VIDEO -> "video"
            Attachment.TYPE_FILE -> "other"
            Attachment.TYPE_AUDIO -> "voice"
            else -> null
        }
    }

    fun decodeAttachmentTitle(name: String?): String? {
        return if (name == null) {
            null
        } else {
            URLDecoder.decode(name, "utf-8")
        }
    }
}