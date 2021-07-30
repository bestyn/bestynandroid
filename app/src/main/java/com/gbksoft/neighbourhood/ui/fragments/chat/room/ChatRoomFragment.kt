package com.gbksoft.neighbourhood.ui.fragments.chat.room

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentChatRoomBinding
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.ConversationIds
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.chat.AttachmentProvider
import com.gbksoft.neighbourhood.ui.fragments.chat.background.component.ChatBackgroundManager
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.AttachmentBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.OwnMessageBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.StrangerMessageBottomSheet
import com.gbksoft.neighbourhood.ui.widgets.base.SimpleTextWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.DownloadProgressCallback
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.MessageAdapter
import com.gbksoft.neighbourhood.utils.CopyUtils
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.VisiblePositionsChangeListener
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatRoomFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<ChatRoomFragmentArgs>()
    private lateinit var layout: FragmentChatRoomBinding
    private val viewModel by viewModel<ChatRoomViewModel>()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatLayoutManager: LinearLayoutManager

    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private val attachmentBottomSheet by lazy {
        val bottomSheet = AttachmentBottomSheet.newInstance()
        bottomSheet.onSelectFromGalleryClickListener = { selectMediaFromGallery() }
        bottomSheet.onTakePhotoClickListener = { takePhoto() }
        bottomSheet.onMakeVideoClickListener = { makeVideo() }
        bottomSheet.onFileClickListener = { selectFile() }
        bottomSheet
    }

    private val ownMessageBottomSheet by lazy {
        val bottomSheet = OwnMessageBottomSheet.newInstance()
        bottomSheet.onEditClickListener = {
            if (it is Message.Text) editMessage(it)
        }
        bottomSheet.onDeleteClickListener = ::showDeleteMessageDialog
        bottomSheet.onCopyClickListener = ::copyMessage
        bottomSheet
    }

    private val strangerMessageBottomSheet by lazy {
        val bottomSheet = StrangerMessageBottomSheet.newInstance()
        bottomSheet.onCopyClickListener = ::copyMessage
        bottomSheet
    }

    private val attachmentProvider by lazy {
        AttachmentProvider(requireContext(), this)
    }

    private val visiblePositionsChangeListener: RecyclerView.OnScrollListener = object : VisiblePositionsChangeListener() {
        override fun visiblePositionsChanged(firstPosition: Int, lastPosition: Int) {
            onVisiblePositionsChanges(firstPosition, lastPosition)
        }
    }

    private val positionSet = mutableSetOf<Int>()
    private val handler = Handler()
    private val runnable = Runnable {
        if (positionSet.isEmpty()) return@Runnable

        val list = positionSet.toList()
        positionSet.clear()
        val lastPosition = list.max()!!
        val firstPosition = list.min()!!
        val visibleMessages = messageAdapter.findMessagesBetween(firstPosition, lastPosition)
        if (visibleMessages.isNotEmpty()) {
            viewModel.onLastVisibleMessageChanged(visibleMessages.last())
            viewModel.markChatMessagesAsRead(visibleMessages)
        }
    }

    private fun onVisiblePositionsChanges(firstPosition: Int, lastPosition: Int) {
        for (i in firstPosition..lastPosition) positionSet.add(i)
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 500)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkIsOpponentOnline(args.chatRoomData.opponentId)
        viewModel.loadChat(args.chatRoomData.conversationId, args.chatRoomData.opponentId)
        viewModel.connectToConversationsChannel(args.chatRoomData.opponentId)
        viewModel.subscribeToConversationActions()
    }

    override fun getStatusBarColor(): Int {
        return R.color.chat_list_action_bar_color
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hideNavigateBar()
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_room, container, false)

        setupActionBar()
        setupView()
        setClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    private fun setupActionBar() {
        layout.avatar.setFullName(args.chatRoomData.opponentName)
        layout.avatar.setImage(args.chatRoomData.opponentAvatar)
        layout.avatar.setBusiness(args.chatRoomData.isBusinessOpponent)
        layout.title.text = args.chatRoomData.opponentName
    }

    private fun setupView() {
        val backgroundManager = ChatBackgroundManager.getInstance()
        val background = backgroundManager.getSelectedBackground()
        backgroundManager
            .loadOrigin(background)
            .into(layout.ivBackground)
        messageAdapter = MessageAdapter(requireContext(), viewModel.getCurrentProfileId())
        messageAdapter.setUnreadMessageHighlighterEnabled(true)
        messageAdapter.isAuthorVisible = false
        messageAdapter.isTextExpandable = false
        messageAdapter.isTextTextToSpeechEnabled = true
        chatLayoutManager = LinearLayoutManager(requireContext())
        chatLayoutManager.reverseLayout = true
        layout.rvMessages.layoutManager = chatLayoutManager
        layout.rvMessages.adapter = messageAdapter
        layout.rvMessages.addOnScrollListener(visiblePositionsChangeListener)
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_chat_room)?.let {
            divider.setDrawable(it)
            layout.rvMessages.addItemDecoration(divider)
        }
    }

    private fun setClickListeners() {
        layout.avatar.setOnClickListener { openOpponentProfile() }
        layout.title.setOnClickListener { openOpponentProfile() }
        layout.inputMessageForm.audioRecordPermissionRequestListener = {
            requestAudioRecordPermission()
        }
        layout.inputMessageForm.onAddAttachmentClickListener = { addAttachment() }
        layout.inputMessageForm.onRemoveAttachmentClickListener = { removeAttachment() }
        layout.inputMessageForm.onSendClickListener = { text, attachment ->
            sendMessage(text, attachment)
        }
        layout.inputMessageForm.onApplyEditClickListener = { oldMessage, text, attachment ->
            updateMessage(oldMessage, text, attachment)
        }
        layout.inputMessageForm.audioRecordListener = ::onAudioRecordDone
        messageAdapter.onIncomingMessageLongClickListener = ::onIncomingMessageLongClick
        messageAdapter.onOutcomingMessageLongClickListener = ::onOutcomingMessageLongClick
        messageAdapter.onAttachmentClickListener = ::onCommentAttachmentClick
        messageAdapter.onFileAttachmentDownloadCompleteListener = ::onFileAttachmentDownloadComplete
        messageAdapter.onIncomingAudioMessageHeardListener = ::onIncomingAudioMessageHeard
        initTypingStateWatcher()
    }

    private fun initTypingStateWatcher() {
        val handler = Handler()
        val notTypingRunnable = Runnable {
            viewModel.sendTypingState(false)
        }
        layout.inputMessageForm.addTextWatcher(SimpleTextWatcher {
            if (it.isEmpty()) return@SimpleTextWatcher

            viewModel.sendTypingState(true)
            handler.removeCallbacks(notTypingRunnable)
            handler.postDelayed(notTypingRunnable, 3000)
        })
    }

    private fun openOpponentProfile() {
        val direction = if (args.chatRoomData.isBusinessOpponent) {
            ChatRoomFragmentDirections.toPublicBusinessProfile(args.chatRoomData.opponentId)
        } else {
            ChatRoomFragmentDirections.toPublicProfile(args.chatRoomData.opponentId)
        }
        findNavController().navigate(direction)
    }

    private fun requestAudioRecordPermission() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionToken = {
            it.continuePermissionRequest()
        }
        val context = requireContext()
        Dexter.withContext(context)
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(permissionListener)
            .withErrorListener { error: DexterError ->
                ToastUtils.showToastMessage(context, "Error occurred: $error")
            }
            .onSameThread()
            .check()
    }

    private fun addAttachment() {
        attachmentBottomSheet.show(childFragmentManager, "AttachmentBottomSheet")
    }

    private fun removeAttachment() {
        viewModel.removeMessageAttachment()
    }

    private fun selectMediaFromGallery() {
        attachmentProvider.requestPictureOrVideoFromGallery()
    }

    private fun takePhoto() {
        attachmentProvider.requestPictureFromCamera()
    }

    private fun makeVideo() {
        attachmentProvider.requestVideoFromCamera()
    }

    private fun selectFile() {
        attachmentProvider.requestFile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        KeyboardUtils.hideKeyboard(getParentActivity())

        when (requestCode) {
            AttachmentProvider.REQUEST_FROM_GALLERY,
            AttachmentProvider.REQUEST_FROM_CAMERA,
            AttachmentProvider.REQUEST_FILE -> try {
                val localAttachment =
                    attachmentProvider.prepareLocalAttachment(requestCode, resultCode, data)
                if (localAttachment != null) {
                    viewModel.addMessageLocalAttachment(localAttachment)
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                ToastUtils.showToastMessage(requireContext(), R.string.error_unable_open_file)
            }
        }
    }

    private fun sendMessage(text: String?, attachment: Attachment?) {
        KeyboardUtils.hideKeyboard(layout.root)
        viewModel.sendTextMessage(text)
    }

    private fun updateMessage(oldMessage: Message, text: String?, attachment: Attachment?) {
        KeyboardUtils.hideKeyboard(layout.root)
        if (oldMessage is Message.Text) {
            viewModel.updateTextMessage(oldMessage, text, attachment)
        }
    }

    private fun onAudioRecordDone(localFile: LocalFile<Int>) {
        viewModel.sendAudioMessage(localFile)
    }

    private fun onIncomingMessageLongClick(position: Int, message: Message) {
        if (message is Message.Text && message.text.isNotEmpty()) {
            strangerMessageBottomSheet.show(childFragmentManager, message)
        }
    }

    private fun onOutcomingMessageLongClick(position: Int, message: Message) {
        ownMessageBottomSheet.isEditingEnabled = message is Message.Text
        ownMessageBottomSheet.show(childFragmentManager, message)
    }

    private fun showDeleteMessageDialog(message: Message) {
        YesNoDialog.Builder()
            .setMessage(R.string.dialog_delete_message_text)
            .setPositiveButton(R.string.dialog_delete_message_yes) {
                viewModel.deleteMessage(message)
            }
            .setNegativeButton(R.string.dialog_delete_message_no, null)
            .build()
            .show(childFragmentManager, "DeleteMessageDialog")
    }

    private fun editMessage(textMessage: Message.Text) {
        layout.inputMessageForm.editTextMessage(textMessage)
        viewModel.setEditingTextMessage(textMessage)
    }

    private fun copyMessage(textMessage: Message.Text) {
        CopyUtils.copy(requireContext(), textMessage.text, R.string.message_copied)
    }

    private fun onCommentAttachmentClick(progressCallback: DownloadProgressCallback, attachment: Attachment) {
        when (attachment.type) {
            Attachment.TYPE_VIDEO -> {
                onMediaClick(Media.Video(
                    attachment.id,
                    attachment.previewUrl.toUri(),
                    attachment.originUrl.toUri(),
                    attachment.created
                ))
            }
            Attachment.TYPE_PICTURE -> {
                onMediaClick(Media.Picture(
                    attachment.id,
                    attachment.previewUrl.toUri(),
                    attachment.originUrl.toUri(),
                    attachment.created
                ))
            }
            Attachment.TYPE_FILE -> {
                downloadFile(progressCallback, attachment)
            }
        }
    }

    private fun onFileAttachmentDownloadComplete() {
        ToastUtils.showToastMessage(requireContext(), R.string.toast_file_downloaded)
    }

    private fun onIncomingAudioMessageHeard(audioMessage: Message.Audio) {
        viewModel.markAudioMessagesAsHeard(audioMessage)
    }

    private fun onMediaClick(media: Media) {
        when (media) {
            is Media.Picture -> {
                val direction =
                    ChatRoomFragmentDirections.toImagePreview(media)
                findNavController().navigate(direction)
            }
            is Media.Video -> {
                val direction =
                    ChatRoomFragmentDirections.toVideoPlayer(media)
                findNavController().navigate(direction)
            }
        }
    }

    private fun downloadFile(progressCallback: DownloadProgressCallback, attachment: Attachment) {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = {
            viewModel.downloadFile(progressCallback, attachment)
        }
        permissionListener.onPermissionToken = { it.continuePermissionRequest() }
        val context = requireContext()
        Dexter.withContext(context)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(permissionListener)
            .withErrorListener { error: DexterError ->
                ToastUtils.showToastMessage(context, "Error occurred: $error")
            }
            .onSameThread()
            .check()
    }

    private fun subscribeToViewModel() {
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            showMessages(it.messages, it.firstUnreadMessageId)
        })
        viewModel.conversationRead.observe(viewLifecycleOwner, Observer { onConversationRead(it) })
        viewModel.messageAttachment.observe(viewLifecycleOwner, Observer {
            onMessageFormAttachmentChanged(it)
        })
        viewModel.messageSendingProcess.observe(viewLifecycleOwner, Observer { isSending ->
            layout.inputMessageForm.setControlsEnabled(!isSending)
        })
        viewModel.clearMessageForm.observe(viewLifecycleOwner, Observer {
            layout.inputMessageForm.clearForm()
        })
        viewModel.isOpponentOnline.observe(viewLifecycleOwner, Observer {
            layout.onlineIndicator.isOnline = it
        })
        viewModel.typingState.observe(viewLifecycleOwner, Observer {
            layout.onlineIndicator.isTyping = it
        })
    }

    private var needFirstMessagesShow = true
    private fun showMessages(messages: List<Message>, firstUnreadMessageId: Long?) {
        val alreadyContainsMessage = messageAdapter.itemCount > 0
        messageAdapter.setData(messages, firstUnreadMessageId)

        if (needFirstMessagesShow) {
            needFirstMessagesShow = false
            messageAdapter.getFirstUnreadMessagePosition()?.let {
                val offset = layout.rvMessages.height / 2
                chatLayoutManager.scrollToPositionWithOffset(it, offset)
            }
        } else if (alreadyContainsMessage) {
            layout.rvMessages.post {
                val firstVisibleItemPosition = chatLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = chatLayoutManager.findLastVisibleItemPosition()
                onVisiblePositionsChanges(firstVisibleItemPosition, lastVisibleItemPosition)
            }
        }
    }

    private fun onConversationRead(conversationId: Long) {
        val conversationIds = ConversationIds(conversationId, args.chatRoomData.opponentId)
        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ResultData<ConversationIds>>(RESULT_CONVERSATION_READ)
            ?.value = ResultData(conversationIds)
    }

    private fun onMessageFormAttachmentChanged(attachment: Attachment?) {
        attachment?.let { layout.inputMessageForm.setAttachment(it) }
    }

    override fun onStart() {
        super.onStart()
        subscribeToAudioFocusChange()
    }

    private fun subscribeToAudioFocusChange() {
        val audioManager = requireContext()
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener {
                    stopAudioPlayback()
                }.build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
                stopAudioPlayback()
            }
            audioManager.requestAudioFocus({ audioFocusChangeListener },
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    override fun onStop() {
        stopAudioPlayback()
        unsubscribeToAudioFocusChange()
        super.onStop()
    }

    private fun unsubscribeToAudioFocusChange() {
        val audioManager = requireContext()
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioFocusChangeListener?.let { audioManager.abandonAudioFocus(it) }
        }
    }

    private fun stopAudioPlayback() {
        messageAdapter.stopTextToSpeech()
        messageAdapter.stopAudioPlayback()
    }

    companion object {
        const val RESULT_CONVERSATION_READ = "result_conversation_read" //ResultData<ConversationIds>
    }

}