package com.gbksoft.neighbourhood.ui.fragments.post_details

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentPostDetailsBinding
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.model.chat.MessageAuthor
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.post.StoryPost
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.animation.WithoutChangeItemAnimator
import com.gbksoft.neighbourhood.ui.fragments.base.chat.AttachmentProvider
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsFeedViewHelper
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.MentionAdapter
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostPopupMenuResolver
import com.gbksoft.neighbourhood.ui.fragments.post_details.adapter.PostEmptyCommentsAdapter
import com.gbksoft.neighbourhood.ui.fragments.post_details.adapter.PostHeaderAdapter
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.AttachmentBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.OwnMessageBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.post_details.bottom_sheet.StrangerMessageBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.search.GlobalSearchFragmentDirections
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.MentionTextWatcher
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.DownloadProgressCallback
import com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter.MessageAdapter
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.PostReactionsBottomSheet
import com.gbksoft.neighbourhood.utils.CopyUtils
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostDetailsFragment : SystemBarsColorizeFragment(), MediaPagerHost, PopupMenu.OnMenuItemClickListener, MentionTextWatcher.OnMentionChangedListener {
    private lateinit var viewModel: PostDetailsViewModel
    private val downloadViewModel by viewModel<DownloadViewModel>()
    private lateinit var layout: FragmentPostDetailsBinding
    private lateinit var headerAdapter: PostHeaderAdapter
    private lateinit var emptyCommentsHintAdapter: PostEmptyCommentsAdapter
    private lateinit var commentsAdapter: MessageAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var mentionAdapter: MentionAdapter
    private lateinit var postsFeedHelper: PostsFeedViewHelper
    private lateinit var postPopupMenuResolver: PostPopupMenuResolver
    private lateinit var mentionTextWatcher: MentionTextWatcher
    private val args by navArgs<PostDetailsFragmentArgs>()
    private var currentFeedPost: FeedPost? = null
    private var isViewInitialized = false

    private lateinit var player: SimpleExoPlayer

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

    override fun getStatusBarColor(): Int {
        return R.color.post_details_action_bar_color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFeedPost = args.feedPost
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
                .get(PostDetailsViewModel::class.java)

        postsFeedHelper = PostsFeedViewHelper(requireContext()).also { it.currentProfileId = viewModel.getCurrentProfileId() }
        postPopupMenuResolver = PostPopupMenuResolver(requireContext()).also { it.currentProfileId = viewModel.getCurrentProfileId() }

        emptyCommentsHintAdapter = PostEmptyCommentsAdapter()
        commentsAdapter = MessageAdapter(requireContext(), viewModel.getCurrentProfileId())
        if (args.feedPost != null) {
            viewModel.setFeedPost(args.feedPost!!)
            initAdapters(args.feedPost!!)
        } else {
            viewModel.setPostId(args.postId)
        }
    }

    private fun initAdapters(feedPost: FeedPost) {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        headerAdapter = PostHeaderAdapter(feedPost, requireContext(), player).apply {
            onAuthorClickListener = { viewModel.onAuthorClick() }
            onHashtagClickListener = ::searchByHashtag
            onMentionClickListener = ::onMentionClick
            onReactionClickListener = { viewModel.onReactionClick(it) }
            onReactionCountClickListener = { onReactionCountClick() }
            onFollowClickListener = { viewModel.onFollowClick() }
            onMenuItemClickListener = this@PostDetailsFragment
            onMediaPageChangeListener = { viewModel.currentMediaPage = it }
            onDescriptionExpandedListener = { viewModel.isDescriptionExpanded = it }
            currentMediaPage = viewModel.currentMediaPage
            isDescriptionExpanded = viewModel.isDescriptionExpanded
            optionsMenuResolver = postPopupMenuResolver.asFunction()
            followButtonResolver = postsFeedHelper.setupFollowButton
            downloadAudioClickListener = ::onDownloadClick
            audioCounter = :: addAudioCounter
        }
        commentsAdapter.onIncomingMessageLongClickListener = ::onIncomingMessageLongClick
        commentsAdapter.onOutcomingMessageLongClickListener = ::onOutcomingMessageLongClick
        commentsAdapter.onAttachmentClickListener = ::onCommentAttachmentClick
        commentsAdapter.onFileAttachmentDownloadCompleteListener = ::onFileAttachmentDownloadComplete
        commentsAdapter.onAuthorClickListener = ::onCommentAuthorClick
        commentsAdapter.onMentionClickListener = ::onMentionClick

        concatAdapter = ConcatAdapter(headerAdapter, emptyCommentsHintAdapter, commentsAdapter)

        mentionAdapter = MentionAdapter()
        mentionAdapter.onProfileClickListener = { addMention(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_post_details, container, false)

        hideNavigateBar()
        if (args.feedPost != null) {
            setupView(args.feedPost!!)
        }
        setClickListeners()
        subscribeToEditPostResult()
        subscribeToViewModel()

        return layout.root
    }

    private fun setupView(feedPost: FeedPost) {
        isViewInitialized = true
        setTitle(feedPost.type)
        layout.rvChat.layoutManager = LinearLayoutManager(requireContext())
        layout.rvChat.adapter = concatAdapter
        layout.rvChat.addOnScrollListener(lastVisiblePositionListener)
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_chat_room)?.let {
            divider.setDrawable(it)
            layout.rvChat.addItemDecoration(divider)
        }
        layout.rvChat.itemAnimator = WithoutChangeItemAnimator()

        mentionTextWatcher = MentionTextWatcher(layout.inputMessageForm.getMessageField(), this).apply {
            setMentionColor(requireContext(), R.color.post_hashtag_color)
        }
        layout.inputMessageForm.addTextWatcher(mentionTextWatcher)
        layout.rvMentions.adapter = mentionAdapter
        layout.rvMentions.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
    }

    override fun onDestroyView() {
        layout.rvChat.adapter = null
        super.onDestroyView()
    }

    private fun setTitle(type: PostType) {
        val titleRes: Int? = when (type) {
            PostType.GENERAL -> R.string.title_general_post
            PostType.NEWS -> R.string.title_news
            PostType.CRIME -> R.string.title_crime
            PostType.OFFER -> R.string.title_offer
            PostType.EVENT -> R.string.title_event
            PostType.MEDIA -> R.string.title_media
            PostType.STORY -> R.string.title_story
        }
        layout.actionBar.setTitle(titleRes)
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
        viewModel.feedPost.observe(viewLifecycleOwner, Observer { setPost(it) })
        viewModel.openAuthor.observe(viewLifecycleOwner, Observer { handleOpenAuthor(it) })
        viewModel.finishWithResult.observe(viewLifecycleOwner, Observer { finishWithResult(it) })
        viewModel.messageAttachment.observe(viewLifecycleOwner, Observer {
            onMessageFormAttachmentChanged(it)
        })
        viewModel.messageSendingProcess.observe(viewLifecycleOwner, Observer { isSending ->
            layout.inputMessageForm.setControlsEnabled(!isSending)
        })
        viewModel.clearMessageForm.observe(viewLifecycleOwner, Observer {
            layout.inputMessageForm.clearForm()
        })
        viewModel.postComments.observe(viewLifecycleOwner, Observer {
            showComments(it)
        })
        viewModel.foundMentions.observe(viewLifecycleOwner, Observer { onMentionsFound(it) })
        viewModel.navigateToMyProfile.observe(viewLifecycleOwner, Observer { openMyProfile(false) })
        viewModel.navigateToMyBusinessProfile.observe(viewLifecycleOwner, Observer { openMyProfile(true) })
        viewModel.navigateToPublicProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, false) })
        viewModel.navigateToPublicBusinessProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, true) })
    }

    fun setPost(feedPost: FeedPost) {
        if (!isViewInitialized) {
            initAdapters(feedPost)
            setupView(feedPost)
        }
        headerAdapter.setData(feedPost)
    }

    private fun handleOpenAuthor(profileData: PostDetailsViewModel.ProfileData) {
        if (profileData.isMine) {
            openMyProfile(profileData.profile.isBusiness)
        } else {
            openStrangerProfile(profileData.profile.id, profileData.profile.isBusiness)
        }
    }

    override fun handleOnBackPressed(): Boolean {
        viewModel.onBackPressed()
        return true
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

    protected fun showDeletePostDialog(feedPost: FeedPost) {
        postsFeedHelper.showDeletePostDialog(feedPost, childFragmentManager) { viewModel.deletePost() }
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

    private fun onDownloadClick (fileUri: String){
        ToastUtils.showToastMessage("Download $fileUri")
        downloadViewModel.download(fileUri.toUri())
    }

    private fun addMention(mention: ProfileSearchItem) {
        mentionTextWatcher.addMention(mention)
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.actionEditPost -> {
                currentFeedPost?.let {
                    openEditPost(it)
                }
            }
            R.id.actionDeletePost -> {
                currentFeedPost?.let {
                    showDeletePostDialog(it)
                }
            }
            R.id.actionUnfollowPost -> {
                viewModel.onUnfollowClick()
            }
            R.id.actionReportPost -> {
                currentFeedPost?.let {
                    val reportContentArgs = ReportContentArgs.fromPost(it.post)
                    val direction = PostDetailsFragmentDirections.toReportPostFragment(reportContentArgs)
                    findNavController().navigate(direction)
                }
            }
            R.id.actionCopyDescription -> {
                currentFeedPost?.post?.description?.let { description ->
                    val toast = R.string.post_description_copied
                    CopyUtils.copy(requireContext(), description, toast)
                }
            }
            R.id.actionMessageAuthor -> {
                currentFeedPost?.let {
                    val chatRoomData = ChatRoomData(
                            null,
                            it.profile.id,
                            it.profile.name,
                            it.profile.avatar?.getSmall(),
                            it.profile.isBusiness)
                    openChatRoom(chatRoomData)
                }
            }
            R.id.actionSetAsAvatar -> {
                if (currentFeedPost?.profile?.isBusiness == true) {
                    viewModel.setBusinessAvatar()
                } else {
                    viewModel.setAsAvatar()
                }
            }
        }
        return true
    }

    private fun openEditPost(feedPost: FeedPost) {
        val post = feedPost.post
        val direction = if (post is StoryPost) {
            val story = ConstructStory.fromPost(post)
            GlobalSearchFragmentDirections.toStoryDescription(story)
        } else {
            GlobalSearchFragmentDirections.toCreateEditPost(post)
        }
        findNavController().navigate(direction)
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

    private fun onMessageFormAttachmentChanged(attachment: Attachment?) {
        attachment?.let { layout.inputMessageForm.setAttachment(it) }
    }

    private fun showComments(comments: List<Message>) {
        emptyCommentsHintAdapter.setVisibility(comments.isEmpty())
        commentsAdapter.setData(comments)
    }

    protected fun onReactionCountClick() {
        val feedPost = args.feedPost ?: return
        val postReactionsBottomSheet = PostReactionsBottomSheet.newInstance(feedPost).apply {
            onProfileAvatarClickListener = { postReaction ->
                postReaction.isMine?.let {
                    if (it) {
                        openStrangerProfile(postReaction.profile.id, postReaction.profile.isBusiness)
                    } else {
                        openMyProfile(postReaction.profile.isBusiness)
                    }
                }
            }
            onChatClickListener = { postReaction ->
                val chatRoomData = ChatRoomData(
                        null,
                        postReaction.profile.id,
                        postReaction.profile.name,
                        postReaction.profile.avatar?.getSmall(),
                        false)
                openChatRoom(chatRoomData)
            }
        }

        postReactionsBottomSheet.show(childFragmentManager, "reactions")
    }

    private fun addAudioCounter(id: Int) {
        Log.d("mediaview", "id $id")
        viewModel.addAudioCounter(id)
    }

    private fun onMentionClick(profileId: Long) {
        viewModel.onMentionClicked(profileId)
    }

    private fun openMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) {
            PostDetailsFragmentDirections.toMyBusinessProfileFragment()
        } else {
            PostDetailsFragmentDirections.toMyProfileFragment()
        }
        findNavController().navigate(direction)
    }

    private fun openStrangerProfile(profileId: Long, isBusiness: Boolean) {
        val direction = if (isBusiness) {
            PostDetailsFragmentDirections.toPublicBusinessProfileFragment(profileId)
        } else {
            PostDetailsFragmentDirections.toPublicProfileFragment(profileId)
        }
        findNavController().navigate(direction)
    }

    private fun openChatRoom(chatRoomData: ChatRoomData) {
        val destination = PostDetailsFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(destination)
    }

    private fun openImagePreview(media: Media.Picture) {
        val destination = PostDetailsFragmentDirections.toImagePreviewFragment(media)
        findNavController().navigate(destination)
    }

    private fun openVideoPreview(media: Media.Video) {
        val destination = PostDetailsFragmentDirections.toVideoPlayer(media)
        findNavController().navigate(destination)
    }

    private fun searchByHashtag(hashtag: String) {
        val destination = PostDetailsFragmentDirections.toHashtagSearch(hashtag)
        findNavController().navigate(destination)
    }
}