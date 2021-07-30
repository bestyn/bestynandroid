package com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageAuthor
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackListener
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import com.gbksoft.neighbourhood.ui.widgets.chat.message.view.*
import com.gbksoft.neighbourhood.ui.widgets.chat.speech.TextToSpeechManager
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


class MessageAdapter(
        context: Context,
        currentProfileId: Long
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AudioPlaybackListener {

    private val context = context.applicationContext
    private val audioDurationJobs = CompositeDisposable()

    var onIncomingMessageLongClickListener: ((Int, Message) -> Unit)? = null
    var onOutcomingMessageLongClickListener: ((Int, Message) -> Unit)? = null
    var onFileAttachmentDownloadCompleteListener: (() -> Unit)? = null
    var onAttachmentClickListener: ((DownloadProgressCallback, Attachment) -> Unit)? = null
    var onAuthorClickListener: ((MessageAuthor, isOutcoming: Boolean) -> Unit)? = null
    var onMentionClickListener: ((Long) -> Unit)? = null
    var isAuthorVisible = true
    var isTextExpandable = true
    var isTextTextToSpeechEnabled = false
    var onIncomingAudioMessageHeardListener: ((audioMessage: Message.Audio) -> Unit)? = null

    private var textToSpeechManager: TextToSpeechManager? = null
    private var audioPlaybackManager: AudioPlaybackManager? = null
    private var speechingMessageIdSet = mutableSetOf<Long>()
    private var audioMessagesStateMap = mutableMapOf<Long, PlaybackState>()
    private var audioMessagesDuration = mutableMapOf<Long, Long>() //id -> duration

    private val messageViewMap = MessageViewMap()
    private val downloadProgressPool = DownloadProgressPool()

    private var isReverseLayout: Boolean? = null
    private val chatRoomContent = ChatRoomContent(context, currentProfileId)

    init {
        initAudioManagers(context)
    }

    fun setReverseLayout(isReverseLayout: Boolean) {
        this.isReverseLayout = isReverseLayout
        chatRoomContent.isReverseLayout = isReverseLayout
    }

    fun setUnreadMessageHighlighterEnabled(isEnabled: Boolean) {
        chatRoomContent.isUnreadMessageHighlighterEnabled = isEnabled
    }

    override fun getItemViewType(position: Int): Int {
        return chatRoomContent.getItemViewType(position)
    }

    override fun getItemCount(): Int = chatRoomContent.getItemCount()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        fetchIsReverseLayoutFromRecyclerView(recyclerView)
        super.onAttachedToRecyclerView(recyclerView)
    }

    private fun fetchIsReverseLayoutFromRecyclerView(recyclerView: RecyclerView) {
        if (isReverseLayout == null) {
            (recyclerView.layoutManager as? LinearLayoutManager)?.let {
                setReverseLayout(it.reverseLayout)
            }
        }
    }

    fun setData(messages: List<Message>, firstUnreadMessageId: Long? = null) {
        chatRoomContent.setData(messages, firstUnreadMessageId)
        notifyDataSetChanged()
    }

    fun clear() {
        chatRoomContent.clear()
        notifyDataSetChanged()
    }

    fun getMessagePosition(message: Message): Int? {
        return chatRoomContent.getMessagePosition(message)
    }

    fun getFirstUnreadMessagePosition(): Int? {
        return chatRoomContent.getFirstUnreadMessagePosition()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatRoomContent.TYPE_DATE -> {
                DateViewHolder(DateView(parent.context))
            }
            ChatRoomContent.TYPE_UNREAD_MESSAGE_HIGHLIGHTER -> {
                val unreadMsgHighlighterView = UnreadMsgHighlighterView(parent.context)
                unreadMsgHighlighterView.setHorizontalMarginMinus(R.dimen.chat_room_padding)
                UnreadMsgHighlighterViewHolder(unreadMsgHighlighterView)
            }
            ChatRoomContent.TYPE_INCOMING_MESSAGE -> {
                val view = IncomingMessageView(parent.context)
                view.setAuthorVisibility(isAuthorVisible)
                view.onMessageLongClickListener = { pos, msg ->
                    onIncomingMessageLongClickListener?.invoke(pos, msg)
                }
                view.onAttachmentClickListener = { message, attachment ->
                    onAttachmentClickListener?.invoke(createProgressCallback(message.id), attachment)
                }
                view.onAuthorClickListener = { author ->
                    onAuthorClickListener?.invoke(author, false)
                }
                view.onMentionClickListener = { profileId ->
                    onMentionClickListener?.invoke(profileId)
                }
                view.textToSpeechHandler = ::handleTextToSpeechClick
                view.audioPlaybackHandler = ::handlePlayOrStopAudioClick
                view.setTextExpandable(isTextExpandable)
                view.setTextToSpeechEnabled(isTextTextToSpeechEnabled)
                view.onAudioMessageHeardListener = {
                    onIncomingAudioMessageHeardListener?.invoke(it)
                }
                IncomingMessageViewHolder(view)
            }
            ChatRoomContent.TYPE_OUTCOMING_MESSAGE -> {
                val view = OutcomingMessageView(parent.context)
                view.setAuthorVisibility(isAuthorVisible)
                view.onMessageLongClickListener = { pos, msg ->
                    onOutcomingMessageLongClickListener?.invoke(pos, msg)
                }
                view.onAttachmentClickListener = { message, attachment ->
                    onAttachmentClickListener?.invoke(createProgressCallback(message.id), attachment)
                }
                view.onAuthorClickListener = { author ->
                    onAuthorClickListener?.invoke(author, true)
                }
                view.onMentionClickListener = { profileId ->
                    onMentionClickListener?.invoke(profileId)
                }
                view.textToSpeechHandler = ::handleTextToSpeechClick
                view.audioPlaybackHandler = ::handlePlayOrStopAudioClick
                view.setTextExpandable(isTextExpandable)
                view.setTextToSpeechEnabled(isTextTextToSpeechEnabled)
                OutcomingMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unsupported viewType: $viewType")
        }
    }

    private fun initAudioManagers(context: Context) {
        if (textToSpeechManager == null) {
            textToSpeechManager = TextToSpeechManager(
                    context.applicationContext,
                    ::onStartSpeech,
                    ::onEndSpeech,
                    ::onErrorSpeech
            )
            audioPlaybackManager?.let { audioPlaybackManager ->
                textToSpeechManager!!.audioPlaybackManager = audioPlaybackManager
            }
        }

        if (audioPlaybackManager == null) {
            val audioManager = AudioPlaybackManager(context)
            audioManager.audioPlaybackListener = this
            audioManager.textToSpeechManager = textToSpeechManager
            textToSpeechManager?.let { it.audioPlaybackManager = audioManager }
            audioPlaybackManager = audioManager
        }
    }

    private fun onStartSpeech(id: String) {
        val messageId = id.toLong()
        speechingMessageIdSet.add(messageId)
        messageViewMap.get(messageId)?.setTextToSpeechActive(true)
    }

    private fun onEndSpeech(id: String) {
        val messageId = id.toLong()
        speechingMessageIdSet.remove(messageId)
        messageViewMap.get(messageId)?.setTextToSpeechActive(false)
    }

    private fun onErrorSpeech(id: String, throwable: Throwable) {
        onEndSpeech(id)
        ToastUtils.showToastMessage(context, throwable.message)
    }

    private fun handleTextToSpeechClick(text: String, messageId: Long, messageView: MessageView) {
        textToSpeechManager?.speakOrStop(text, messageId.toString())
    }

    fun stopTextToSpeech() {
        textToSpeechManager?.stop()
    }

    fun stopAudioPlayback() {
        audioPlaybackManager?.stop()
    }

    private fun handlePlayOrStopAudioClick(url: String, messageId: Long, messageView: MessageView) {
        audioPlaybackManager?.playOrPauseAudio(url, messageId)
    }

    override fun onAudioPlaybackStateChanged(id: Long, isPlaying: Boolean) {
        messageViewMap.get(id)?.setPlayingState(isPlaying)
        audioMessagesStateMap[id]?.let {
            it.isPlaying = isPlaying
        } ?: run {
            val state = PlaybackState(isPlaying, 0, 0)
            audioMessagesStateMap[id] = state
        }
    }

    override fun onAudioPlaybackProgressChanged(id: Long, totalMs: Long, currentMs: Long) {
        Timber.tag("AudioTag2").d("onAudioPlaybackProgressChanged id: ${id}")
        messageViewMap.get(id)?.setPlayingProgress(totalMs, currentMs)
        audioMessagesStateMap[id]?.let {
            it.totalProgressMs = totalMs
            it.currentProgressMs = currentMs
        }
    }

    override fun onAudioPlaybackStopped(id: Long) {
        messageViewMap.get(id)?.resetPlayingState()
        audioMessagesStateMap.remove(id)
    }

    private fun createProgressCallback(messageId: Long): DownloadProgressCallback {
        return object : DownloadProgressCallback {
            override fun onProgressChanged(total: Int, current: Int) {
                if (downloadProgressPool.setProgress(messageId, total, current)) {
                    onDownloadProgressChanged(messageId, total, current)
                }
                if (total == current) {
                    onFileAttachmentDownloadCompleteListener?.invoke()
                }
            }
        }
    }

    private fun onDownloadProgressChanged(messageId: Long, total: Int, current: Int) {
        messageViewMap.get(messageId)?.onDownloadProgressChanged(total, current)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ChatRoomContent.TYPE_DATE -> {
                holder as DateViewHolder
                holder.dateView.setDate(chatRoomContent.getDate(position))
            }
            ChatRoomContent.TYPE_INCOMING_MESSAGE -> {
                holder as IncomingMessageViewHolder
                val realPos = chatRoomContent.getMessageRealPosition(position)
                val message = chatRoomContent.getIncomingMessage(position)
                holder.incomingMessageView.setMessage(realPos, message, downloadProgressPool.getProgress(message.id))
                holder.incomingMessageView.setTextToSpeechActive(speechingMessageIdSet.contains(message.id))
                putMessageView(message, holder.incomingMessageView)
                if (message is Message.Audio) {
                    setMessageDuration(message)
                    updateAudioPlaybackState(message)
                }
            }
            ChatRoomContent.TYPE_OUTCOMING_MESSAGE -> {
                holder as OutcomingMessageViewHolder
                val realPos = chatRoomContent.getMessageRealPosition(position)
                val message = chatRoomContent.getOutcomingMessage(position)
                holder.outcomingMessageView.setMessage(realPos, message, downloadProgressPool.getProgress(message.id))
                holder.outcomingMessageView.setTextToSpeechActive(speechingMessageIdSet.contains(message.id))
                putMessageView(message, holder.outcomingMessageView)
                if (message is Message.Audio) {
                    setMessageDuration(message)
                    updateAudioPlaybackState(message)
                }
            }
        }
    }

    private fun putMessageView(message: Message, messageView: MessageView) {
        messageViewMap.put(message.id, messageView)
    }

    private fun setMessageDuration(message: Message.Audio) {
        Timber.tag("DurationTag").d("setMessageDuration")
        val duration = audioMessagesDuration[message.id]
        Timber.tag("DurationTag").d("duration = $duration")
        if (duration != null) {
            messageViewMap.get(message.id)?.setAudioMessageDuration(duration)
        } else {
            audioDurationJobs.add(Single.fromCallable {
                return@fromCallable fetchDuration(message.attachment.originUrl)
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        audioMessagesDuration[message.id] = it
                        messageViewMap.get(message.id)?.setAudioMessageDuration(it)
                    }, {
                        it.printStackTrace()
                    })
            )
        }
    }

    private fun fetchDuration(originUrl: String): Long {
        audioPlaybackManager?.textToSpeechManager
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(originUrl, HashMap())
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr.toLong()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        audioDurationJobs.dispose()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun updateAudioPlaybackState(message: Message.Audio) {
        audioMessagesStateMap[message.id]?.let { state ->
            messageViewMap.get(message.id)?.let { messageView ->
                messageView.setPlayingState(state.isPlaying)
                messageView.setPlayingProgress(state.totalProgressMs, state.currentProgressMs)
            }
        }
    }

    fun findMessagesBetween(firstPosition: Int, lastPosition: Int): MutableList<Message> {
        return chatRoomContent.getMessagesBetween(firstPosition, lastPosition)
    }

    private inner class PlaybackState(
            var isPlaying: Boolean,
            var totalProgressMs: Long,
            var currentProgressMs: Long
    )

}