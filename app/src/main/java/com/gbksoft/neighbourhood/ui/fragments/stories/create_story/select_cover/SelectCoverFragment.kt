package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.select_cover

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentSelectCoverBinding
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.StorySource
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.mvvm.result.ResultInt
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilder
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilderUtil
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story.VideoSegmentsManager
import com.gbksoft.neighbourhood.ui.widgets.stories.timeline.SelectCoverView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.ceil
import kotlin.math.roundToInt

class SelectCoverFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<SelectCoverFragmentArgs>()
    private lateinit var layout: FragmentSelectCoverBinding
    private lateinit var player: SimpleExoPlayer
    private lateinit var duetTopPlayer: SimpleExoPlayer
    private lateinit var duetBottomPlayer: SimpleExoPlayer
    private var seekToTime = 0
    private val handler = Handler()
    private val seekRunnable = Runnable {
        seekToTime(seekToTime)
    }

    private var timelineWidth = 0
    private var thumbHeight: Int = 0
    private var thumbWidth: Int = 0

    private val videoSegmentsManager = VideoSegmentsManager()

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_select_cover, container, false)

        initPlayers()
        setupView(args.story)
        setClickListeners()
        return layout.root
    }


    private fun initPlayers() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        duetTopPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        duetBottomPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        layout.playerView.player = player
        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false

        layout.duetTopVideoPlayer.player = duetTopPlayer
        layout.duetTopVideoPlayer.controllerAutoShow = false
        layout.duetTopVideoPlayer.useController = false

        layout.duetBottomVideoPlayer.player = duetBottomPlayer
        layout.duetBottomVideoPlayer.controllerAutoShow = false
        layout.duetBottomVideoPlayer.useController = false
    }

    private fun setupView(constructStory: ConstructStory) {
        layout.selectCoverView.onSizeChanged = { width, height ->
            Timber.tag("TimeLineView").d("width: $width, height: $height")
            timelineWidth = width
            thumbHeight = height
            thumbWidth = (height * 9f / 16f).toInt()
            updateTimeline(constructStory)
        }
        when (constructStory.source) {
            StorySource.FROM_CAMERA, StorySource.FROM_GALLERY -> setupVideoSegmentsView(constructStory)
            StorySource.FROM_DUET -> setupDuetView(constructStory)
            StorySource.FROM_TEXT_STORY -> setupTextStoryView(constructStory)
        }
        setTime(args.currentCoverTimestamp.toInt(), 0)
    }

    private fun updateTimeline(constructStory: ConstructStory) {
        layout.selectCoverView.setLoading(true)
        when (constructStory.source) {
            StorySource.FROM_CAMERA, StorySource.FROM_GALLERY -> getTotalVideoBitmaps()
            StorySource.FROM_DUET -> getDuetStoryBitmaps()
            StorySource.FROM_TEXT_STORY -> getTextStoryBitmaps()
        }
    }

    private fun setupVideoSegmentsView(constructStory: ConstructStory) {
        val videoSegments = constructStory.videoSegments ?: return
        setupVideoSegmentsPlayer(videoSegments)
        videoSegmentsManager.setVideoSegments(videoSegments)
    }

    private fun setupDuetView(constructStory: ConstructStory) {
        val originalVideo = constructStory.duetOriginalVideoUri ?: return
        val cameraVideoSegments = constructStory.videoSegments ?: return
        setupDuetPlayers(originalVideo, cameraVideoSegments)
    }

    private fun setupTextStoryView(constructStory: ConstructStory) {
        val storyBackgroundResId = constructStory.background?.backgroundResId ?: return
        setupTextStoryBackground(storyBackgroundResId)
    }

    fun setupVideoSegmentsPlayer(videoSegments: List<VideoSegment>) {
        val videoSourceList = Array<MediaSource?>(videoSegments.size) { null }
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        videoSegments.forEachIndexed { pos, videoSegment ->
            var videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSegment.uri)
            val clippingMediaSource = ClippingMediaSource(videoSource, videoSegment.startTime * 1000L, videoSegment.endTime * 1000L)
            videoSourceList[pos] = clippingMediaSource
        }
        val videoSource = ConcatenatingMediaSource(*videoSourceList)
        player.prepare(LoopingMediaSource(videoSource))
        layout.playerView.visibility = View.VISIBLE
    }

    fun setupDuetPlayers(originalVideo: Uri, videoSegments: List<VideoSegment>) {
        layout.playerView.visibility = View.GONE
        layout.llDuetPlayerView.visibility = View.VISIBLE
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
        duetBottomPlayer.prepare(videoSource)
        layout.llDuetPlayerView.visibility = View.VISIBLE
    }

    fun setupTextStoryBackground(backgroundResId: Int, time: Int = 0) {
        val storyBuilder = StoryBuilder(requireContext(), args.story)
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                val tmp = BitmapFactory.decodeResource(context?.resources, backgroundResId)
                storyBuilder.getBitmapFromTextStory(tmp, time)
            }
            layout.ivStoryBackround.setImageBitmap(bitmap)
            layout.ivStoryBackround.visibility = View.VISIBLE
        }
    }

    private fun getTotalVideoBitmaps() {
        lifecycleScope.launch {
            val currentCoverTimestamp = args.currentCoverTimestamp.toInt()
            val duration = args.story.getCurrentDuration()
            layout.selectCoverView.setDuration(duration, currentCoverTimestamp)

            videoSegmentsManager.calculatingTotalVideoBitmaps = true
            val bitmaps = withContext(Dispatchers.Default) {
                videoSegmentsManager.getTotalVideoBitmaps(timelineWidth, thumbWidth, thumbHeight)
            }
            layout.selectCoverView.setLoading(false)
            layout.selectCoverView.setBitmaps(bitmaps)
            Timber.tag("Timeline").d("Total video bitmaps finish loading")
        }
    }

    private fun getDuetStoryBitmaps() {
        val originalVideo = args.story.duetOriginalVideoUri ?: return
        val cameraVideoSegments = args.story.videoSegments ?: return
        val duration = args.story.getCurrentDuration()
        val currentCoverTimestamp = args.currentCoverTimestamp.toInt()

        val cameraVideoSegmentsManager = VideoSegmentsManager().apply { setVideoSegments(cameraVideoSegments) }
        val originalVideoSegmentsManager = VideoSegmentsManager().apply { setVideoSegments(listOf(VideoSegment(originalVideo, duration, false))) }
        layout.selectCoverView.setDuration(duration, currentCoverTimestamp)
        lifecycleScope.launch {
            cameraVideoSegmentsManager.calculatingTotalVideoBitmaps = true
            originalVideoSegmentsManager.calculatingTotalVideoBitmaps = true

            val cameraVideoBitmaps = withContext(Dispatchers.Default) {
                cameraVideoSegmentsManager.getTotalVideoBitmaps(timelineWidth, thumbWidth, thumbHeight)
            }
            val originalVideoBitmaps = withContext(Dispatchers.Default) {
                originalVideoSegmentsManager.getTotalVideoBitmaps(timelineWidth, thumbWidth, thumbHeight)
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

            layout.selectCoverView.setLoading(false)
            layout.selectCoverView.setBitmaps(duetBitmaps)
        }
    }

    private fun getTextStoryBitmaps() {
        lifecycleScope.launch {
            val currentCoverTimestamp = args.currentCoverTimestamp.toInt()
            val duration = args.story.duration.millis
            layout.selectCoverView.setDuration(duration, currentCoverTimestamp)

            val storyBuilder = StoryBuilder(requireContext(), args.story)
            val numThumbs = ceil(timelineWidth.toFloat() / thumbWidth.toDouble()).toInt()
            val interval = duration / numThumbs
            val bitmap = BitmapFactory.decodeResource(context?.resources, args.story.background?.backgroundResId!!)
            val bitmaps = withContext(Dispatchers.Default) {
                val thumbnailList = LongSparseArray<Bitmap>()
                for (i in 0 until numThumbs) {
                    val frameTime = i * interval
                    var frameBitmap = storyBuilder.getBitmapFromTextStory(bitmap, frameTime)
                    try {
                        frameBitmap = Bitmap.createScaledBitmap(frameBitmap, thumbWidth, thumbHeight, false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    thumbnailList.put(i.toLong(), frameBitmap)
                }
                thumbnailList
            }
            layout.selectCoverView.setLoading(false)
            layout.selectCoverView.setBitmaps(bitmaps)
        }
    }

    private fun seekToTime(time: Int) {
        Timber.tag("SelectCoverFragment").d("Seek to time: $time")
        when (args.story.source) {
            StorySource.FROM_GALLERY,
            StorySource.FROM_CAMERA -> seekVideoSegmentsPlayer(time)
            StorySource.FROM_TEXT_STORY -> seekTextStory(time)
            StorySource.FROM_DUET -> seekDuetStory(time)
        }
    }

    private fun seekVideoSegmentsPlayer(time: Int) {
        player.seekTo(time.toLong())
    }

    private fun seekTextStory(time: Int) {
        setupTextStoryBackground(args.story.background?.backgroundResId!!, time)
    }

    private fun seekDuetStory(time: Int) {
        duetTopPlayer.seekTo(time.toLong())
        duetBottomPlayer.seekTo(time.toLong())
    }

    private fun setClickListeners() {
        layout.selectCoverView.onTimeChangedListener = { setTime(it) }
        layout.btnCancel.setOnClickListener { popBackStackWithoutResult() }
        layout.btnDone.setOnClickListener { onDoneClicked() }
    }

    private fun setTime(time: Int, delay: Long = 100) {
        seekToTime = roundUpTime(time)
        handler.removeCallbacks(seekRunnable)
        handler.postDelayed(seekRunnable, delay)
    }

    private fun popBackStackWithoutResult() {
        findNavController().popBackStack()
    }

    private fun onDoneClicked() {
        val selectedFrameTime = layout.selectCoverView.getSelectedFrameTime()
        if (selectedFrameTime != SelectCoverView.NOTHING_SELECTED_TIME) {
            val time = roundUpTime(selectedFrameTime)
            popBackStackWithResult(ResultInt(time))
        }
    }

    private fun roundUpTime(time: Int): Int {
        val res = (time.toFloat() / 1000.toFloat()).roundToInt()
        return if (res > 0) res * 1000 else 1000
    }

    private fun popBackStackWithResult(resultData: ResultInt) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultInt>(RESULT_COVER_TIMESTAMP)
                ?.value = resultData
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(seekRunnable)
        player.release()
        super.onDestroyView()
    }

    companion object {
        const val RESULT_COVER_TIMESTAMP = "result_cover_timestamp"
    }
}