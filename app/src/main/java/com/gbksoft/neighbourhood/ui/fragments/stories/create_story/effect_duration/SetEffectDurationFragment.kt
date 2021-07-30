package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.effect_duration

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.util.isNotEmpty
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentSetEffectDurationBinding
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.StorySource
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilderUtil
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryText
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryTextHelper
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story.VideoSegmentsManager
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SetEffectDurationFragment : SystemBarsColorizeFragment() {

    private lateinit var layout: FragmentSetEffectDurationBinding
    private val args by navArgs<SetEffectDurationFragmentArgs>()

    private lateinit var player: SimpleExoPlayer
    private lateinit var duetTopPlayer: SimpleExoPlayer
    private lateinit var duetBottomPlayer: SimpleExoPlayer

    private lateinit var storyTextHelper: StoryTextHelper

    private val videoProgressObservable = Observable.interval(16, TimeUnit.MILLISECONDS)
    private var videoProgressDisposable: Disposable? = null

    private var textStoryProgress = 0
    private var isTextStoryPlaying = true
    private lateinit var storyTextView: TextView
    private var isPlayerReady = false
    private var prevVideoProgress: Long = 0

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_set_effect_duration, container, false)
        storyTextHelper = StoryTextHelper(requireContext())
        player = SimpleExoPlayer.Builder(requireContext()).build()
        duetTopPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        duetBottomPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        setupView(args.story)
        setupStoryText(args.storyText)
        setClickListeners()
        return layout.root
    }

    private fun setupView(story: ConstructStory) {
        when (story.source) {
            StorySource.FROM_TEXT_STORY -> setupTextStoryView(story)
            StorySource.FROM_GALLERY, StorySource.FROM_CAMERA -> setupVideoSegmentsView(story)
            StorySource.FROM_DUET -> setupDuetStoryView(story)
        }
    }

    private fun setupVideoSegmentsView(story: ConstructStory) {
        val videoSegments = story.videoSegments ?: return
        val duration = story.getCurrentDuration()
        setupVideoSegmentBitmaps(videoSegments, duration)
        setupVideoSegmentsPlayer(videoSegments)
    }

    private fun setupTextStoryView(story: ConstructStory) {
        val storyBackgroundResId = story.background?.backgroundResId ?: return
        val duration = story.duration.millis
        setupTextStoryBitmaps(storyBackgroundResId, duration)
        setupStoryBackground(storyBackgroundResId)
    }

    private fun setupDuetStoryView(story: ConstructStory) {
        val originalVideo = story.duetOriginalVideoUri ?: return
        val cameraVideoSegments = story.videoSegments ?: return
        val duration = story.getCurrentDuration()
        setupDuetStoryBitmaps(originalVideo, cameraVideoSegments, duration)
        setupDuetStoryPlayers(originalVideo, cameraVideoSegments, duration)
    }

    private fun setupVideoSegmentBitmaps(videoSegments: List<VideoSegment>, duration: Int) {
        val videoSegmentsManager = VideoSegmentsManager().apply { setVideoSegments(videoSegments) }
        layout.trimVideoView.setDuration(duration, 0, duration)
        layout.trimVideoView.onSizeChanged = { width, height ->
            val thumbHeight = height
            val thumbWidth = (height * 9f / 16f).toInt()
            lifecycleScope.launch {
                videoSegmentsManager.calculatingTotalVideoBitmaps = true

                val bitmaps = withContext(Dispatchers.Default) {
                    videoSegmentsManager.getTotalVideoBitmaps(width, thumbWidth, thumbHeight)
                }
                if (bitmaps.isNotEmpty()) {
                    layout.trimVideoView.setLoading(false)
                    layout.trimVideoView.setBitmaps(bitmaps)
                }
            }
        }
    }

    private fun setupTextStoryBitmaps(storyBackgroundResId: Int, duration: Int) {
        layout.trimVideoView.setDuration(duration, 0, duration)

        val bitmap = BitmapFactory.decodeResource(resources, storyBackgroundResId)
        layout.trimVideoView.onSizeChanged = { width, height ->
            val thumbHeight = height
            val thumbWidth = (height * 9f / 16f).toInt()
            lifecycleScope.launch {
                val bitmaps = withContext(Dispatchers.Default) {
                    MediaUtils.fetchBitmapsFromSingleBitmap(bitmap, width, thumbWidth, thumbHeight)
                }
                layout.trimVideoView.setBitmaps(bitmaps)
            }
        }
    }

    private fun setupDuetStoryBitmaps(originalVideo: Uri, cameraVideoSegments: List<VideoSegment>, duration: Int) {
        val cameraVideoSegmentsManager = VideoSegmentsManager().apply { setVideoSegments(cameraVideoSegments) }
        val originalVideoSegmentsManager = VideoSegmentsManager().apply { setVideoSegments(listOf(VideoSegment(originalVideo, duration, false))) }
        layout.trimVideoView.setDuration(duration, 0, duration)
        layout.trimVideoView.onSizeChanged = { width, height ->
            val thumbHeight = height
            val thumbWidth = (height * 9f / 16f).toInt()
            lifecycleScope.launch {
                cameraVideoSegmentsManager.calculatingTotalVideoBitmaps = true
                originalVideoSegmentsManager.calculatingTotalVideoBitmaps = true

                val cameraVideoBitmaps = withContext(Dispatchers.Default) {
                    cameraVideoSegmentsManager.getTotalVideoBitmaps(width, thumbWidth, thumbHeight)
                }
                val originalVideoBitmaps = withContext(Dispatchers.Default) {
                    originalVideoSegmentsManager.getTotalVideoBitmaps(width, thumbWidth, thumbHeight)
                }

                val duetBitmaps = LongSparseArray<Bitmap>()
                withContext(Dispatchers.Default) {
                    for (i in 0 until cameraVideoBitmaps.size()) {
                        val duetBitmap = StoryBuilderUtil.combineDuetBitmaps(
                                originalVideoBitmaps.get(i.toLong()),
                                cameraVideoBitmaps.get(i.toLong()))
                        duetBitmaps.put(i.toLong(), duetBitmap)
                    }
                }

                layout.trimVideoView.setBitmaps(duetBitmaps)
            }
        }
    }

    private fun setupVideoSegmentsPlayer(videoSegments: List<VideoSegment>) {
        val videoSourceList = Array<MediaSource?>(videoSegments.size) { null }
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        videoSegments.forEachIndexed { pos, videoSegment ->
            var videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSegment.uri)
            val clippingMediaSource = ClippingMediaSource(videoSource, videoSegment.startTime * 1000L, videoSegment.endTime * 1000L)
            videoSourceList[pos] = clippingMediaSource
        }
        val concatenatingSource = ConcatenatingMediaSource(*videoSourceList)
        player.prepare(LoopingMediaSource(concatenatingSource))
        player.playWhenReady = true
        subscribeVideoProgress()

        layout.playerView.player = player
        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false
        layout.playerView.visibility = View.VISIBLE
    }

    private fun setupDuetStoryPlayers(originalVideo: Uri, cameraVideoSegments: List<VideoSegment>, duration: Int) {
        val videoSourceList = Array<MediaSource?>(cameraVideoSegments.size) { null }
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        cameraVideoSegments.forEachIndexed { pos, videoSegment ->
            var videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSegment.uri)
            val clippingMediaSource = ClippingMediaSource(videoSource, videoSegment.startTime * 1000L, videoSegment.endTime * 1000L)
            videoSourceList[pos] = clippingMediaSource
        }

        val videoSource = ConcatenatingMediaSource(*videoSourceList)
        val originalVideoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(originalVideo)

        subscribeDuetStoryProgress()

        duetTopPlayer.prepare(originalVideoSource)
        duetBottomPlayer.prepare(LoopingMediaSource(videoSource))

        duetTopPlayer.volume = args.story.videoVolume
        duetBottomPlayer.volume = args.story.videoVolume

        layout.duetTopVideoPlayer.player = duetTopPlayer
        layout.duetBottomVideoPlayer.player = duetBottomPlayer
        layout.llDuetPlayerView.visibility = View.VISIBLE

        Handler().postDelayed({
            duetTopPlayer.playWhenReady = true
            duetBottomPlayer.playWhenReady = true
            isPlayerReady = true
        }, 500)

    }

    private fun setupStoryBackground(storyBackgroundResId: Int) {
        layout.ivStoryBackround.setImageResource(storyBackgroundResId)
        layout.ivStoryBackround.visibility = View.VISIBLE
        subscribeTextStoryProgress()
    }

    private fun setupStoryText(storyText: StoryText) {
        storyTextView = storyTextHelper.createStoryTextView(storyText).apply { enableMoving = false }

        layout.actionBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        layout.trimVideoView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        lp.leftMargin = storyText.posX.toInt()
        lp.topMargin = storyText.posY.toInt() - layout.actionBar.measuredHeight - layout.trimVideoView.measuredHeight
        layout.flStoryTextContainer.addView(storyTextView, lp)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListeners() {
        layout.btnPlay.setOnClickListener { togglePlaying() }
        layout.trimVideoView.onTimeChangedListener = { handleTimeThumbPosChanged(it) }
        layout.trimVideoView.getTimeLineThumb().setOnTouchListener { _, event -> handleTimeLineThumbTouchEvent(event) }
        layout.btnDone.setOnClickListener { handleDoneButtonClick() }
        layout.btnCancel.setOnClickListener { popBackStackWithoutResult() }
    }

    private fun subscribeVideoProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = videoProgressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val progress = player.currentPosition.toInt()
                    layout.trimVideoView.setThumbTime(progress)
                    checkStoryTextVisibility(progress)
                    updateStartEndTimeThumbs()
                }
    }

    private fun subscribeTextStoryProgress() {
        val duration = args.story.duration.millis
        videoProgressDisposable?.dispose()
        videoProgressDisposable = videoProgressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    textStoryProgress += 16
                    textStoryProgress %= duration
                    layout.trimVideoView.setThumbTime(textStoryProgress)
                    checkStoryTextVisibility(textStoryProgress)
                    updateStartEndTimeThumbs()
                }
    }

    private fun subscribeDuetStoryProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = videoProgressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val videoSegments = args.story.videoSegments ?: return@subscribe
                    val curWindow = duetBottomPlayer.currentWindowIndex
                    val curProgress = duetBottomPlayer.currentPosition

                    val totalProgress = MediaUtils.getTotalProgress(videoSegments, curWindow, curProgress)
                    layout.trimVideoView.setThumbTime(totalProgress.toInt())
                    checkStoryTextVisibility(totalProgress.toInt())

                    if (abs(totalProgress - prevVideoProgress) > args.story.getCurrentDuration() / 2) {
                        duetTopPlayer.seekTo(0)
                    }
                    prevVideoProgress = totalProgress
                    updateStartEndTimeThumbs()
                }
    }

    private fun unsubscribeVideoProgress() {
        videoProgressDisposable?.dispose()
    }

    private fun togglePlaying() {
        when (args.story.source) {
            StorySource.FROM_GALLERY,
            StorySource.FROM_CAMERA -> toggleVideoStoryPlayer()
            StorySource.FROM_TEXT_STORY -> toggleTextStoryPlayer()
            StorySource.FROM_DUET -> toggleDuetStoryPlayer()
        }
    }

    private fun toggleVideoStoryPlayer() {
        player.playWhenReady = !player.playWhenReady
        if (player.isPlaying) {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_pause)
            subscribeVideoProgress()
        } else {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_play)
            unsubscribeVideoProgress()
        }
    }

    private fun toggleTextStoryPlayer() {
        isTextStoryPlaying = !isTextStoryPlaying
        if (isTextStoryPlaying) {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_pause)
            subscribeTextStoryProgress()
        } else {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_play)
            unsubscribeVideoProgress()
        }
    }

    private fun toggleDuetStoryPlayer() {
        duetTopPlayer.playWhenReady = !duetTopPlayer.playWhenReady
        duetBottomPlayer.playWhenReady = !duetBottomPlayer.playWhenReady
        if (duetTopPlayer.isPlaying) {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_pause)
            subscribeDuetStoryProgress()
        } else {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_play)
            unsubscribeVideoProgress()
        }

    }

    private fun handleTimeLineThumbTouchEvent(event: MotionEvent): Boolean {
        when (args.story.source) {
            StorySource.FROM_GALLERY,
            StorySource.FROM_CAMERA -> handleTimeLineThumbTouchForVideoStory(event)
            StorySource.FROM_TEXT_STORY -> handleTimeLineThumbTouchForTextStory(event)
            StorySource.FROM_DUET -> handleTimeLineThumbTouchForDuetStory(event)
        }
        return false
    }

    private fun handleTimeLineThumbTouchForVideoStory(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            player.playWhenReady = false
        } else if (event.action == MotionEvent.ACTION_UP) {
            player.playWhenReady = true
        }
    }

    private fun handleTimeLineThumbTouchForTextStory(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            unsubscribeVideoProgress()
        } else if (event.action == MotionEvent.ACTION_UP) {
            subscribeTextStoryProgress()
        }
    }

    private fun handleTimeLineThumbTouchForDuetStory(event: MotionEvent) {
        if (!isPlayerReady) {
            return
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            duetTopPlayer.playWhenReady = false
            duetBottomPlayer.playWhenReady = false
        } else if (event.action == MotionEvent.ACTION_UP) {
            duetTopPlayer.playWhenReady = true
            duetBottomPlayer.playWhenReady = true
        }
    }

    private fun handleTimeThumbPosChanged(timeMills: Int) {
        when (args.story.source) {
            StorySource.FROM_GALLERY,
            StorySource.FROM_CAMERA -> {
                textStoryProgress = timeMills
                player.seekTo(timeMills.toLong())
            }
            StorySource.FROM_TEXT_STORY -> {
                textStoryProgress = timeMills
            }
            StorySource.FROM_DUET -> {
                duetTopPlayer.seekTo(timeMills.toLong())
                duetBottomPlayer.seekTo(timeMills.toLong())
            }
        }

    }

    private fun checkStoryTextVisibility(curTime: Int) {
        val minTime = layout.trimVideoView.getLeftTrim()
        val maxTime = layout.trimVideoView.getRightTrim()

        if (curTime in minTime..maxTime || maxTime < 0) {
            storyTextView.visibility = View.VISIBLE
        } else {
            storyTextView.visibility = View.GONE
        }
    }

    private fun updateStartEndTimeThumbs() {
        val minTime = layout.trimVideoView.getLeftTrim()
        val maxTime = layout.trimVideoView.getRightTrim()

        layout.trimAreaLeftTime.text = String.format("%.1f", minTime.toDouble() / 1000.0)
        layout.trimAreaRightTime.text = String.format("%.1f", maxTime.toDouble() / 1000.0)

        layout.trimAreaLeftTime.setPosition(layout.trimVideoView.getLeftTrimPos())
        layout.trimAreaRightTime.setPosition(layout.trimVideoView.getRightTrimPos())
    }

    private fun handleDoneButtonClick() {
        val minTime = layout.trimVideoView.getLeftTrim()
        val maxTime = layout.trimVideoView.getRightTrim()
        val storyText = args.storyText.apply {
            startTime = minTime
            endTime = maxTime
        }
        popBackStackWithResult(ResultData(storyText))
    }

    private fun popBackStackWithoutResult() {
        findNavController().popBackStack()
    }

    private fun popBackStackWithResult(result: ResultData<StoryText>) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<StoryText>>(SET_EFFECT_DURATION_RESULT)
                ?.value = result
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (args.story.videoSegments != null) {
            player.release()
        }
    }

    companion object {
        const val SET_EFFECT_DURATION_RESULT = "set_effect_duration_result"
    }
}