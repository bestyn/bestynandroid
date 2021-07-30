package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.text_story

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.arthenica.mobileffmpeg.FFmpeg
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentCreateTextStoryBinding
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.StoryTime
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.list.AudioListFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.AddImagesToVideoHandler
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.AddTextFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryText
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.effect_duration.SetEffectDurationFragment
import com.gbksoft.neighbourhood.ui.widgets.base.SimpleSeekBarChangeListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CreateTextStoryFragment : SystemBarsColorizeFragment(), AddImagesToVideoHandler {

    private lateinit var layout: FragmentCreateTextStoryBinding
    private val addTextFragment = AddTextFragment()

    private val progressObservable = Observable.interval(16, TimeUnit.MILLISECONDS)
    private var videoProgressDisposable: Disposable? = null

    private var textStoryProgress = 0L
    private var textStoryDuration = StoryTime.SEC_60
    private var storyBackground = StoryBackground.BACKGROUND_1

    private lateinit var audioPlayer: SimpleExoPlayer

    private var audioProgressDisposable: Disposable? = null
    private var audio: Audio? = null
    private var audioVolume = 1f

    private lateinit var trimAudioBottomSheet: BottomSheetBehavior<View>
    private lateinit var adjustVolumeBottomSheet: BottomSheetBehavior<View>

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_create_text_story, container, false)
        layout.recordTimeSwitcher.currentTime = textStoryDuration
        audioPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        applyBackground(storyBackground)
        setClickListeners()
        setupAddTextFragment()
        setupTrimAudioBottomSheet()
        setupAdjustAudioBottomSheet()
        subscribeSetEffectDurationResult()
        subscribeAddAudioResult()
        setupVolumeSeekbars()
        checkAudioButtonStatus()
        return layout.root
    }

    private fun setupTrimAudioBottomSheet() {
        trimAudioBottomSheet = BottomSheetBehavior.from(layout.trimAudioBottomSheet.clRoot)
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupAdjustAudioBottomSheet() {
        adjustVolumeBottomSheet = BottomSheetBehavior.from(layout.adjustVolumeBottomSheet.clRoot)
        layout.adjustVolumeBottomSheet.sbVideoVolume.isEnabled = false
        ContextCompat.getDrawable(requireContext(), R.drawable.adjust_volume_thumb)
        adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupVolumeSeekbars() {
        layout.adjustVolumeBottomSheet.sbVideoVolume.progress = 0
        layout.adjustVolumeBottomSheet.sbVideoVolume.thumb = getVolumeSeekbarThumb(0)
        layout.adjustVolumeBottomSheet.sbAudioVolume.progress = (audioVolume * 100).toInt()
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

    private fun setupAddTextFragment() {
        childFragmentManager.beginTransaction()
                .replace(R.id.addTextFragment, addTextFragment)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        subscribeVideoProgress()
    }

    override fun onPause() {
        super.onPause()
        videoProgressDisposable?.dispose()
        FFmpeg.cancel()
        layout.progressBar.visibility = View.GONE
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.setOnApplyWindowInsetsListener { v, insets ->
            layout.actionBarPositionHelper.updatePadding(top = insets.systemWindowInsetTop)
            layout.doneButtonPositionHelper.updatePadding(bottom = insets.systemWindowInsetBottom)
            layout.trimAudioBottomSheet.clRoot.updatePadding(bottom = insets.systemWindowInsetBottom)
            layout.adjustVolumeBottomSheet.clRoot.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    private fun setClickListeners() {
        layout.ivBack.setOnClickListener { findNavController().popBackStack() }
        layout.btnAddStoryText.setOnClickListener { openAddStoryText() }
        layout.btnStoryBackground.setOnClickListener { handleStoryBackgroundButtonClick() }
        layout.storyBackgroundPicker.onBackgroundClickListener = { applyBackground(it) }
        layout.recordTimeSwitcher.timeSwitchListener = { textStoryDuration = it }
        layout.btnAddStorySound.setOnClickListener { openAddAudio() }
        layout.btnAdjustVolume.setOnClickListener { openAdjustVolume() }
        layout.trimAudioBottomSheet.trimAudioView.onTimeChangedListener = { onAudioTimeThumbChanged(it) }
        layout.trimAudioBottomSheet.btnDone.setOnClickListener {
            trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            layout.btnDone.visibility = View.VISIBLE
        }
        layout.trimAudioBottomSheet.btnCloseAudioTrimming.setOnClickListener { showCancelChangesDialog() }
        layout.trimAudioBottomSheet.tvChangeTrack.setOnClickListener { navigateToAddAudio() }
        layout.adjustVolumeBottomSheet.btnCloseAdjustingVolume.setOnClickListener {
            adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            layout.btnDone.visibility = View.VISIBLE
        }
        layout.btnDone.setOnClickListener { handleOnDoneButtonClick() }
    }

    private fun openAddStoryText() {
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        addTextFragment.startAddingTest()
    }

    private fun openAddAudio() {
        if (audio == null) {
            navigateToAddAudio()
        } else {
            layout.btnDone.visibility = View.GONE
            adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            trimAudioBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun openAdjustVolume() {
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        adjustVolumeBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        layout.btnDone.visibility = View.GONE
    }

    private fun onAudioTimeThumbChanged(timeInMs: Int) {
        audio?.startTime = timeInMs
        prepareAudioPlayer()
        layout.trimAudioBottomSheet.trimAudioView.skipFirstLevels(timeInMs)
    }

    private fun showCancelChangesDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.adjust_story_cancel_changes_dialog_cancel, null)
                .setPositiveButton(R.string.adjust_story_cancel_changes_dialog_ok) { trimAudioBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN }
                .setMessage(R.string.adjust_story_cancel_changes_dialog_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun checkAudioButtonStatus() {
        if (audio == null) {
            layout.btnAdjustVolume.visibility = View.GONE
        } else {
            layout.btnAdjustVolume.visibility = View.VISIBLE
        }
    }

    private fun applyBackground(storyBackground: StoryBackground) {
        this.storyBackground = storyBackground
        layout.ivStoryBackground.setImageResource(storyBackground.backgroundResId)
    }

    private fun handleStoryBackgroundButtonClick() {
        if (layout.storyBackgroundPicker.visibility == View.VISIBLE) {
            hideStoryBackground()
        } else {
            showStoryBackgroundPicker()
        }
    }

    private fun showStoryBackgroundPicker() {
        layout.storyBackgroundPicker.visibility = View.VISIBLE
        layout.btnDone.visibility = View.GONE
        layout.recordTimeSwitcher.visibility = View.GONE
        layout.btnStoryBackground.setBackgroundResource(R.drawable.bg_add_story_text_selected_item)
    }

    private fun hideStoryBackground() {
        layout.storyBackgroundPicker.visibility = View.GONE
        layout.btnDone.visibility = View.VISIBLE
        layout.recordTimeSwitcher.visibility = View.VISIBLE
        layout.btnStoryBackground.setBackgroundResource(0)
    }

    private fun handleOnDoneButtonClick() {
        val storyTime = layout.recordTimeSwitcher.currentTime
        val constructStory = ConstructStory.fromTextStory(storyBackground, storyTime)
        prepareStoryTextModels(constructStory)
        prepareStoryAudio(constructStory)
        navigateToAddDescription(constructStory)
    }

    private fun prepareStoryTextModels(constructStory: ConstructStory) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels + getNavigationBarHeight()
        val screenWidth = displayMetrics.widthPixels
        constructStory.screenDimension = screenWidth to screenHeight
        constructStory.textModels = addTextFragment.getStoryTextModels()
    }

    private fun prepareStoryAudio(constructStory: ConstructStory) {
        constructStory.audio = audio
        constructStory.audioVolume = audioVolume
        constructStory.videoVolume = 0f
    }

    override fun onInputFieldOpen() {
        layout.ivBack.visibility = View.GONE
        layout.options.visibility = View.GONE
        layout.recordTimeSwitcher.visibility = View.GONE
        layout.btnDone.visibility = View.GONE
        layout.storyBackgroundPicker.visibility = View.GONE
        layout.btnStoryBackground.setBackgroundResource(0)
    }

    override fun onInputFieldClosed() {
        layout.ivBack.visibility = View.VISIBLE
        layout.options.visibility = View.VISIBLE
        layout.recordTimeSwitcher.visibility = View.VISIBLE
        layout.btnDone.visibility = View.VISIBLE
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
        textStoryProgress = 0
    }

    private fun subscribeAddAudioResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<Audio>>(AudioListFragment.ADD_AUDIO_RESULT)
                ?.observe(viewLifecycleOwner, Observer { handleAddAudioListResult(it) })
    }

    private fun handleAddAudioListResult(resultData: ResultData<Audio>) {
        val audio = resultData.consumeData() ?: return
        val audioUri = audio.fileUri ?: return
        this.audio = audio
        textStoryProgress = 0

        prepareAudioPlayer()
        layout.trimAudioBottomSheet.trimAudioView.setAudioUri(audioUri)
        layout.trimAudioBottomSheet.tvAudioDescription.text = audio.description
        trimAudioBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        layout.btnDone.visibility = View.GONE
        checkAudioButtonStatus()
    }

    private fun prepareAudioPlayer() {
        val uri = audio?.fileUri ?: return
        val duration = audio?.fileDuration ?: return
        val startTime = audio?.startTime ?: return

        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        val clippingSource = ClippingMediaSource(audioSource, startTime * 1000L, duration * 1000L)
        val loopingSource = LoopingMediaSource(clippingSource)

        audioPlayer.prepare(loopingSource)
        audioPlayer.volume = audioVolume
        audioPlayer.playWhenReady = true
        subscribeAudioProgress()
    }

    private fun subscribeAudioProgress() {
        if (audio == null) {
            return
        }
        audioProgressDisposable?.dispose()
        audioProgressDisposable = progressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val audioStartTime = audio?.startTime ?: return@subscribe
                    val progress = audioPlayer.currentPosition + audioStartTime
                    layout.trimAudioBottomSheet.trimAudioView.setPlayingProgress(progress)
                }
    }

    private fun subscribeVideoProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = progressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    textStoryProgress += 16
                    if (textStoryProgress > textStoryDuration.millis) {
                        textStoryProgress = 0
                        audioPlayer.seekTo(0)
                    }
                    addTextFragment.setCurrentProgress(textStoryProgress)
                }
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

    override fun navigateToSetEffectDuration(storyText: StoryText) {
        val storyBackground = layout.storyBackgroundPicker.selectedBackground
        val storyTime = layout.recordTimeSwitcher.currentTime
        val constructStory = ConstructStory.fromTextStory(storyBackground, storyTime)
        val direction = CreateTextStoryFragmentDirections.toSetEffectDuration(constructStory, storyText)
        findNavController().navigate(direction)
    }

    private fun navigateToAddAudio() {
        val direction = CreateTextStoryFragmentDirections.toAudioList()
        findNavController().navigate(direction)
    }

    private fun navigateToAddDescription(constructStory: ConstructStory) {
        val direction = CreateTextStoryFragmentDirections.toStoryDescription(constructStory)
        findNavController().navigate(direction)
    }

    override fun onStop() {
        super.onStop()
        audioPlayer.playWhenReady = false
        audioProgressDisposable?.dispose()
        videoProgressDisposable?.dispose()
    }

    override fun onStart() {
        super.onStart()
        audioPlayer.playWhenReady = true
        subscribeVideoProgress()
        subscribeAudioProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}