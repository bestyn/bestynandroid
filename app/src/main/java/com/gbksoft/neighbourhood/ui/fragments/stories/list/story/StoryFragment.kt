package com.gbksoft.neighbourhood.ui.fragments.stories.list.story

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.databinding.FragmentStoryBinding
import com.gbksoft.neighbourhood.mappers.audio.AudioMapper
import com.gbksoft.neighbourhood.model.audio.AudioStories
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.StoryPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.activities.main.FloatingMenuDelegate
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.stories.StoriesViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.StoryCommentsBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.stories.StoryOptionsBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.CreateStoryHandler
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.BaseStoryListFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.popup.StoryReactionPopup
import com.gbksoft.neighbourhood.utils.CopyUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.permission.DexterMultiplePermissionListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.karumi.dexter.Dexter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

private const val KEY_STORY = "key_story"
private const val SHOW_BACK_BUTTON = "show_back_button"

class StoryFragment : BaseFragment() {

    private val storiesViewModel: StoriesViewModel by lazy {
        requireParentFragment().requireParentFragment().getViewModel<StoriesViewModel>()
    }
    private val downloadViewModel by viewModel<DownloadViewModel>()
    private lateinit var layout: FragmentStoryBinding
    private lateinit var player: SimpleExoPlayer
    private val simpleCache: SimpleCache by inject()
    private var videoProgressDisposable: Disposable? = null
    private val storyLiveData = MutableLiveData<FeedPost>()
    private val storyReactionPopup by lazy {
        val popup = StoryReactionPopup(layout.root.context)
        popup.onReactionClickListener = this::onReactionSelected
        popup
    }
    private var authorInfoBottomPadding: Int = 0
    private var topInset: Int = 0
    private var storyCommentsFragment: StoryCommentsBottomSheet? = null
    private var showBackButton = false

    private val observable = Observable
            .interval(16, TimeUnit.MILLISECONDS)

    companion object {
        fun newInstance(story: FeedPost, showBackButton: Boolean = false): StoryFragment {
            val args = Bundle()
            val storyFragment = StoryFragment()
            args.putParcelable(KEY_STORY, story)
            args.putBoolean(SHOW_BACK_BUTTON, showBackButton)
            storyFragment.arguments = args
            return storyFragment
        }
    }

    fun setStory(story: FeedPost) {
        arguments?.run {
            putParcelable(KEY_STORY, story)
        }
        storyLiveData.value = story
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        storyLiveData.value = arguments?.getParcelable(KEY_STORY)
        showBackButton = arguments?.getBoolean(SHOW_BACK_BUTTON) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_story, container, false)
        subscribeToInsets()
        setClickListeners()
        subscribeStoryLiveData()
        subscribeViewModel()

        return layout.root
    }

    private fun subscribeToInsets() {
        authorInfoBottomPadding = layout.authorInfo.paddingBottom
        (parentFragment as? BaseStoryListFragment)?.getInsetsLiveData()?.observe(viewLifecycleOwner,
                Observer { setWindowInsets(it) })
    }

    private fun setWindowInsets(insets: WindowInsets) {
        topInset = insets.systemWindowInsetTop
        layout.storyMenu.updatePadding(top = insets.systemWindowInsetTop)
        layout.btnBack.updatePadding(top = insets.systemWindowInsetTop)
        layout.authorInfo.updatePadding(bottom = authorInfoBottomPadding + insets.systemWindowInsetBottom)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnApplyWindowInsetsListener(view)
    }

    private fun setOnApplyWindowInsetsListener(view: View) {
        val authorInfoBottomPadding = layout.authorInfo.paddingBottom
        view.setOnApplyWindowInsetsListener { v, insets ->
            layout.storyMenu.updatePadding(top = insets.systemWindowInsetTop)
            layout.authorInfo.updatePadding(bottom = authorInfoBottomPadding + insets.systemWindowInsetTop)
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    override fun onDestroyView() {
        player.release()
        storyCommentsFragment = null
        layout.playerView.player = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        startPlaying()
        subscribeVideoProgress()
    }

    override fun onPause() {
        super.onPause()
        stopPlaying()
        unsubscribeVideoProgress()
    }

    private fun subscribeStoryLiveData() {
        storyLiveData.observe(viewLifecycleOwner, Observer {
            Timber.tag("StoryTag").d("storyLiveData updated")
            setupView(it)
            prepareVideo(it)
        })
    }

    private fun setupView(story: FeedPost) {
        layout.model = story
        layout.tvDescription.setCollapsedText(story.getDescription())
        layout.tvDescription.visibility = if (TextUtils.isEmpty(story.getDescription())) View.GONE else View.VISIBLE
        layout.tvAddress.visibility = if (TextUtils.isEmpty(story.getAddress())) View.GONE else View.VISIBLE
        layout.playerView.player = player
        layout.audioStory.visibility = if (story.audio == null || story.audio?.url.isNullOrEmpty()) View.GONE else View.VISIBLE
        layout.tvAudioStory.text = story.audio?.description
        layout.btnBack.visibility = if (showBackButton) View.VISIBLE else View.GONE
    }

    private var currentVideoUri: Uri? = null

    private fun prepareVideo(story: FeedPost) {
        val videoUri = story.post.media.first().origin
        if (videoUri == currentVideoUri) return

        currentVideoUri = videoUri
        val cacheDataSourceFactory = CacheDataSourceFactory(
                simpleCache,
                DefaultHttpDataSourceFactory(context?.let {
                    Util.getUserAgent(
                            it, getString(
                            R.string.app_name))
                }),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri)

        player.prepare(LoopingMediaSource(mediaSource))
    }

    private fun subscribeVideoProgress() {
        videoProgressDisposable = observable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map { player.currentPosition }
                .subscribe {
                    val progressVideo = it.toFloat() / player.duration.toFloat() * 100
                    layout.storyProgressBar.setProgress(progressVideo)
                }
    }

    private fun unsubscribeVideoProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = null
    }

    private fun setClickListeners() {
        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false
        layout.playerView.videoSurfaceView?.setOnClickListener { switchPlaying() }
        layout.btnPlay.setOnClickListener { startPlaying() }
        layout.avatar.setOnClickListener { openProfile() }
        layout.tvAuthorName.setOnClickListener { openProfile() }
        layout.tvDescription.setOnClickListener { openProfile() }
        layout.tvDescription.onHashTagClickListener = { openSearchByHashtag(it) }
        layout.ivLike.setOnClickListener { onReactionClick(it) }
        layout.btnMute.setOnClickListener { toggleAudio() }
        layout.btnCreateStory.setOnClickListener {
            when {
                FloatingMenuDelegate.userIsRecordingAudio.not() && FloatingMenuDelegate.storyIsPublishing.not() -> {
                    openCreateStoryScreen()
                }
                FloatingMenuDelegate.userIsRecordingAudio -> {
                    ToastUtils.showToastMessage(getString(R.string.you_can_not_create_new_post))
                }
                FloatingMenuDelegate.storyIsPublishing -> {
                    ToastUtils.showToastMessage(getString(R.string.sorry_your_story_is_publishing))
                }
            }
        }
        layout.ivFollowers.setOnClickListener { onFollowersClick() }
        layout.ivComments.setOnClickListener { onStoryCommentClick() }
        layout.btnMenu.setOnClickListener {
            showStoryOptionsBottomSheet()
        }
        layout.imageAudioStory.setOnClickListener { if (showBackButton) findNavController().popBackStack() else storyLiveData.value?.audio?.let { it1 -> openAudioDetails(it1) } }
        layout.tvAudioStory.setOnClickListener { if (showBackButton) findNavController().popBackStack() else storyLiveData.value?.audio?.let { it1 -> openAudioDetails(it1) } }
        layout.btnBack.setOnClickListener { findNavController().popBackStack() }
        layout.tvDescription.onMentionClickListener = { storiesViewModel.onMentionClicked(it) }
    }

    private fun switchPlaying() {
        if (player.isPlaying) stopPlaying()
        else startPlaying()
    }

    private fun startPlaying() {
        player.playWhenReady = true
        layout.btnPlay.visibility = View.GONE
    }

    private fun stopPlaying() {
        player.playWhenReady = false
        layout.btnPlay.visibility = View.VISIBLE
    }

    private fun openProfile() {
        val story = storyLiveData.value ?: return
        if (story.isMine == true) {
            openMyProfile(story.profile.isBusiness)
        } else {
            openStrangerProfile(story.profile.id, story.profile.isBusiness)
        }
    }

    private fun openSearchByHashtag(hashtag: String) {
        (parentFragment as? StoryNavigationHandler)?.openSearchByHashtag(hashtag)
    }

    private fun onReactionClick(view: View) {
        val myReaction = storyLiveData.value?.myReaction ?: return
        if (myReaction == Reaction.NO_REACTION) {
            storyReactionPopup.show(layout.root, view)
        } else {
            val currentStoryListFragment = getCurrentStoryListFragment()
            (currentStoryListFragment as? StoryActionListener)?.onReactionButtonClicked(myReaction)
        }
    }

    private fun onReactionSelected(reaction: Reaction) {
        val currentStoryListFragment = getCurrentStoryListFragment()
        (currentStoryListFragment as? StoryActionListener)?.onReactionButtonClicked(reaction)
    }

    private fun toggleAudio() {
        storiesViewModel.toggleAudio()
    }

    private fun openCreateStoryScreen() {
        val isCreatingStory = (getParentActivity() as? CreateStoryHandler)?.isCreatingStory()
        if (isCreatingStory == true) {
            showStoryIsPublishingMessage()
            return
        }
        if (sharedStorage.isFirstStoryCreating()) {
            sharedStorage.setFirstStoryCreating(false)
            showFirstStoryCreatingDialog()
        } else {
            goToCreateStory()
        }
    }

    private fun showFirstStoryCreatingDialog() {
        YesNoDialog.Builder()
                .setTitle(R.string.dialog_title_first_story_creating)
                .setMessage(R.string.dialog_msg_first_story_creating)
                .setPositiveButton(R.string.dialog_btn_first_story_creating) { goToCreateStory() }
                .build()
                .show(childFragmentManager, "FirstStoryCreatingDialog")
    }


    private fun showStoryIsPublishingMessage() {
        ToastUtils.showToastMessage(R.string.toast_story_publishing)
    }

    private fun goToCreateStory() {
        (parentFragment as? StoryNavigationHandler)?.openCreateStory()
    }

    private fun onFollowersClick() {
        val currentStoryListFragment = getCurrentStoryListFragment()
        (currentStoryListFragment as? StoryActionListener)?.onFollowButtonClicked()
    }

    private fun onStoryCommentClick() {
        val story = storyLiveData.value ?: return

        storyCommentsFragment?.let {
            it.show(childFragmentManager, "StoryCommentsFragment")
            return
        }

        val fragment = StoryCommentsBottomSheet.newInstance(story, topInset).apply {
            onUpdateCommentsCounterListener = {
                (this@StoryFragment.parentFragment as? StoryActionListener)?.updateStoryCommentsCounter(it)
            }
        }
        storyCommentsFragment = fragment
        fragment.show(childFragmentManager, "StoryCommentsFragment")
    }

    private fun showStoryOptionsBottomSheet() {
        val story = storyLiveData.value ?: return
        val bottomSheet = StoryOptionsBottomSheet.newInstance(story).apply {
            onDownloadVideoClickListener = ::downloadVideo
            onMessageAuthorClickListener = ::openChatRoom
            onCreateDuetClickListener = ::createDuet
            onCopyDescriptionClickListener = ::copyDescription
            onReportStoryPostClickListener = ::reportStory
            onEditStoryClickListener = ::editStory
            onDeleteStoryClickListener = ::deleteStory
            onUnfollowStoryClickListener = ::unfollowStory
        }
        bottomSheet.show(childFragmentManager, "StoryOptionsBottomSheet")
    }


    private fun downloadVideo() {
        val story = storyLiveData.value ?: return
        val video = story.post.media.first() as Media.Video
        downloadViewModel.download(video.origin)
    }

    private fun openChatRoom() {
        val story = storyLiveData.value ?: return
        val chatRoomData = ChatRoomData(
                null,
                story.profile.id,
                story.profile.name,
                story.profile.avatar?.getSmall(),
                story.profile.isBusiness
        )
        (parentFragment as? StoryNavigationHandler)?.openChatRoom(chatRoomData)
    }

    private fun createDuet() {
        val video = currentVideoUri ?: return
        checkCameraAndMicPermissions {
            val root = File(NApplication.context.filesDir, "story")
            if (!root.exists()) {
                root.mkdirs()
            }
            val filePath = "$root/${MediaUtils.generateFileName("duet_story")}.mp4"
            layout.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                withContext(Dispatchers.IO) { MediaUtils.downloadAndSaveToFile(video.toString(), filePath) }
                layout.progressBar.visibility = View.GONE
                (parentFragment as? StoryNavigationHandler)?.openCreteDuetStory(File(filePath).toUri())
            }
        }
    }

    private fun checkCameraAndMicPermissions(onPermissionChecked: () -> Unit) {
        val permissionListener = DexterMultiplePermissionListener()
        permissionListener.onPermissionsChecked = { report ->
            if (report.areAllPermissionsGranted()) {
                onPermissionChecked.invoke()
            }
            if (report.isAnyPermissionPermanentlyDenied) {
                showAllowCameraAndMicDialog()
            }
        }
        permissionListener.onPermissionToken = {
            it.continuePermissionRequest()
        }
        Dexter.withContext(context)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(permissionListener)
                .onSameThread()
                .check()
    }

    private fun showAllowCameraAndMicDialog() {
        val builder = YesNoDialog.Builder()
                .setTitle(R.string.story_media_camera_permission_title)
                .setMessage(R.string.story_media_camera_permission_msg)
                .setNegativeButton(R.string.story_media_permission_required_cancel_btn, null)
                .setPositiveButton(R.string.story_media_permission_required_settings_btn) {
                    openAppSettings()
                }
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeleteMediaPostDialog")
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
        startActivity(intent)
    }

    private fun copyDescription() {
        val story = storyLiveData.value ?: return
        val toast = R.string.post_description_copied
        CopyUtils.copy(requireContext(), story.post.description, toast)
    }

    private fun reportStory() {
        val story = storyLiveData.value ?: return
        val reportContentArgs = ReportContentArgs.fromPost(story.post)
        (parentFragment as? StoryNavigationHandler)?.openReportStory(reportContentArgs)
    }

    private fun editStory() {
        val story = storyLiveData.value ?: return
        val post = story.post
        if (post is StoryPost) {
            val constructStory = ConstructStory.fromPost(post.apply { allowedDuet = story.allowedDuet })
            (parentFragment as? StoryNavigationHandler)?.openEditStory(constructStory)
        }
    }

    private fun deleteStory() {
        val story = storyLiveData.value ?: return
        val currentStoryListFragment = getCurrentStoryListFragment()
        (currentStoryListFragment as? StoryActionListener)?.deleteStory(story)
    }

    private fun unfollowStory() {
        val currentStoryListFragment = getCurrentStoryListFragment()
        (currentStoryListFragment as? StoryActionListener)?.onUnfollowButtonClicked()
    }

    private fun getCurrentStoryListFragment(): Fragment? {
        return parentFragment
    }

    private fun subscribeViewModel() {
        storiesViewModel.isAudioEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
            if (isEnabled) {
                layout.btnMute.setImageResource(R.drawable.ic_unmuted)
                player.volume = 100f
            } else {
                layout.btnMute.setImageResource(R.drawable.ic_muted)
                player.volume = 0f
            }
        })

        storiesViewModel.navigateToMyProfile.observe(viewLifecycleOwner, Observer { openMyProfile(false) })
        storiesViewModel.navigateToMyBusinessProfile.observe(viewLifecycleOwner, Observer { openMyProfile(true) })
        storiesViewModel.navigateToPublicProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, false) })
        storiesViewModel.navigateToPublicBusinessProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, true) })
    }

    private fun openMyProfile(isBusiness: Boolean) {
        if (isBusiness) {
            (parentFragment as? StoryNavigationHandler)?.openMyBusinessProfile()
        } else {
            (parentFragment as? StoryNavigationHandler)?.openMyProfile()
        }
    }

    private fun openStrangerProfile(profileId: Long, isBusiness: Boolean) {
        if (isBusiness) {
            (parentFragment as? StoryNavigationHandler)?.openPublicBuisinessProfile(profileId)
        } else {
            (parentFragment as? StoryNavigationHandler)?.openPublicProfile(profileId)
        }
    }

    private fun openAudioDetails(audio: AudioStories) {
        val mAudio = AudioMapper.toAudio(audio)
        (parentFragment as? StoryNavigationHandler)?.openAudioDetails(mAudio)
    }
}