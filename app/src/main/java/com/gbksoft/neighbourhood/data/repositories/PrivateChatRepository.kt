package com.gbksoft.neighbourhood.data.repositories

import android.annotation.SuppressLint
import com.gbksoft.neighbourhood.data.centrifuge.CentrifugeManager
import com.gbksoft.neighbourhood.data.local.dao.AudioMessageHeardStatusDao
import com.gbksoft.neighbourhood.data.local.entity.AudioMessageHeardStatusEntity
import com.gbksoft.neighbourhood.data.models.request.post_chat.PostMessageReq
import com.gbksoft.neighbourhood.data.models.request.post_chat.UploadFileReq
import com.gbksoft.neighbourhood.data.models.request.private_chat.MessageIds
import com.gbksoft.neighbourhood.data.models.request.private_chat.PrivateMessageReq
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.chat.*
import com.gbksoft.neighbourhood.data.network.api.ApiProfileMessage
import com.gbksoft.neighbourhood.data.network.api.ApiProfileMessageAttachment
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.chat.ConversationMapper
import com.gbksoft.neighbourhood.mappers.chat.MessageEventMapper
import com.gbksoft.neighbourhood.mappers.chat.MessageMapper
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Conversation
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageEvent
import com.gbksoft.neighbourhood.utils.Constants
import com.google.gson.Gson
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

@SuppressLint("CheckResult")
class PrivateChatRepository(
    profileRepository: ProfileRepository,
    private val apiProfileMessage: ApiProfileMessage,
    private val apiProfileMessageAttachment: ApiProfileMessageAttachment,
    private val audioMessageHeardStatusDao: AudioMessageHeardStatusDao
) : BaseRepository() {
    private val conversationExpand = "profile.avatar.formatted,lastMessage.attachment.formatted,profile.isOnline,firstUnreadMessageId"
    private val messageExpand = "senderProfile.avatar.formatted,recipientProfile.avatar.formatted,attachment.formatted"
    private val paginationDirection = "prev" //"next"
    private val profilesMessagesSubject = PublishSubject.create<MessageEvent>()
    private val typingStateSubject = PublishSubject.create<TypingStateModel>()
    private var currentUserId: Long = -1
    private var chatsChannelDisposable: Disposable? = null
    private var conversationsChannelDisposable: Disposable? = null
    private val gson = Gson()
    private val pagingHelper = PagingHelper<ConversationModel, Conversation> {
        ConversationMapper.toConversation(it)
    }

    init {
        profileRepository.subscribeCurrentUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { userModel ->
                if (userModel.userId != currentUserId) {
                    currentUserId = userModel.userId
                    connectToCentrifuge(userModel.userId)
                }
            }
    }

    private fun connectToCentrifuge(currentUserId: Long) {
        chatsChannelDisposable?.dispose()
        chatsChannelDisposable = CentrifugeManager.getInstance().connectToChatsChannel(currentUserId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { data ->
                try {
                    val model = gson.fromJson(data, ChatMessageEventModel::class.java)
                    MessageEventMapper.toMessageEvent(model)
                } catch (t: Throwable) {
                    t.printStackTrace()
                    throw t
                }
            }
            .subscribe { event ->
                profilesMessagesSubject.onNext(event)
            }
    }

    fun subscribeToChatActions(): Flowable<MessageEvent> {
        return profilesMessagesSubject.toFlowable(BackpressureStrategy.LATEST)
    }

    fun subscribeToChatActions(opponentId: Long): Flowable<MessageEvent> {
        return subscribeToChatActions()
            .filter { event ->
                event.message.author.id == opponentId || event.message.recipient?.id == opponentId
            }
    }

    fun sendTypingState(typingStateModel: TypingStateModel, opponentId: Long) {
        val model = TypingStateEventModel().apply { message = typingStateModel }
        val data = gson.toJson(model)
        CentrifugeManager.getInstance()
                .sendToConversationsChannel(listOf(typingStateModel.profileId, opponentId), data)
    }

    fun getConversationList(searchQuery: String?, currentPage: Int?, perPage: Int = Constants.PER_PAGE)
        : Observable<Paging<List<Conversation>>> {
        val search = if (searchQuery != null) {
            if (searchQuery.length >= Constants.CHAT_SEARCH_QUERY_MIN_LENGTH) searchQuery else null
        } else {
            null
        }

        val page = if (currentPage != null) currentPage + 1 else 0

        return apiProfileMessage
            .getConversationList(search, conversationExpand, perPage, page)
            .map { pagingHelper.getPagingResult(it) }
    }

    fun getConversationByOpponentId(opponentId: Long): Observable<Conversation> {
        return apiProfileMessage
            .getConversationByOpponentId(opponentId, conversationExpand)
            .map { ConversationMapper.toConversation(it.requireResult()) }
    }

    fun loadChatHistory(conversationId: Long,
                        lastMessageId: Long? = null,
                        unreadMsgCount: Int = 0): Observable<List<Message>> {
        return apiProfileMessage
            .getConversationMessagesByConversationId(conversationId, messageExpand,
                lastMessageId, paginationDirection, unreadMsgCount + Constants.PER_PAGE)
            .map { MessageMapper.fromChatMessageModels(it.requireResult()) }
            .map { addHeardStatusToAudioMessages(it) }
    }

    fun connectToConversationChannel(vararg profileIds: Long) {
        conversationsChannelDisposable?.dispose()
        conversationsChannelDisposable = CentrifugeManager.getInstance()
            .connectToConversationChannel(*profileIds)
                .map { data ->
                    try {
                        val model = gson.fromJson(data, TypingStateEventModel::class.java)
                        model
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        throw t
                    }
                }
                .subscribe { event ->
                    typingStateSubject.onNext(event.message)
                }
    }

    fun subscribeToConversationActions(): Flowable<TypingStateModel> {
        return typingStateSubject.toFlowable(BackpressureStrategy.DROP)
    }

    private fun addHeardStatusToAudioMessages(messages: MutableList<Message>): MutableList<Message> {
        val audioMessages = messages.filterIsInstance<Message.Audio>()
        val audioIds = audioMessages.map { it.id }
        val statuses = audioMessageHeardStatusDao.getStatuses(audioIds)
        for (status in statuses) {
            val audioMessage = audioMessages.find { it.id == status.id }
            audioMessage?.isHeard = status.status == 1
        }
        return messages
    }

    fun markConversationMessagesAsRead(messageIds: List<Long>): Completable {
        val body = MessageIds(messageIds.toLongArray())
        return apiProfileMessage.markMessagesAsRead(body)
    }

    fun markAudioMessageAsHeard(audioMessageId: Long): Completable {
        return audioMessageHeardStatusDao
            .setStatus(AudioMessageHeardStatusEntity(audioMessageId, 1))
    }

    fun sendTextMessage(opponentId: Long, text: String?, attachment: LocalFile<Int>?): Observable<Message> {
        if (text == null && attachment == null)
            return Observable.error(IllegalArgumentException("at least one of text or attachment must be non-empty"))

        return if (attachment != null) {
            val attachmentReq = UploadFileReq(attachment, MessageMapper.getTypeName(attachment.type))
            apiProfileMessageAttachment.uploadChatMedia(attachmentReq)
                .flatMap {
                    val messageReq = PrivateMessageReq(opponentId, text, it.requireResult().id)
                    apiProfileMessage.postMessage(messageExpand, messageReq)
                }
                .map { MessageMapper.toMessage(it.requireResult()) }
        } else {
            val messageReq = PrivateMessageReq(opponentId, text, null)
            apiProfileMessage.postMessage(messageExpand, messageReq)
                .map { MessageMapper.toMessage(it.requireResult()) }
        }
    }

    fun updateTextMessage(
        oldMessage: Message.Text,
        text: String?,
        deleteAttachment: Boolean): Observable<Message> {
        return updateTextMessage(oldMessage, text, null, deleteAttachment)
    }

    fun updateTextMessage(
        oldMessage: Message.Text,
        text: String?,
        attachment: LocalFile<*>): Observable<Message> {
        return updateTextMessage(oldMessage, text, attachment, true)
    }

    private fun updateTextMessage(
        oldMessage: Message.Text,
        text: String?,
        attachment: LocalFile<*>?,
        deleteAttachment: Boolean): Observable<Message> {

        if (text == null && attachment == null && (oldMessage.attachment == null || deleteAttachment))
            return Observable.error(IllegalArgumentException("at least one of text or attachment must be non-empty"))

        val messageId = oldMessage.id
        return if (attachment != null) {
            val attachmentReq = UploadFileReq(attachment)
            apiProfileMessageAttachment.uploadChatMedia(attachmentReq)
                .flatMap {
                    val messageReq = PostMessageReq(text, it.requireResult().id)
                    apiProfileMessage.updateMessage(messageId, messageExpand, messageReq)
                }
                .map { MessageMapper.toMessage(it.requireResult()) }
        } else {
            val attachmentId = if (deleteAttachment) null else oldMessage.attachment?.id
            val messageReq = PostMessageReq(text, attachmentId)
            apiProfileMessage.updateMessage(messageId, messageExpand, messageReq)
                .map { MessageMapper.toMessage(it.requireResult()) }
        }
    }

    fun deletePostMessage(messageId: Long): Completable {
        return apiProfileMessage
            .deleteMessage(messageId)
    }

    fun sendAudioMessage(opponentId: Long, audioFile: LocalFile<Int>): Observable<Message> {
        val attachmentReq = UploadFileReq(audioFile, MessageMapper.getTypeName(audioFile.type))
        return apiProfileMessageAttachment.uploadChatMedia(attachmentReq)
            .flatMap {
                val messageReq = PrivateMessageReq(opponentId, null, it.requireResult().id)
                apiProfileMessage.postMessage(messageExpand, messageReq)
            }
            .map { MessageMapper.toMessage(it.requireResult()) }
    }

    fun archiveConversation(conversationId: Long): Completable {
        return apiProfileMessage.archiveConversation(conversationId)
    }

    fun checkIsProfilesOnline(ids: List<Long>): Observable<BaseResponse<OnlineProfilesModel>> {
        return  apiProfileMessage.checkIsProfilesOnline(ids)
    }
}