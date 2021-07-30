package com.gbksoft.neighbourhood.ui.fragments.stories

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetStoryCommentsBinding
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageAuthor
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.BaseBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.base.chat.AttachmentProvider
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsFeedViewHelper
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.MentionAdapter
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostPopupMenuResolver
import com.gbksoft.neighbourhood.ui.fragments.post_details.PostDetailsViewModel
import com.gbksoft.neighbourhood.ui.fragments.post_details.adapter.PostEmptyCommentsAdapter
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.AttachmentBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.OwnMessageBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.StrangerMessageBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.MentionTextWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.DownloadProgressCallback
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.MessageAdapter
import com.gbksoft.neighbourhood.utils.CopyUtils
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError


class StoryCommentsBottomSheet : BaseBottomSheet(), MediaPagerHost, MentionTextWatcher.OnMentionChangedListener {
    companion object {
        fun newInstance(story: FeedPost, topInset: Int): StoryCommentsBottomSheet {
            val fragment = StoryCommentsBottomSheet()
            val arguments = Bundle()
            arguments.putParcelable("story", story)
            arguments.putInt("topInset", topInset)
            fragment.arguments = arguments
            return fragment
        }
    }

    var onUpdateCommentsCounterListener: ((FeedPost) -> Unit)? = null

    private lateinit var viewModel: PostDetailsViewModel
    private lateinit var layout: BottomSheetStoryCommentsBinding
    private lateinit var emptyCommentsHintAdapter: PostEmptyCommentsAdapter
    private lateinit var commentsAdapter: MessageAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var mentionAdapter: MentionAdapter
    private lateinit var postsFeedHelper: PostsFeedViewHelper
    private lateinit var postPopupMenuResolver: PostPopupMenuResolver
    private lateinit var mentionTextWatcher: MentionTextWatcher

    private var currentStory: FeedPost? = null
    private var topInset: Int = 0

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleCommentChanged(lastVisibleItemPosition)
        }
    }

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

    private var bottomSheetDialog: BottomSheetDialog? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog?.apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CommentsBottomSheetDialogTheme)

        val story: FeedPost = requireArguments().getParcelable("story")!!
        topInset = requireArguments().getInt("topInset")
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
                .get(PostDetailsViewModel::class.java)
        this.currentStory = story
        viewModel.setFeedPost(story)
        postsFeedHelper = PostsFeedViewHelper(requireContext()).also { it.currentProfileId = viewModel.getCurrentProfileId() }
        postPopupMenuResolver = PostPopupMenuResolver(requireContext()).also { it.currentProfileId = viewModel.getCurrentProfileId() }
        initAdapters()
    }

    private fun initAdapters() {
        emptyCommentsHintAdapter = PostEmptyCommentsAdapter()
        commentsAdapter = MessageAdapter(requireContext(), viewModel.getCurrentProfileId())
        commentsAdapter.onIncomingMessageLongClickListener = ::onIncomingMessageLongClick
        commentsAdapter.onOutcomingMessageLongClickListener = ::onOutcomingMessageLongClick
        commentsAdapter.onAttachmentClickListener = ::onCommentAttachmentClick
        commentsAdapter.onFileAttachmentDownloadCompleteListener = ::onFileAttachmentDownloadComplete
        commentsAdapter.onAuthorClickListener = ::onCommentAuthorClick
        commentsAdapter.onMentionClickListener = ::onMentionClick
        concatAdapter = ConcatAdapter(emptyCommentsHintAdapter, commentsAdapter)

        mentionAdapter = MentionAdapter()
        mentionAdapter.onProfileClickListener = { addMention(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_story_comments, container, false)

        setupView()
        setClickListeners()
        subscribeToEditPostResult()
        subscribeToViewModel()

        return layout.root
    }

    private fun setupView() {
        layout.rootView.updatePadding(top = topInset + layout.rootView.paddingTop)
        layout.rvChat.layoutManager = LinearLayoutManager(requireContext())
        layout.rvChat.adapter = concatAdapter
        layout.rvChat.addOnScrollListener(lastVisiblePositionListener)
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_chat_room)?.let {
            divider.setDrawable(it)
            layout.rvChat.addItemDecoration(divider)
        }

        mentionTextWatcher = MentionTextWatcher(layout.inputMessageForm.getMessageField(), this).apply {
            setMentionColor(requireContext(), R.color.post_hashtag_color)
        }
        layout.inputMessageForm.addTextWatcher(mentionTextWatcher)
        layout.rvMentions.adapter = mentionAdapter
        layout.rvMentions.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
    }

    override fun onDestroyView() {
        layout.rvChat.adapter = null
        super.onDestroyView()
    }

    private fun setClickListeners() {
        layout.inputMessageForm.onAddAttachmentClickListener = { addAttachment() }
        layout.inputMessageForm.onRemoveAttachmentClickListener = { removeAttachment() }
        layout.inputMessageForm.onSendClickListener = { text, attachment ->
            sendMessage(mentionTextWatcher.prepareMentionsText(), attachment)
        }
        layout.inputMessageForm.onApplyEditClickListener = { oldMessage, text, attachment ->
            updateMessage(oldMessage, mentionTextWatcher.prepareMentionsText(), attachment)
        }
        layout.btnClose.setOnClickListener { dismiss() }
        layout.rootView.setOnClickListener { dismiss() }
    }

    private fun subscribeToEditPostResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.observe(viewLifecycleOwner, Observer { handleCreateEditResult(it) })
    }

    private fun handleCreateEditResult(resultData: ResultData<PostResult>) {
        if (resultData.notContainsData()) return
        resultData.consumeData()?.let { result ->
            when (result.status) {
                PostResult.STATUS_EDITED,
                PostResult.STATUS_CHANGED -> {
                    result.feedPost.let { viewModel.refreshPost(it.type, it.post.id) }
                }
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.openAuthor.observe(viewLifecycleOwner, Observer { handleOpenAuthor(it) })
        viewModel.finishWithResult.observe(viewLifecycleOwner, Observer { finishWithResult(it) })
        viewModel.messageAttachment.observe(viewLifecycleOwner, Observer { onMessageFormAttachmentChanged(it) })
        viewModel.messageSendingProcess.observe(viewLifecycleOwner, Observer { isSending -> layout.inputMessageForm.setControlsEnabled(!isSending) })
        viewModel.clearMessageForm.observe(viewLifecycleOwner, Observer { layout.inputMessageForm.clearForm() })
        viewModel.postComments.observe(viewLifecycleOwner, Observer { showComments(it) })
        viewModel.foundMentions.observe(viewLifecycleOwner, Observer { onMentionsFound(it) })
        viewModel.navigateToMyProfile.observe(viewLifecycleOwner, Observer { openMyProfile(false) })
        viewModel.navigateToMyBusinessProfile.observe(viewLifecycleOwner, Observer { openMyProfile(true) })
        viewModel.navigateToPublicProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, false) })
        viewModel.navigateToPublicBusinessProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, true) })
    }

    private fun handleOpenAuthor(profileData: PostDetailsViewModel.ProfileData) {
        if (profileData.isMine) {
            openMyProfile(profileData.profile.isBusiness)
        } else {
            openStrangerProfile(profileData.profile.id, profileData.profile.isBusiness)
        }
    }

    private fun finishWithResult(result: ResultData<PostResult>) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.value = result
        findNavController().popBackStack()
    }

    override fun onMediaClick(postMedia: Media) {
        when (postMedia) {
            is Media.Picture -> openImagePreview(postMedia)
            is Media.Video -> openVideoPreview(postMedia)
        }
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

    private fun sendMessage(text: String?, attachment: Attachment?) {
        KeyboardUtils.hideKeyboard(layout.root)
        viewModel.sendPostTextMessage(text, attachment)
    }

    private fun updateMessage(oldMessage: Message, text: String?, attachment: Attachment?) {
        KeyboardUtils.hideKeyboard(layout.root)
        if (oldMessage is Message.Text) {
            viewModel.updatePostTextMessage(oldMessage, text, attachment)
        }
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
        }
    }

    private fun onFileAttachmentDownloadComplete() {
        ToastUtils.showToastMessage(requireContext(), R.string.toast_file_downloaded)
    }

    private fun onCommentAuthorClick(author: MessageAuthor, isOutcoming: Boolean) {
        if (isOutcoming) {
            openMyProfile(author.isBusiness)
        } else {
            openStrangerProfile(author.id, author.isBusiness)
        }
    }

    private fun onMentionClick(profileId: Long) {
        viewModel.onMentionClicked(profileId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        KeyboardUtils.hideKeyboard(activity)
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

    private fun onMessageFormAttachmentChanged(attachment: Attachment?) {
        attachment?.let { layout.inputMessageForm.setAttachment(it) }
    }

    private fun showComments(comments: List<Message>) {
        emptyCommentsHintAdapter.setVisibility(comments.isEmpty())
        commentsAdapter.setData(comments)

        val story = currentStory ?: return
        story.messages = comments.size
        onUpdateCommentsCounterListener?.invoke(story)
    }

    override fun onNewMention() {
        viewModel.searchMentions(null)
    }

    override fun onMentionChanged(mention: String) {
        viewModel.searchMentions(mention)
    }

    override fun onMentionEnd() {
        viewModel.cancelMentionsSearching()
        layout.rvMentions.visibility = View.GONE
    }

    private fun onMentionsFound(mentions: List<ProfileSearchItem>) {
        mentionAdapter.setData(mentions)
        if (mentions.isEmpty()) {
            layout.textNoSearchMentions.visibility = View.VISIBLE
            layout.rvMentions.visibility = View.GONE
        } else {
            layout.textNoSearchMentions.visibility = View.GONE
            layout.rvMentions.visibility = View.VISIBLE
        }
    }

    private fun addMention(mention: ProfileSearchItem) {
        mentionTextWatcher.addMention(mention)
    }

    private fun openMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) {
            StoriesFragmentDirections.toMyBusinessProfileFragment()
        } else {
            StoriesFragmentDirections.toMyProfileFragment()
        }
        findNavController().navigate(direction)
    }

    private fun openStrangerProfile(profileId: Long, isBusiness: Boolean) {
        val direction = if (isBusiness) {
            StoriesFragmentDirections.toPublicBusinessProfileFragment(profileId)
        } else {
            StoriesFragmentDirections.toPublicProfileFragment(profileId)
        }
        findNavController().navigate(direction)
    }

    private fun openChatRoom(chatRoomData: ChatRoomData) {
        val destination = StoriesFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(destination)
    }

    private fun openImagePreview(media: Media.Picture) {
        val destination = StoriesFragmentDirections.toImagePreviewFragment(media)
        findNavController().navigate(destination)
    }

    private fun openVideoPreview(media: Media.Video) {
        val destination = StoriesFragmentDirections.toVideoPlayer(media)
        findNavController().navigate(destination)
    }
}