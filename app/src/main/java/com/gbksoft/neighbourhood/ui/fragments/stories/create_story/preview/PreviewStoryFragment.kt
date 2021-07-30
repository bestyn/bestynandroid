package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.preview

import android.Manifest
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentPreviewStoryBinding
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.StorySource
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.list.AudioListFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilderUtil
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.AddImagesToVideoHandler
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.AddTextFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryText
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story.AdjustStoryFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.effect_duration.SetEffectDurationFragment
import com.gbksoft.neighbourhood.ui.widgets.base.SimpleSeekBarChangeListener
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class PreviewStoryFragment : SystemBarsColorizeFragment(), AddImagesToVideoHandler {

    private lateinit var layout: FragmentPreviewStoryBinding
    private lateinit var player: SimpleExoPlayer
    private lateinit var duetTopPlayer: SimpleExoPlayer
    private lateinit var duetBottomPlayer: SimpleExoPlayer
    private lateinit var audioPlayer: SimpleExoPlayer

    private lateinit var trimAudioBottomSheet: BottomSheetBehavior<View>
    private lateinit var adjustVolumeBottomSheet: BottomSheetBehavior<View>
    private val args by navArgs<PreviewStoryFragmentArgs>()
    private val addTextFragment = AddTextFragment()

    private val progressObservable = Observable.interval(16, TimeUnit.MILLISECONDS)
    private var videoProgressDisposable: Disposable? = null
    private var audioProgressDisposable: Disposable? = null

    lateinit var constructStory: ConstructStory
    private var prevVideoProgress: Long = 0
    private var audio: Audio? = null
    private var audioEnabled = true

    private var videoVolume = 0.5f
    private var audioVolume = 0.5f

    lateinit var audioThumbHelper: AudioThumbHelper

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.setOnApplyWindowInsetsListener { _, insets ->
            layout.actionBarPositionHelper.updatePadding(top = insets.systemWindowInsetTop)
            layout.doneButtonPositionHelper.updatePadding(bottom = insets.systemWindowInsetBottom)
            layout.trimAudioBottomSheet.clRoot.updatePadding(bottom = insets.systemWindowInsetBottom)
            layout.adjustVolumeBottomSheet.clRoot.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_story, container, false)
        constructStory = args.story
        val story = constructStory


        setupView()
        createAudioPlayer()
        createVideoPlayer()
        prepareVideo()
        setupAddTextFragment()
        setClickListeners()
        subscribeAdjustStoryResult()
        subscribeSetEffectDurationResult()
        subscribeAddAudioResult()
        return layout.root
    }

    private fun setupAddTextFragment() {
        childFragmentManager.beginTransaction()
                .replace(R.id.addTextFragment, addTextFragment)
                .commit()
    }

    private fun setupTrimAudioBottomSheet() {
        trimAudioBottomSheet = BottomSheetBehavior.from(layout.trimAudioBottomSheet.clRoot)
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupAdjustAudioBottomSheet() {
        adjustVolumeBottomSheet = BottomSheetBehavior.from(layout.adjustVolumeBottomSheet.clRoot)
        adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupView() {
        checkGalleryButtonVisibility()
        checkAudioButtonStatus()
        setupTrimAudioBottomSheet()
        setupAdjustAudioBottomSheet()
        setupVolumeSeekbars()
    }

    private fun setupVolumeSeekbars() {
        layout.adjustVolumeBottomSheet.sbVideoVolume.setOnSeekBarChangeListener(SimpleSeekBarChangeListener { progress ->
            videoVolume = progress / 100f
            player.volume = videoVolume
            layout.adjustVolumeBottomSheet.sbVideoVolume.thumb = getVolumeSeekbarThumb(progress)
        })
        layout.adjustVolumeBottomSheet.sbAudioVolume.setOnSeekBarChangeListener(SimpleSeekBarChangeListener { progress ->
            audioVolume = progress / 100f
            audioPlayer.volume = audioVolume
            layout.adjustVolumeBottomSheet.sbAudioVolume.thumb = getVolumeSeekbarThumb(progress)
        })
    }

    private fun getVolumeSeekbarThumb(progress: Int): Drawable? {
        return if (progress == 0) {
            ContextCompat.getDrawable(requireContext(), R.drawable.adjust_volume_thumb)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.adjust_volume_thumb_active)
        }
    }

    private fun setClickListeners() {
        layout.ivBack.setOnClickListener {
            player.playWhenReady = false
            duetTopPlayer.playWhenReady = false
            duetBottomPlayer.playWhenReady = false
            findNavController().popBackStack()
        }
        layout.btnMute.setOnClickListener { toggleAudio() }
        layout.btnAdjustStory.setOnClickListener { adjustStory() }
        layout.ivFromGallery.setOnClickListener { checkGalleryPermissions() }
        layout.btnDone.setOnClickListener { handleOnDoneButtonClick() }
        layout.btnAddStoryText.setOnClickListener { openAddStoryText() }
        layout.btnAddStorySound.setOnClickListener { openAddAudio() }
        layout.btnAdjustVolume.setOnClickListener { openAdjustVolume() }
        layout.trimAudioBottomSheet.trimAudioView.onTimeChangedListener = { audioThumbHelper.onAudioTimeThumbChangedUri(it) }
        layout.trimAudioBottomSheet.btnDone.setOnClickListener { onAudioTrimmingDone() }
        layout.trimAudioBottomSheet.btnCloseAudioTrimming.setOnClickListener { showCancelChangesDialog() }
        layout.trimAudioBottomSheet.tvChangeTrack.setOnClickListener { navigateToAddAudio() }
        layout.adjustVolumeBottomSheet.btnCloseAdjustingVolume.setOnClickListener {
            adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            layout.btnDone.visibility = View.VISIBLE
            checkGalleryButtonVisibility()
        }
    }

    private fun createVideoPlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        duetTopPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        duetBottomPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        layout.playerView.player = player
        layout.duetTopVideoPlayer.player = duetTopPlayer
        layout.duetBottomVideoPlayer.player = duetBottomPlayer

        layout.playerView.controllerAutoShow = false
        layout.duetTopVideoPlayer.controllerAutoShow = false
        layout.duetBottomVideoPlayer.controllerAutoShow = false

        layout.playerView.useController = false
        layout.duetTopVideoPlayer.useController = false
        layout.duetBottomVideoPlayer.useController = false
    }

    private fun createAudioPlayer() {
        audioPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        audioThumbHelper = AudioThumbHelper(audioPlayer, layout.trimAudioBottomSheet.trimAudioView, requireContext())
    }

    private fun prepareVideo() {
        if (constructStory.source == StorySource.FROM_DUET) {
            prepareDuetVideo()
        } else {
            prepareVideoPlayer()
        }
    }

    private fun prepareVideoPlayer() {
        layout.llDuetPlayerView.visibility = View.GONE
        layout.playerView.visibility = View.VISIBLE
        val videoSegments = constructStory.videoSegments ?: return
        val videoSourceList = Array<MediaSource?>(videoSegments.size) { null }
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        videoSegments.forEachIndexed { pos, videoSegment ->
            var videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSegment.uri)
            val clippingMediaSource = ClippingMediaSource(videoSource, videoSegment.startTime * 1000L, videoSegment.endTime * 1000L)
            videoSourceList[pos] = clippingMediaSource
        }
        val videoSource = ConcatenatingMediaSource(*videoSourceList)
        player.prepare(LoopingMediaSource(videoSource))
        player.volume = videoVolume
        player.playWhenReady = true
    }

    private fun prepareDuetVideo() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1) {
            layout.duetTopVideoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            layout.duetBottomVideoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        layout.playerView.visibility = View.GONE
        layout.llDuetPlayerView.visibility = View.VISIBLE
        val originalVideo = constructStory.duetOriginalVideoUri ?: return
        val videoSegments = constructStory.videoSegments ?: return
        val originalVideoDuration = StoryBuilderUtil.getMediaDuration(requireContext(), originalVideo)
        val cameraVideoSegmentsDuration = constructStory.getCurrentDuration()

        Timber.tag("KEK").d("original = $originalVideoDuration")
        Timber.tag("KEK").d("camera = $cameraVideoSegmentsDuration")

        val videoSourceList = Array<MediaSource?>(videoSegments.size) { null }
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        videoSegments.forEachIndexed { pos, videoSegment ->
            var videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSegment.uri)
            val clippingMediaSource = ClippingMediaSource(videoSource, videoSegment.startTime * 1000L, videoSegment.endTime * 1000L)
            videoSourceList[pos] = clippingMediaSource
        }
        val videoSource = ConcatenatingMediaSource(*videoSourceList)
        val originalVideoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(originalVideo)

        duetTopPlayer.prepare(originalVideoSource)
        duetBottomPlayer.prepare(LoopingMediaSource(videoSource))
        duetTopPlayer.volume = videoVolume
        duetBottomPlayer.volume = videoVolume

        Handler().postDelayed({
            duetTopPlayer.playWhenReady = true
            duetBottomPlayer.playWhenReady = true
        }, 500)
    }

    private fun getNavigationBarHeight(): Int {
        val defaultDisplay = activity?.windowManager?.defaultDisplay;
        val metrics = DisplayMetrics()
        defaultDisplay?.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        defaultDisplay?.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        return if (realHeight > usableHeight) realHeight - usableHeight else 0
    }

    private fun handleOnDoneButtonClick() {
        prepareStoryTextModels()
        prepareAudio()
        openAddStoryDescription(constructStory)
    }

    private fun prepareStoryTextModels() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels + getNavigationBarHeight()
        val screenWidth = displayMetrics.widthPixels
        constructStory.screenDimension = screenWidth to screenHeight
        constructStory.textModels = addTextFragment.getStoryTextModels()
    }

    private fun prepareAudio() {
        constructStory.audio = audio
        constructStory.isAudioEnabled = audioEnabled
        constructStory.audioVolume = audioVolume
        constructStory.videoVolume = videoVolume
    }

    private fun checkGalleryButtonVisibility() {
        layout.ivFromGallery.visibility = if (constructStory.source == StorySource.FROM_GALLERY) View.VISIBLE else View.GONE
    }

    private fun checkAudioButtonStatus() {
        if (audio?.url == null) {
            layout.btnAdjustVolume.visibility = View.GONE
        } else {
            layout.btnAdjustVolume.visibility = View.VISIBLE
        }
    }

    private fun toggleAudio() {
        if (audioEnabled) {
            audioEnabled = false
            player.volume = 0f
            layout.btnMute.setImageResource(R.drawable.ic_muted)
        } else {
            audioEnabled = true
            player.volume = 100f
            layout.btnMute.setImageResource(R.drawable.ic_unmuted)
        }
    }

    override fun onInputFieldOpen() {
        layout.ivBack.visibility = View.GONE
        layout.options.visibility = View.GONE
        layout.btnDone.visibility = View.GONE
        layout.ivFromGallery.visibility = View.GONE
    }

    override fun onInputFieldClosed() {
        layout.ivBack.visibility = View.VISIBLE
        layout.options.visibility = View.VISIBLE
        layout.btnDone.visibility = View.VISIBLE
        checkGalleryButtonVisibility()
    }

    private fun adjustStory() {
        val storyTextModels = addTextFragment.getStoryTextModels()
        if (storyTextModels.isEmpty()) {
            navigateToAdjustStory()
        } else {
            showAdjustStoryDialog()
        }
    }

    private fun navigateToAdjustStory() {
        addTextFragment.clearStoryTexts()
        player.playWhenReady = false
        duetTopPlayer.playWhenReady = false
        duetBottomPlayer.playWhenReady = false
        val direction = PreviewStoryFragmentDirections.toAdjustStory(constructStory, true)
        findNavController().navigate(direction)
    }

    private fun showAdjustStoryDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.adjust_story_clip_dialog_cancel, null)
                .setPositiveButton(R.string.adjust_story_clip_dialog_yes) { navigateToAdjustStory() }
                .setMessage(R.string.adjust_story_clip_dialog_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun openAddStoryDescription(story: ConstructStory) {
        Timber.tag("Creating story").d("$story")
        player.playWhenReady = false
        duetTopPlayer.playWhenReady = false
        duetBottomPlayer.playWhenReady = false
        val direction = PreviewStoryFragmentDirections.toStoryDescription(story)
        findNavController().navigate(direction)
    }

    override fun navigateToSetEffectDuration(storyText: StoryText) {
        player.playWhenReady = false
        duetTopPlayer.playWhenReady = false
        duetBottomPlayer.playWhenReady = false
        val direction = PreviewStoryFragmentDirections.toSetEffectDuration(constructStory, storyText)
        findNavController().navigate(direction)
    }

    private fun openAddStoryText() {
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        addTextFragment.startAddingTest()
    }

    private fun onAudioTrimmingDone() {
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        layout.btnDone.visibility = View.VISIBLE
        constructStory.audio = audio
        checkGalleryButtonVisibility()
    }

    private fun showCancelChangesDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.adjust_story_cancel_changes_dialog_cancel, null)
                .setPositiveButton(R.string.adjust_story_cancel_changes_dialog_ok) { trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN }
                .setMessage(R.string.adjust_story_cancel_changes_dialog_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun subscribeAdjustStoryResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<ConstructStory>>(AdjustStoryFragment.ADJUST_STORY_RESULT)
                ?.observe(viewLifecycleOwner, Observer(this::handleAdjustStoryResult))
    }

    private fun handleAdjustStoryResult(resultData: ResultData<ConstructStory>) {
        constructStory = resultData.consumeData() ?: return
        player.playWhenReady = false
        player.release()
        setupView()
        createAudioPlayer()
        createVideoPlayer()
        prepareVideo()
        setupAddTextFragment()
        setClickListeners()
        subscribeAdjustStoryResult()
        subscribeSetEffectDurationResult()
        subscribeAddAudioResult()
    }

    private fun subscribeSetEffectDurationResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<StoryText>>(SetEffectDurationFragment.SET_EFFECT_DURATION_RESULT)
                ?.observe(viewLifecycleOwner, Observer(this::handleSetEffectDurationResult))
    }

    private fun handleSetEffectDurationResult(resultData: ResultData<StoryText>) {
        val storyText = resultData.consumeData() ?: return
        addTextFragment.updateStoryText(storyText)
    }


    private fun subscribeAddAudioResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<Audio>>(AudioListFragment.ADD_AUDIO_RESULT)
                ?.observe(viewLifecycleOwner, Observer { handleAddAudioListResult(it) })
    }

    private fun handleAddAudioListResult(resultData: ResultData<Audio>) {
        this.audio = resultData.consumeData() ?: return
        val audioUri = audio?.fileUri ?: return

        checkGalleryButtonVisibility()
        checkAudioButtonStatus()

        player.release()
        audioPlayer.release()

        createAudioPlayer()
        createVideoPlayer()
        setupView()
        prepareVideo()

        audioThumbHelper.initUri(audioUri)

        layout.trimAudioBottomSheet.trimAudioView.setAudioUri(audioUri)
        layout.trimAudioBottomSheet.tvAudioDescription.text = audio?.description
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        layout.btnDone.visibility = View.GONE
        layout.ivFromGallery.visibility = View.GONE
    }

    private fun subscribeVideoProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = progressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val curWindow = player.currentWindowIndex
                    val curProgress = player.currentPosition

                    val videoSegments = constructStory.videoSegments ?: return@subscribe
                    val totalProgress = MediaUtils.getTotalProgress(videoSegments, curWindow, curProgress)

                    addTextFragment.setCurrentProgress(totalProgress)
                    if (abs(totalProgress - prevVideoProgress) > constructStory.getCurrentDuration() / 2) {
                        audioPlayer.seekTo(0)
                    }
                    prevVideoProgress = totalProgress
                }
    }

    private fun subscribeDuetStoryProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = progressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val curWindow = duetBottomPlayer.currentWindowIndex
                    val curProgress = duetBottomPlayer.currentPosition

                    val videoSegments = constructStory.videoSegments ?: return@subscribe
                    val totalProgress = MediaUtils.getTotalProgress(videoSegments, curWindow, curProgress)

                    addTextFragment.setCurrentProgress(totalProgress)
                    if (abs(totalProgress - prevVideoProgress) > constructStory.getCurrentDuration() / 2) {
                        audioPlayer.seekTo(0)
                        duetTopPlayer.seekTo(0)
                    }
                    prevVideoProgress = totalProgress
                }
    }

    private fun checkGalleryPermissions() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = {
            showCreateStoryFromGalleryDialog()
        }
        permissionListener.onPermissionToken = { it.continuePermissionRequest() }

        Dexter.withContext(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(permissionListener)
                .withErrorListener { error: DexterError ->
                    ToastUtils.showToastMessage(requireActivity(), "Error occurred: $error")
                }
                .onSameThread()
                .check()
    }

    private fun showCreateStoryFromGalleryDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.create_story_from_gallery_dialog_cancel, null)
                .setPositiveButton(R.string.create_story_from_gallery_dialog_yes) {
                    val direction = PreviewStoryFragmentDirections.toImageVideoPicker(false)
                    findNavController().navigate(direction)
                }
                .setMessage(R.string.create_story_from_gallery_dialog_msg)
                .setTitle(R.string.create_story_from_gallery_dialog_title)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun openAddAudio() {
        if (audio == null) {
            navigateToAddAudio()
        } else {
            layout.btnDone.visibility = View.GONE
            layout.ivFromGallery.visibility = View.GONE
            adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            trimAudioBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun openAdjustVolume() {
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        layout.btnDone.visibility = View.GONE
        layout.ivFromGallery.visibility = View.GONE
    }

    private fun navigateToAddAudio() {
        player.playWhenReady = false
        duetTopPlayer.playWhenReady = false
        duetBottomPlayer.playWhenReady = false
        val direction = PreviewStoryFragmentDirections.toAudioList()
        findNavController().navigate(direction)
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
        audioPlayer.playWhenReady = false
        videoProgressDisposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
        audioPlayer.playWhenReady = true
        if (constructStory.source == StorySource.FROM_DUET) {
            subscribeDuetStoryProgress()
        } else {
            subscribeVideoProgress()
        }
        audioThumbHelper.subscribeAudioProgress(false)
    }

    override fun onDestroyView() {
        player.release()
        audioPlayer.release()
        super.onDestroyView()
    }
}