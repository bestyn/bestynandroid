package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.centrifuge.CentrifugeManager
import com.gbksoft.neighbourhood.data.models.request.post_chat.PostMessageReq
import com.gbksoft.neighbourhood.data.models.request.post_chat.UploadFileReq
import com.gbksoft.neighbourhood.data.models.response.chat.PostMessageEventModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.mappers.chat.MessageEventMapper
import com.gbksoft.neighbourhood.mappers.chat.MessageMapper
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageEvent
import com.gbksoft.neighbourhood.utils.Constants
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Observable

class PostChatRepository : BaseRepository() {
    private val expand = "profile.avatar.formatted,attachment.formatted"
    private val paginationDirection = "prev" //"next"
    private val gson = Gson()

    fun getPostMessages(postId: Long, lastMessageId: Long? = null): Observable<List<Message>> {
        return ApiFactory.apiPostMessage
            .getPostMessageList(postId, expand, lastMessageId, paginationDirection, Constants.PER_PAGE)
            .map { MessageMapper.fromPostMessageModels(it.requireResult()) }
    }

    fun subscribeToMessageEvents(postId: Long): Observable<MessageEvent> {
        return CentrifugeManager.getInstance()
            .connectToPostChannel(postId)
            .map { data ->
                val model = gson.fromJson(data, PostMessageEventModel::class.java)
                MessageEventMapper.toMessageEvent(model)
            }
    }

    fun sendPostMessage(postId: Long, text: String?, attachment: LocalFile<*>?): Observable<Message> {
        if (text == null && attachment == null)
            return Observable.error(IllegalArgumentException("at least one of text or attachment must be non-empty"))

        return if (attachment != null) {
            val attachmentReq = UploadFileReq(attachment)
            ApiFactory.apiPostMessageAttachment.uploadPostMedia(attachmentReq)
                .flatMap {
                    val messageReq = PostMessageReq(text, it.requireResult().id)
                    ApiFactory.apiPostMessage.createPostMessage(postId, expand, messageReq)
                }
                .map { MessageMapper.toMessage(it.requireResult()) }
        } else {
            val messageReq = PostMessageReq(text, null)
            ApiFactory.apiPostMessage.createPostMessage(postId, expand, messageReq)
                .map { MessageMapper.toMessage(it.requireResult()) }
        }
    }

    fun updatePostMessage(
        oldMessage: Message.Text,
        text: String?,
        deleteAttachment: Boolean): Observable<Message> {
        return updatePostMessage(oldMessage, text, null, deleteAttachment)
    }

    fun updatePostMessage(
        oldMessage: Message.Text,
        text: String?,
        attachment: LocalFile<*>): Observable<Message> {
        return updatePostMessage(oldMessage, text, attachment, true)
    }

    private fun updatePostMessage(
        oldMessage: Message.Text,
        text: String?,
        attachment: LocalFile<*>?,
        deleteAttachment: Boolean): Observable<Message> {

        if (text == null && attachment == null && (oldMessage.attachment == null || deleteAttachment))
            return Observable.error(IllegalArgumentException("at least one of text or attachment must be non-empty"))

        val messageId = oldMessage.id
        return if (attachment != null) {
            val attachmentReq = UploadFileReq(attachment)
            ApiFactory.apiPostMessageAttachment.uploadPostMedia(attachmentReq)
                .flatMap {
                    val messageReq = PostMessageReq(text, it.requireResult().id)
                    ApiFactory.apiPostMessage.updatePostMessage(messageId, expand, messageReq)
                }
                .map { MessageMapper.toMessage(it.requireResult()) }
        } else {
            val attachmentId = if (deleteAttachment) null else oldMessage.attachment?.id
            val messageReq = PostMessageReq(text, attachmentId)
            ApiFactory.apiPostMessage.updatePostMessage(messageId, expand, messageReq)
                .map { MessageMapper.toMessage(it.requireResult()) }
        }
    }

    fun deletePostMessage(messageId: Long): Completable {
        return ApiFactory.apiPostMessage
            .deleteMessage(messageId)
    }
}