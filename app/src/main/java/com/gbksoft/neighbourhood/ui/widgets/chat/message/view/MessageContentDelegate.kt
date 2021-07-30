package com.gbksoft.neighbourhood.ui.widgets.chat.message.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageAuthor
import com.gbksoft.neighbourhood.ui.widgets.avatar.AvatarView
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import com.gbksoft.neighbourhood.ui.widgets.expandable_text.ExpandableTextView
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider

class MessageContentDelegate(
        private val context: Context,
        private val authorGroup: AuthorGroup,
        private val textGroup: TextGroup,
        private val mediaGroup: MediaGroup,
        private val fileGroup: FileGroup,
        private val audioGroup: AudioGroup,
        private val withTextRequestOptions: RequestOptions,
        private val withoutTextRequestOptions: RequestOptions,
        @DrawableRes
        textToSpeechActiveIcon: Int,
        @DrawableRes
        textToSpeechInactiveIcon: Int,
        @DrawableRes
        playAudioIcon: Int,
        @DrawableRes
        pauseAudioIcon: Int
) {
    private val textToSpeechActiveIcon = AppCompatResources.getDrawable(context, textToSpeechActiveIcon)
    private val textToSpeechInactiveIcon = AppCompatResources.getDrawable(context, textToSpeechInactiveIcon)

    private val playAudioIcon = AppCompatResources.getDrawable(context, playAudioIcon)
    private val pauseAudioIcon = AppCompatResources.getDrawable(context, pauseAudioIcon)

    internal var onAttachmentClick: (() -> Unit)? = null
    internal var onAuthorClick: (() -> Unit)? = null
    internal var onTextToSpeechClick: (() -> Unit)? = null
    internal var isTextExpandable: Boolean = true
    internal var isTextToSpeechEnabled: Boolean = true
    internal var onPlayStopAudioClick: (() -> Unit)? = null
    internal var onMentionClick: ((Long) -> Unit)? = null

    private val audioProgressAnimDuration = AudioPlaybackManager.PROGRESS_UPDATE_PERIOD / 2
    private val resetPlayingStateDelay = AudioPlaybackManager.PROGRESS_UPDATE_PERIOD
    private var audioProgressAnimator: ObjectAnimator? = null

    private var audioMessageDuration: Long? = null
    private var isAudioMessagePlayingProgress = false

    @DrawableRes
    private val readStatusIcon = R.drawable.ic_message_status_read

    @DrawableRes
    private val sentStatusIcon = R.drawable.ic_message_status_sent

    init {
        mediaGroup.preview.setOnClickListener { onAttachmentClick?.invoke() }
        mediaGroup.playVideoButton.setOnClickListener { onAttachmentClick?.invoke() }
        fileGroup.downloadButton.setOnClickListener { onAttachmentClick?.invoke() }
        authorGroup.avatar.setOnClickListener { onAuthorClick?.invoke() }
        authorGroup.name.setOnClickListener { onAuthorClick?.invoke() }
        textGroup.textToSpeech.setOnClickListener { onTextToSpeechClick?.invoke() }
        fileGroup.textToSpeech.setOnClickListener { onTextToSpeechClick?.invoke() }
        audioGroup.playButton.setOnClickListener { onPlayStopAudioClick?.invoke() }
        textGroup.content.onMentionClickListener = { onMentionClick?.invoke(it) }
    }


    fun showAuthor(author: MessageAuthor) {
        authorGroup.avatar.setFullName(author.fullName)
        authorGroup.avatar.setImage(author.avatar)
        authorGroup.avatar.setBusiness(author.isBusiness)
        authorGroup.name.text = author.fullName
    }

    fun setupTextMessage(textMessage: Message.Text) {
        hideViews(audioGroup.layout)
        if (textMessage.text.isEmpty()) {
            textGroup.content.visibility = View.GONE
            if (authorGroup.name.visibility == View.GONE) {
                textGroup.contentTopPadding.visibility = View.GONE
            }
        } else {
            textGroup.content.visibility = View.VISIBLE
            textGroup.contentTopPadding.visibility = View.VISIBLE
            setText(textMessage.text)
        }
        setupTextToSpeech(textMessage.text)
        if (textMessage.attachment == null) {
            setupWithText(textMessage)
        } else {
            setupWithAttachment(textMessage, textMessage.attachment)
        }
    }

    private fun setText(text: String) {
        if (isTextExpandable) {
            textGroup.content.setCollapsedText(text)
        } else {
            textGroup.content.setExpandedText(text)
        }
    }

    private fun setupTextToSpeech(text: String) {
        if (isTextToSpeechEnabled && text.isNotBlank()) {
            textGroup.textToSpeech.visibility = View.VISIBLE
            fileGroup.textToSpeech.visibility = View.VISIBLE
        } else {
            textGroup.textToSpeech.visibility = View.GONE
            fileGroup.textToSpeech.visibility = View.GONE
        }
    }

    fun setTextToSpeechActive(isActive: Boolean) {
        if (!isTextToSpeechEnabled) return

        val icon = if (isActive) {
            textToSpeechActiveIcon
        } else {
            textToSpeechInactiveIcon
        }
        textGroup.textToSpeech.setImageDrawable(icon)
        fileGroup.textToSpeech.setImageDrawable(icon)
    }

    private fun setupWithText(textMessage: Message.Text) {
        hideViews(mediaGroup.layout, fileGroup.layout, fileGroup.divider, fileGroup.downloadButton)
        showViews(textGroup.timeLayout)
        if (textMessage.isEdited()) {
            showViews(textGroup.edited, textGroup.editedDot)
        } else {
            hideViews(textGroup.edited, textGroup.editedDot)
        }
        textGroup.time.text = DateTimeUtils.getTime(textMessage.createdAt)
        resolveMessageStatus(textMessage.isRead, textGroup.status)
    }

    private fun setupWithAttachment(textMessage: Message.Text, attachment: Attachment) {
        hideViews(textGroup.timeLayout)
        when (attachment.type) {
            Attachment.TYPE_PICTURE, Attachment.TYPE_VIDEO -> {
                setupWithMediaAttachment(textMessage, attachment)
            }
            Attachment.TYPE_FILE -> {
                setupWithFileAttachment(textMessage, attachment)
            }
        }
    }

    private fun setupWithMediaAttachment(textMessage: Message.Text, attachment: Attachment) {
        hideViews(fileGroup.layout, fileGroup.divider, fileGroup.downloadButton)
        showViews(mediaGroup.layout)
        val isTextAbove = textMessage.text.isNotEmpty() || authorGroup.name.isVisible
        loadPreview(attachment, isTextAbove)
        if (attachment.type == Attachment.TYPE_VIDEO) {
            showViews(mediaGroup.playVideoButton)
        } else {
            hideViews(mediaGroup.playVideoButton)
        }
        if (textMessage.isEdited()) {
            showViews(mediaGroup.edited, mediaGroup.editedDot)
        } else {
            hideViews(mediaGroup.edited, mediaGroup.editedDot)
        }
        mediaGroup.time.text = DateTimeUtils.getTime(textMessage.createdAt)
        resolveMessageStatus(textMessage.isRead, mediaGroup.status)
    }

    private fun setupWithFileAttachment(textMessage: Message.Text, attachment: Attachment) {
        hideViews(mediaGroup.layout)
        showViews(fileGroup.layout, fileGroup.downloadButton)
        resolveFileDividerVisibility(textMessage)
        fileGroup.fileName.text = attachment.title
        if (textMessage.isEdited()) {
            showViews(fileGroup.edited, fileGroup.editedDot)
        } else {
            hideViews(fileGroup.edited, fileGroup.editedDot)
        }
        fileGroup.time.text = DateTimeUtils.getTime(textMessage.createdAt)
        resolveMessageStatus(textMessage.isRead, fileGroup.status)
    }

    private fun resolveFileDividerVisibility(textMessage: Message.Text) {
        if (textMessage.text.isEmpty() && !authorGroup.name.isVisible) {
            fileGroup.divider.visibility = View.GONE
        } else {
            fileGroup.divider.visibility = View.VISIBLE
        }
    }

    private fun loadPreview(attachment: Attachment, isTextAbove: Boolean) {
        var placeholder: Drawable? = null
        val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.chat_msg_corner).toFloat()
        if (attachment.type == Attachment.TYPE_PICTURE) {
            placeholder = PlaceholderProvider.getPicturePlaceholder(context, cornerRadius)
        } else if (attachment.type == Attachment.TYPE_VIDEO) {
            placeholder = PlaceholderProvider.getVideoPlaceholder(context, cornerRadius)
        }
        Glide.with(mediaGroup.preview)
                .load(attachment.previewUrl)
                .placeholder(placeholder)
                .apply(if (isTextAbove) withTextRequestOptions else withoutTextRequestOptions)
                .into(mediaGroup.preview)
    }

    fun setupAudioMessage(message: Message.Audio) {
        hideViews(mediaGroup.layout, fileGroup.layout, fileGroup.downloadButton, textGroup.content,
                textGroup.timeLayout)
        showViews(audioGroup.layout)
        if (!authorGroup.name.isVisible) {
            textGroup.contentTopPadding.visibility = View.GONE
            fileGroup.divider.visibility = View.GONE
        } else {
            textGroup.contentTopPadding.visibility = View.VISIBLE
            fileGroup.divider.visibility = View.VISIBLE
        }
        audioGroup.time.text = DateTimeUtils.getTime(message.createdAt)
        audioGroup.isHeardIcon?.visibility = if (message.isHeard) View.VISIBLE else View.GONE
        resolveMessageStatus(message.isRead, audioGroup.status)
        resetPlayingState(false)
    }

    private fun resolveMessageStatus(isRead: Boolean?, ivStatus: ImageView?) {
        if (isRead != null) {
            ivStatus?.visibility = View.VISIBLE
            ivStatus?.setImageResource(if (isRead) readStatusIcon else sentStatusIcon)
        } else {
            ivStatus?.visibility = View.GONE
        }
    }

    fun resetPlayingState(delayed: Boolean = true) {
        isAudioMessagePlayingProgress = false
        setPlayingState(false)
        if (delayed) {
            audioGroup.playbackProgress.postDelayed({
                resetPlayingProgress()
            }, resetPlayingStateDelay)
        } else {
            resetPlayingProgress()
        }
    }

    private fun resetPlayingProgress() {
        audioProgressAnimator?.cancel()
        (audioGroup.playbackProgress.drawable as? ClipDrawable)?.let {
            it.level = 0
        }
        audioMessageDuration?.let {
            setFormattedDuration(it)
        } ?: run {
            audioGroup.duration.text = ""
        }
    }

    fun setAudioMessageDuration(duration: Long) {
        audioMessageDuration = duration
        if (!isAudioMessagePlayingProgress) {
            setFormattedDuration(duration)
        }
    }

    fun setPlayingState(isPlaying: Boolean) {
        val icon = if (isPlaying) pauseAudioIcon else playAudioIcon
        audioGroup.playButton.setImageDrawable(icon)
    }

    fun setPlayingProgress(totalMs: Long, currentMs: Long) {
        isAudioMessagePlayingProgress = true
        (audioGroup.playbackProgress.drawable as? ClipDrawable)?.let {
            val currentProgress = (10000.toFloat() / totalMs * currentMs).toInt()
            audioProgressAnimator = ObjectAnimator.ofInt(it, "level", currentProgress).apply {
                duration = audioProgressAnimDuration
                start()
            }
        }

        setFormattedDuration(currentMs)
    }

    private fun setFormattedDuration(millis: Long) {
        val min = millis / 60_000
        val sec = (millis - min * 60_000) / 1_000
        val minStr = if (min < 10) "0$min" else if (min > 99) "99" else "$min"
        val secStr = if (sec < 10) "0$sec" else "$sec"
        audioGroup.duration.text = context.getString(R.string.audio_message_duration_pattern,
                minStr, secStr)
    }

    fun setAuthorVisibility(isVisible: Boolean) {
        if (isVisible) {
            showViews(authorGroup.avatar, authorGroup.avatarHelper, authorGroup.name)
        } else {
            hideViews(authorGroup.avatar, authorGroup.avatarHelper, authorGroup.name)
        }
    }

    private fun hideViews(vararg views: View) {
        for (v in views) v.visibility = View.GONE
    }

    private fun showViews(vararg views: View) {
        for (v in views) v.visibility = View.VISIBLE
    }

    fun setAudioMessageHeard() {
        audioGroup.isHeardIcon?.visibility = View.VISIBLE
    }

    class AuthorGroup(
            internal val avatar: AvatarView,
            internal val avatarHelper: View,
            internal val name: TextView
    )

    class TextGroup(
            internal val timeLayout: ViewGroup,
            internal val time: TextView,
            internal val edited: View,
            internal val editedDot: View,
            internal val textToSpeech: ImageView,
            internal val content: ExpandableTextView,
            internal val contentTopPadding: View,
            internal val status: ImageView? = null
    )

    class MediaGroup(
            internal val layout: ViewGroup,
            internal val preview: ImageView,
            internal var playVideoButton: View,
            internal val time: TextView,
            internal val edited: View,
            internal val editedDot: View,
            internal val status: ImageView? = null
    )

    class FileGroup(
            internal val layout: ViewGroup,
            internal val fileName: TextView,
            internal val divider: View,
            internal val downloadButton: View,
            internal val time: TextView,
            internal val edited: View,
            internal val editedDot: View,
            internal val textToSpeech: ImageView,
            internal val status: ImageView? = null
    )

    class AudioGroup(
            internal val layout: ViewGroup,
            internal val playButton: ImageView,
            internal val duration: TextView,
            internal val playbackProgress: ImageView,
            internal val time: TextView,
            internal val isHeardIcon: View?,
            internal val status: ImageView?
    )
}