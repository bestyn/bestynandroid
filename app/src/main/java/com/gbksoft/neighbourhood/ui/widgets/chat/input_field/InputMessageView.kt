package com.gbksoft.neighbourhood.ui.widgets.chat.input_field

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutInputMessageBinding
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.audio.AudioMessageDelegate
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.CorrectMessageWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.child_watchers.AttachmentWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher.child_watchers.MessageFieldWatcher

class InputMessageView : ConstraintLayout {
    private lateinit var inputButton: ImageView
    private lateinit var layout: LayoutInputMessageBinding
    private lateinit var attachmentButtonController: AttachmentButtonController
    private lateinit var attachmentInfoController: AttachmentInfoController

    private val DEFAULT_INPUT_MODE = InputMode.TEXT_MESSAGE

    private var hintButton: ImageView? = null
    private var isVoiceEnabled = false
    private var hintText = ""
    private var editMessage: Message? = null
    private var mode: InputMode = DEFAULT_INPUT_MODE
    private val correctMessageWatcher = CorrectMessageWatcher()
    private val attachmentWatcher = AttachmentWatcher()

    var onAddAttachmentClickListener: (() -> Unit)? = null
    var onRemoveAttachmentClickListener: (() -> Unit)? = null
    var onSendClickListener: ((text: String?, attachment: Attachment?) -> Unit)? = null
    var onApplyEditClickListener: ((oldMessage: Message, text: String?, attachment: Attachment?) -> Unit)? = null
    var audioRecordPermissionRequestListener: (() -> Unit)? = null
    var audioRecordListener: ((localFile: LocalFile<Int>) -> Unit)? = null

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
        attrs?.let { extractAttrs(it) }
        layout = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.layout_input_message, this, true)
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.InputMessageView)
        try {
        } finally {
            isVoiceEnabled = a.getBoolean(R.styleable.InputMessageView_imv_isVoiceEnabled, isVoiceEnabled)
            hintText = a.getString(R.styleable.InputMessageView_imv_hint) ?: hintText
            a.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setup()
    }

    private fun setup() {
        correctMessageWatcher.setControlsBarrier(layout.controlsBarrier)

        val messageFieldWatcher = MessageFieldWatcher()
        correctMessageWatcher.setChildren(messageFieldWatcher, attachmentWatcher)

        setInputMode(DEFAULT_INPUT_MODE)
        setupMessageField(messageFieldWatcher)
        setupAttachmentButton()
        setupAttachmentInfo()
        setOnClickListeners()
        AudioMessageDelegate(context, layout, ::requestAudioRecordPermission, ::onAudioRecordDone)
    }

    private fun setInputMode(mode: InputMode) {
        this.mode = mode
        updateCorrectMessageWatcher(mode, correctMessageWatcher)
        resolveEditModeButtonVisibility()
    }

    private fun setupMessageField(textWatcher: TextWatcher) {
        layout.messageField.hint = hintText
        layout.messageField.addTextChangedListener(textWatcher)
    }

    private fun updateCorrectMessageWatcher(mode: InputMode, correctMessageWatcher: CorrectMessageWatcher) {
        when (mode) {
            InputMode.TEXT_MESSAGE -> {
                inputButton = layout.btnPostMessage
                if (isVoiceEnabled) {
                    hintButton = layout.btnVoiceMessage
                    correctMessageWatcher.setIncorrectMessageVisibleControls(hintButton)
                } else {
                    layout.btnVoiceMessage.visibility = View.GONE
                    correctMessageWatcher.setIncorrectMessageVisibleControls(null)
                }
                correctMessageWatcher.setCorrectMessageVisibleControls(inputButton)
                correctMessageWatcher.resolveControlsVisibility()
            }
            InputMode.EDIT_TEXT_MESSAGE -> {
                inputButton = layout.btnApplyEdits
                layout.btnVoiceMessage.visibility = View.GONE
                hintButton = null
                correctMessageWatcher.setIncorrectMessageVisibleControls(hintButton)
                correctMessageWatcher.setCorrectMessageVisibleControls(inputButton)
                correctMessageWatcher.resolveControlsVisibility()
            }
            InputMode.VOICE_MESSAGE -> {
                correctMessageWatcher.resolveControlsVisibility()
            }
        }
    }

    private fun resolveEditModeButtonVisibility() {
        if (mode == InputMode.EDIT_TEXT_MESSAGE) {
            layout.groupEdit.visibility = View.VISIBLE
        } else {
            layout.groupEdit.visibility = View.GONE
        }
    }

    private fun setupAttachmentButton() {
        attachmentButtonController = AttachmentButtonController(
            layout.btnAddAttachment,
            layout.messageField
        )
        layout.btnAddAttachment.setOnClickListener {
            onAddAttachmentClickListener?.invoke()
        }
    }

    private fun setupAttachmentInfo() {
        attachmentInfoController = AttachmentInfoController(
            layout.groupAttachment,
            layout.iconPicture,
            layout.iconVideo,
            layout.iconFile,
            layout.attachmentName,
            layout.btnRemoveAttachment
        )
        layout.btnRemoveAttachment.setOnClickListener {
            removeAttachment()
            onRemoveAttachmentClickListener?.invoke()
        }
    }

    fun setAttachment(attachment: Attachment) {
        attachmentWatcher.setAttachment(attachment)
        attachmentButtonController.setAttachment(attachment)
        attachmentInfoController.setAttachment(attachment)
    }

    fun removeAttachment() {
        attachmentWatcher.setAttachment(null)
        attachmentButtonController.setAttachment(null)
        attachmentInfoController.setAttachment(null)
    }

    fun setMessageFieldText(text: String) {
        layout.messageField.setText(text)
//        if(text.lastIndex != -1){
//            layout.messageField.setSelection(text.lastIndex)
//        }
    }

    fun clearMessageField() {
        layout.messageField.setText("")
    }

    private fun setOnClickListeners() {
        layout.btnPostMessage.setOnClickListener {
            val text = layout.messageField.text.toString()
            val attachment = attachmentWatcher.attachment
            onSendClickListener?.invoke(text, attachment)
        }
        layout.btnApplyEdits.setOnClickListener {
            editMessage?.let { message ->
                val text = layout.messageField.text.toString()
                val attachment = attachmentWatcher.attachment
                onApplyEditClickListener?.invoke(message, text, attachment)
            }
        }
        layout.btnCancelEdit.setOnClickListener {
            clearForm()
        }
    }

    private fun requestAudioRecordPermission() {
        audioRecordPermissionRequestListener?.invoke()
    }

    private fun onAudioRecordDone(localFile: LocalFile<Int>) {
        audioRecordListener?.invoke(localFile)
    }

    fun setControlsEnabled(isEnabled: Boolean) {
        layout.btnPostMessage.isEnabled = isEnabled
        layout.btnApplyEdits.isEnabled = isEnabled
        layout.btnCancelEdit.isEnabled = isEnabled
        layout.btnVoiceMessage.isEnabled = isEnabled
    }

    fun editTextMessage(textMessage: Message.Text) {
        editMessage = textMessage
        setInputMode(InputMode.EDIT_TEXT_MESSAGE)

        textMessage.attachment?.let {
            setAttachment(it)
        } ?: run {
            removeAttachment()
        }

        setMessageFieldText(textMessage.text)

//        val mentionManager = MentionManager()
//        mentionManager.parseOriginText(textMessage.text)
//        val spannedText = mentionManager.spanMentions(textMessage.text).toString()
//        setMessageFieldText(spannedText)
    }

    fun clearForm() {
        removeAttachment()
        clearMessageField()
        setInputMode(DEFAULT_INPUT_MODE)
        setControlsEnabled(true)
    }

    fun addTextWatcher(textWatcher: TextWatcher) {
        layout.messageField.addTextChangedListener(textWatcher)
    }

    fun getMessageField(): EditText {
        return layout.messageField
    }

}