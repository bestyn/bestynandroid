package com.gbksoft.neighbourhood.ui.fragments.chat.room

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.models.response.chat.TypingStateModel
import com.gbksoft.neighbourhood.data.repositories.PrivateChatRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageEvent
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.fragments.base.chat.ValidationDelegate
import com.gbksoft.neighbourhood.ui.fragments.post_details.MessageList
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.DownloadProgressCallback
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.download.AppDownloader
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.net.HttpURLConnection

class ChatRoomViewModel(
    private val context: Context,
    private val privateChatRepository: PrivateChatRepository
) : BaseViewModel() {
    private val paginationBuffer = Constants.PRIVATE_CHAT_PAGINATION_BUFFER
    private val validationDelegate = ValidationDelegate(validationUtils, context)
    private var isMessagesLoading: Boolean = false
    private var conversationId: Long? = null
    private var opponentId: Long? = null
    private val messageList = MessageList()
    private var isSubscribedToMessages = false
    private var lastLoadedMessagesCount = 0
    private var editingTextMessage: Message.Text? = null

    private val _conversationRead = SingleLiveEvent<Long>()
    val conversationRead = _conversationRead as LiveData<Long>

    private val _typingState = SingleLiveEvent<Boolean>()
    val typingState = _typingState as LiveData<Boolean>

    private val messagesData = MessagesData(listOf())
    private val _messages = MutableLiveData<MessagesData>()
    val messages = _messages as LiveData<MessagesData>

    private val _isOpponentOnline = MutableLiveData<Boolean>()
    val isOpponentOnline = _isOpponentOnline as LiveData<Boolean>

    private val _messageAttachment = SingleLiveEvent<Attachment?>()
    val messageAttachment = _messageAttachment as LiveData<Attachment?>

    private val _messageSendingProcess = MutableLiveData<Boolean>()
    val messageSendingProcess = _messageSendingProcess as LiveData<Boolean>

    private val _clearMessageForm = SingleLiveEvent<Boolean>()
    val clearMessageForm = _clearMessageForm as LiveData<Boolean>

    private val appDownloader by lazy { AppDownloader(context) }

    /** LocalFile<Int> type is one of [Attachment] TYPE_.. */
    private var messageAttachmentFile: LocalFile<Int>? = null

    fun getCurrentProfileId(): Long {
        return sharedStorage.getCurrentProfile()?.id
            ?: throw NullPointerException("currentProfile = null")
    }

    fun checkIsOpponentOnline(opponentId: Long) {
        addDisposable("checkIsOpponentOnline", privateChatRepository.checkIsProfilesOnline(listOf(opponentId))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                resp.result?.onlineProfileIds?.let {
                    _isOpponentOnline.value = it.contains(opponentId)
                }
            }, { handleError(it) }))
    }

    fun loadChat(conversationId: Long?, opponentId: Long) {
        this.opponentId = opponentId
        getConversationIdByOpponent(opponentId)
    }

    private fun getConversationIdByOpponent(opponentId: Long) {
        addDisposable("getConversationIdByOpponent", privateChatRepository
            .getConversationByOpponentId(opponentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                this.conversationId = it.id
                messagesData.firstUnreadMessageId = it.firstUnreadMessageId
                loadMessages(it.id, null, it.unreadMessages)
            }, {
                handleConversationByOpponentError(it)
            }))
    }

    private fun loadMessages(conversationId: Long, lastMessageId: Long? = null, unreadMsgCount: Int = 0) {
        isMessagesLoading = true
        addDisposable("loadMessages", privateChatRepository
            .loadChatHistory(conversationId, lastMessageId, unreadMsgCount)
            .doOnTerminate { isMessagesLoading = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onNextMessagesLoaded(it)
                checkMessageSubscription()
            }, { handleError(it) }))
    }

    private fun onNextMessagesLoaded(messages: List<Message>) {
        lastLoadedMessagesCount = messages.size
        messageList.add(messages)
        messagesData.messages = messageList.asList()
        _messages.value = messagesData
    }

    fun markAudioMessagesAsHeard(audioMessage: Message.Audio) {
        addDisposable("markAudioMessagesAsHeard", privateChatRepository
            .markAudioMessageAsHeard(audioMessage.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, { handleError(it) }))
    }

    private fun handleConversationByOpponentError(t: Throwable) {
        if (t is HttpException && t.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            //chat not exists yet
        } else {
            handleError(t)
        }
    }

    private fun checkMessageSubscription() {
        val opponentId = opponentId ?: return
        if (isSubscribedToMessages) return
        isSubscribedToMessages = true
        addDisposable("MessageSubscription", privateChatRepository
            .subscribeToChatActions(opponentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event ->
                onMessageListEvent(event)
            }, {
                it.printStackTrace()
                isSubscribedToMessages = false
            }))
    }

    private fun onMessageListEvent(event: MessageEvent) {
        when (event.eventType) {
            MessageEvent.CREATE -> onNewMessage(event.message)
            MessageEvent.UPDATE -> onMessageUpdated(event.message)
            MessageEvent.DELETE -> onMessageDeleted(event.messageId)
        }
    }

    fun onLastVisibleMessageChanged(message: Message) {
        val conversationId = conversationId ?: return
        val position = messageList.getMessagePosition(message) ?: return
        if (isMessagesLoading) return
        if (lastLoadedMessagesCount == 0) return
        val needLoadMore: Boolean = position + paginationBuffer > messageList.size()
        if (!needLoadMore) return
        loadMessages(conversationId, messageList.last().id)
    }

    fun addMessageLocalAttachment(localFile: LocalFile<Int>) {
        val attachmentType = localFile.type ?: return

        val errorFieldsModel = ErrorFieldsModel()
        when (attachmentType) {
            Attachment.TYPE_PICTURE -> {
            }
            Attachment.TYPE_VIDEO -> {
                validationDelegate.validateVideoFileSize(errorFieldsModel, localFile.size.toInt())
            }
            Attachment.TYPE_FILE -> {
                validationDelegate.validateFileSize(errorFieldsModel, localFile.size.toInt())
            }
        }

        if (!errorFieldsModel.isValid) {
            val error = errorFieldsModel.errorsMap[ValidationField.ATTACHMENT_FILE]
            ToastUtils.showToastMessage(error)
            return
        }

        _messageAttachment.value = Attachment(0, attachmentType,
            localFile.name, "", "")
        messageAttachmentFile = localFile
    }

    fun removeMessageAttachment() {
        _messageAttachment.value = null
        messageAttachmentFile = null
    }

    fun sendTextMessage(text: String?) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validatePrivateMessageTextMaxLength(errorFieldsModel, text)
        if (!errorFieldsModel.isValid) {
            val error = errorFieldsModel.errorsMap[ValidationField.PRIVATE_MESSAGE]
            ToastUtils.showToastMessage(error)
            return
        }

        val opponentId = opponentId ?: return
        onMessageSendingStart()
        addDisposable("sendPostMessage", privateChatRepository
            .sendTextMessage(opponentId, text, messageAttachmentFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onMessageSendingFinish() }
            .subscribe({
                Analytics.onSentDirectMessage()
                onNewMessage(it)
                clearMessageForm()
                checkMessageSubscription()
            }, {
                it.printStackTrace()
                handleError(it)
            }))
    }

    private fun onNewMessage(message: Message) {
        newUnreadMessages.add(message.id)
        messageList.addSingle(message)
        messagesData.messages = messageList.asList()
        _messages.value = messagesData
    }

    private val markedAsReadMessageIds = mutableListOf<Long>()
    private val readMessageIds = mutableListOf<Long>()
    private val newUnreadMessages = mutableListOf<Long>()
    fun markChatMessagesAsRead(messages: List<Message>) {
        readMessageIds.clear()
        val myId: Long = getCurrentProfileId()
        for (msg in messages) {
            if (msg.author.id == myId) continue
            val isRead = msg.isRead ?: true
            val isUnread = !isRead || newUnreadMessages.contains(msg.id)
            if (isUnread && !markedAsReadMessageIds.contains(msg.id)) {
                readMessageIds.add(msg.id)
            }
        }
        if (readMessageIds.isEmpty()) return
        markedAsReadMessageIds.addAll(readMessageIds)
        addDisposable("markChatMessagesAsRead", privateChatRepository
            .markConversationMessagesAsRead(readMessageIds)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _conversationRead.value = conversationId
            }, { handleError(it) }))
    }

    fun updateTextMessage(oldMessage: Message.Text, text: String?, attachment: Attachment?) {
        val errorFieldsModel = ErrorFieldsModel()
        validationDelegate.validatePrivateMessageTextMaxLength(errorFieldsModel, text)
        if (!errorFieldsModel.isValid) {
            val error = errorFieldsModel.errorsMap[ValidationField.PRIVATE_MESSAGE]
            ToastUtils.showToastMessage(error)
            return
        }

        if (oldMessage.text == text && oldMessage.attachment == attachment) {
            clearMessageForm()
            return
        }

        val updateRequest = messageAttachmentFile?.let {
            privateChatRepository.updateTextMessage(oldMessage, text, it)
        } ?: run {
            val deleteAttachment = oldMessage.attachment != null && attachment == null
            privateChatRepository.updateTextMessage(oldMessage, text, deleteAttachment)
        }
        onMessageSendingStart()
        addDisposable("updatePostTextMessage", updateRequest
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onMessageSendingFinish() }
            .subscribe({
                onMessageUpdated(it)
                clearMessageForm()
            }, {
                it.printStackTrace()
                handleError(it)
            }))
    }

    private fun onMessageUpdated(message: Message) {
        messageList.update(message)
        messagesData.messages = messageList.asList()
        _messages.value = messagesData
    }

    fun deleteMessage(message: Message) {
        addDisposable("deleteMessage_${message.id}", privateChatRepository
            .deletePostMessage(message.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onMessageDeleted(message.id)
            }, {
                handleError(it)
            }))
    }

    private fun onMessageDeleted(messageId: Long) {
        editingTextMessage?.let { if (it.id == messageId) clearMessageForm() }
        if (messageList.delete(messageId)) {
            messagesData.messages = messageList.asList()
            _messages.value = messagesData
        }
    }

    fun sendAudioMessage(localFile: LocalFile<Int>) {
        val opponentId = opponentId ?: return
        addDisposable("sendAudioMessage", privateChatRepository
            .sendAudioMessage(opponentId, localFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { onMessageSendingFinish() }
            .subscribe({
                Analytics.onSentDirectMessage()
                onNewMessage(it)
                clearMessageForm()
                checkMessageSubscription()
            }, {
                it.printStackTrace()
                handleError(it)
            }))
    }

    private fun onMessageSendingStart() {
        showLoader()
        _messageSendingProcess.value = true
    }

    private fun onMessageSendingFinish() {
        hideLoader()
        _messageSendingProcess.value = false
    }

    fun setEditingTextMessage(textMessage: Message.Text) {
        editingTextMessage = textMessage
    }

    private fun clearMessageForm() {
        editingTextMessage = null
        messageAttachmentFile = null
        _clearMessageForm.value = true
    }

    fun downloadFile(progressCallback: DownloadProgressCallback, attachment: Attachment) {
        val downloadId = appDownloader.download(attachment.originUrl, attachment.title)
        appDownloader.observeDownloadProgress(downloadId) { total, current ->
            progressCallback.onProgressChanged(total, current)
        }
    }

    fun sendTypingState(isTyping: Boolean) {
        val opponentId = opponentId ?: return
        privateChatRepository.sendTypingState(TypingStateModel(isTyping, getCurrentProfileId()), opponentId)
    }

    fun connectToConversationsChannel(opponentId: Long) {
        privateChatRepository.connectToConversationChannel(getCurrentProfileId(), opponentId)
    }

    fun subscribeToConversationActions() {
        addDisposable("conversationsChannelSubscribtion", privateChatRepository.subscribeToConversationActions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.profileId != getCurrentProfileId()) {
                    if (it.isTyping) {
                        _isOpponentOnline.value = true
                    }
                    _typingState.value = it.isTyping
                }
            })
    }

    private fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        hideLoader()
    }
}