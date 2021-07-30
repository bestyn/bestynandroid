package com.gbksoft.neighbourhood.ui.widgets.chat.message.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnLongClickListener
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutIncomingMessageBinding
import com.gbksoft.neighbourhood.model.chat.Message
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class IncomingMessageView : MessageView {
    private lateinit var layout: LayoutIncomingMessageBinding
    private lateinit var contentDelegate: MessageContentDelegate
    private var isAuthorVisible = true
    var onAudioMessageHeardListener: ((audioMessage: Message.Audio) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val radius = resources.getDimensionPixelSize(R.dimen.chat_media_attachment_corner)
        val withTextTransformation = RoundedCornersTransformation(radius, 0,
                RoundedCornersTransformation.CornerType.BOTTOM)
        val withoutTextTransformation = RoundedCornersTransformation(radius, 0,
                RoundedCornersTransformation.CornerType.ALL)
        val withTextRequestOptions = RequestOptions().transform(CenterCrop(), withTextTransformation)
        val withoutTextRequestOptions = RequestOptions().transform(CenterCrop(), withoutTextTransformation)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layout = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.layout_incoming_message, this, true)
        val longClickListener = OnLongClickListener {
            val position = messagePosition
            val message = message
            if (position != null && message != null) {
                onMessageLongClickListener?.invoke(position, message)
            }
            true
        }
        layout.contentBackground.setOnLongClickListener(longClickListener)
        layout.tvText.setOnLongClickListener(longClickListener)
        layout.ivMediaPreview.setOnLongClickListener(longClickListener)
        layout.ivPlay.setOnLongClickListener(longClickListener)
        layout.ivDownloadFile.setOnLongClickListener(longClickListener)
        layout.fileAttachment.setOnLongClickListener(longClickListener)
        contentDelegate = createContentManager(withTextRequestOptions, withoutTextRequestOptions)
        setClickListeners(contentDelegate)
    }

    private fun createContentManager(
            withTextRequestOptions: RequestOptions,
            withoutTextRequestOptions: RequestOptions
    ) = MessageContentDelegate(
            context,
            MessageContentDelegate.AuthorGroup(
                    layout.avatar,
                    layout.avatarHelper,
                    layout.tvAuthor),
            MessageContentDelegate.TextGroup(
                    layout.textContentTime,
                    layout.tvTime,
                    layout.tvEdited,
                    layout.tvEditedDot,
                    layout.ivTextToSpeech,
                    layout.tvText,
                    layout.contentTopPadding),
            MessageContentDelegate.MediaGroup(
                    layout.mediaAttachment,
                    layout.ivMediaPreview,
                    layout.ivPlay,
                    layout.tvMediaTime,
                    layout.tvMediaEdited,
                    layout.tvMediaEditedDot),
            MessageContentDelegate.FileGroup(
                    layout.fileAttachment,
                    layout.tvFileName,
                    layout.fileAttachmentDivider,
                    layout.ivDownloadFile,
                    layout.tvFileTime,
                    layout.tvFileEdited,
                    layout.tvFileEditedDot,
                    layout.ivFileTextToSpeech
            ),
            MessageContentDelegate.AudioGroup(
                    layout.audioAttachment,
                    layout.ivPlayAudio,
                    layout.tvAudioDuration,
                    layout.ivPlayedLevels,
                    layout.tvAudioTime,
                    layout.ivIsHeard,
                    null
            ),
            withTextRequestOptions,
            withoutTextRequestOptions,
            R.drawable.ic_incoming_text_to_speech_active,
            R.drawable.ic_incoming_text_to_speech,
            R.drawable.ic_play_audio,
            R.drawable.ic_pause_audio
    )

    private fun setClickListeners(contentDelegate: MessageContentDelegate) {
        contentDelegate.onAttachmentClick = {
            message?.let { message ->
                when (message) {
                    is Message.Text -> {
                        if (message.attachment != null) {
                            onAttachmentClickListener?.invoke(message, message.attachment)
                        }
                    }
                }
            }
        }
        contentDelegate.onAuthorClick = {
            message?.let { message ->
                onAuthorClickListener?.invoke(message.author)
            }
        }
        contentDelegate.onTextToSpeechClick = {
            message?.let { message ->
                if (message is Message.Text) {
                    textToSpeechHandler?.invoke(message.text, message.id, this)
                }
            }
        }
        contentDelegate.onPlayStopAudioClick = {
            message?.let { message ->
                if (message is Message.Audio) {
                    audioPlaybackHandler?.invoke(message.attachment.originUrl, message.id, this)
                    if (message.isHeard.not()) {
                        message.isHeard = true
                        contentDelegate.setAudioMessageHeard()
                        onAudioMessageHeardListener?.invoke(message)
                    }
                }
            }
        }
        contentDelegate.onMentionClick = {
            onMentionClickListener?.invoke(it)
        }
    }

    override fun provideDownloadProgressBar(): ProgressBar = layout.progressDownloadFile

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateContentMaxWidth()
        }
    }

    private var lastContentMaxWidth = 0
    private fun updateContentMaxWidth() {
        val maxWidth = (layout.contentRightLimiter.x - layout.contentLeftLimiter.x).toInt()
        if (lastContentMaxWidth == maxWidth) return
        lastContentMaxWidth = maxWidth

        post {
            layout.tvAuthor.maxWidth = maxWidth
            layout.tvText.maxWidth = maxWidth
            layout.tvFileName.maxWidth = (maxWidth * 0.66).toInt()
            layout.tvAuthor.invalidate()
            layout.tvText.invalidate()
            layout.tvFileName.invalidate()
        }
    }

    override fun setupView(message: Message) {
        if (isAuthorVisible) {
            contentDelegate.showAuthor(message.author)
        }
        when (message) {
            is Message.Text -> {
                contentDelegate.setupTextMessage(message)
            }
            is Message.Audio -> {
                contentDelegate.setupAudioMessage(message)
            }
        }

        layout.tvText.onMentionClickListener
    }

    override fun setAuthorVisibility(isVisible: Boolean) {
        isAuthorVisible = isVisible
        contentDelegate.setAuthorVisibility(isVisible)
    }

    override fun setTextExpandable(isExpandable: Boolean) {
        contentDelegate.isTextExpandable = isExpandable
    }

    override fun setTextToSpeechEnabled(isEnabled: Boolean) {
        contentDelegate.isTextToSpeechEnabled = isEnabled
    }

    override fun setTextToSpeechActive(isActive: Boolean) {
        contentDelegate.setTextToSpeechActive(isActive)
    }

    override fun setAudioMessageDuration(duration: Long) {
        contentDelegate.setAudioMessageDuration(duration)
    }

    override fun setPlayingState(isPlaying: Boolean) {
        contentDelegate.setPlayingState(isPlaying)
    }

    override fun setPlayingProgress(totalMs: Long, currentMs: Long) {
        contentDelegate.setPlayingProgress(totalMs, currentMs)
    }

    override fun resetPlayingState() {
        contentDelegate.resetPlayingState()
    }

}