package com.gbksoft.neighbourhood.ui.widgets.chat.message.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageAuthor

abstract class MessageView : ConstraintLayout {
    var onMessageLongClickListener: ((position: Int, message: Message) -> Unit)? = null
    var onAttachmentClickListener: ((Message, Attachment) -> Unit)? = null
    var onAuthorClickListener: ((MessageAuthor) -> Unit)? = null
    var onMentionClickListener: ((Long) -> Unit)? = null
    var textToSpeechHandler: ((text: String, messageId: Long, messageView: MessageView) -> Unit)? = null
    var audioPlaybackHandler: ((url: String, messageId: Long, messageView: MessageView) -> Unit)? = null

    protected var messagePosition: Int? = null
    protected var message: Message? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setMessage(position: Int, message: Message, progress: Progress?) {
        this.messagePosition = position
        this.message = message
        checkDownloadProgress(message, progress)
        setupView(message)
    }

    private fun checkDownloadProgress(message: Message, progress: Progress?) {
        if (message !is Message.Text) return
        if (message.attachment == null || message.attachment.type != Attachment.TYPE_FILE) return

        val progressBar = provideDownloadProgressBar()
        progressBar.animation?.cancel()
        if (progress != null) {
            progressBar.max = progress.total
            progressBar.progress = progress.current
        } else {
            progressBar.max = 0
            progressBar.progress = 0
        }
    }

    abstract fun setupView(message: Message)

    protected abstract fun provideDownloadProgressBar(): ProgressBar

    fun onDownloadProgressChanged(total: Int, current: Int) {
        val progressBar = provideDownloadProgressBar()
        progressBar.max = total
        val anim = ProgressBarAnimation(
            progressBar,
            progressBar.progress,
            current
        )
        anim.duration = 200
        progressBar.startAnimation(anim)
    }

    abstract fun setAuthorVisibility(isVisible: Boolean)

    abstract fun setTextExpandable(isExpandable: Boolean)

    abstract fun setTextToSpeechEnabled(isEnabled: Boolean)

    abstract fun setTextToSpeechActive(isActive: Boolean)

    abstract fun setAudioMessageDuration(duration: Long)

    abstract fun setPlayingState(isPlaying: Boolean)

    abstract fun setPlayingProgress(totalMs: Long, currentMs: Long)

    abstract fun resetPlayingState()

}