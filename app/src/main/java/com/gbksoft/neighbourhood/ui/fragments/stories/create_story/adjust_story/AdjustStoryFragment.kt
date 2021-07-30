package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.util.isNotEmpty
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentAdjustStoryBinding
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.StorySource
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.ToastUtils
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
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AdjustStoryFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<AdjustStoryFragmentArgs>()
    private lateinit var layout: FragmentAdjustStoryBinding
    private lateinit var player: SimpleExoPlayer
    private lateinit var videoSegmentsAdapter: VideoSegmentsAdapter
    private lateinit var videoSegmentsManager: VideoSegmentsManager
    private lateinit var constructStory: ConstructStory

    private var isInEditingSegmentMode = false
    private var curVideoSegmentPos = -1

    private var curProgressTime: Int = 0

    private val videoProgressObservable = Observable
            .interval(16, TimeUnit.MILLISECONDS)
    private var videoProgressDisposable: Disposable? = null

    private var timelineWidth = 0
    private var thumbHeight: Int = 0
    private var thumbWidth: Int = 0

    private val MAX_VIDEO_SEGMENTS_LENGTH = 30
    private var videoSegmentsLength = 0
    private val deletedVideoSegments = mutableListOf<VideoSegment>()

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_adjust_story, container, false)

        preparePlayer()
        constructStory = args.story
        constructStory.videoSegments?.let {
            videoSegmentsManager = VideoSegmentsManager().apply { setVideoSegments(it) }
            setupView()
            prepareVideo(it)
            setupVideoSegments(it)
        }
        setClickListeners()
        return layout.root
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
    }

    private fun preparePlayer() {
        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false
        player = SimpleExoPlayer.Builder(requireContext())
                .build()
        layout.playerView.player = player
    }

    private fun prepareVideoSegmentPlayer(videoSegment: VideoSegment) {
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSegment.uri)
        player.prepare(videoSource)
        player.playWhenReady = true
        player.addListener(object : Player.EventListener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    subscribeVideoProgress()
                } else {
                    videoProgressDisposable?.dispose()
                }
            }
        })
    }

    private fun setupView() {
        layout.trimVideoView.onSizeChanged = { width, height ->
            Timber.tag("TimeLineView").d("width: $width, height: $height")
            timelineWidth = width
            thumbHeight = height
            thumbWidth = (height * 9f / 16f).toInt()
            updateTimeline()
        }
    }

    private fun updateTimeline() {
        layout.trimVideoView.setLoading(true)
        if (curVideoSegmentPos == -1) {
            getTotalVideoBitmaps()
        } else {
            getVideoSegmentBitmaps(curVideoSegmentPos)
        }
    }

    private fun getTotalVideoBitmaps() {
        lifecycleScope.launch {
            Timber.tag("Timeline").d("Total video bitmaps start loading, width = $timelineWidth")
            val duration = videoSegmentsManager.getTotalVideoDuration()
            layout.trimVideoView.setDuration(duration)
            videoSegmentsManager.calculatingTotalVideoBitmaps = true

            val bitmaps = withContext(Dispatchers.Default) {
                videoSegmentsManager.getTotalVideoBitmaps(timelineWidth, thumbWidth, thumbHeight)
            }
            if (curVideoSegmentPos == -1 && bitmaps.isNotEmpty()) {
                layout.trimVideoView.setLoading(false)
                layout.trimVideoView.setBitmaps(bitmaps)
                Timber.tag("Timeline").d("Total video bitmaps finish loading")
            }
        }
    }

    private fun getVideoSegmentBitmaps(videoSegmentPos: Int) {
        lifecycleScope.launch {
            Timber.tag("Timeline").d("Video segment bitmaps start loading, width = $timelineWidth")
            val videoSegment = videoSegmentsManager.getVideoSegments()[videoSegmentPos]
            layout.trimVideoView.setDuration(videoSegment.duration, videoSegment.startTime, videoSegment.endTime)
            videoSegmentsManager.calculatingTotalVideoBitmaps = false

            val bitmaps = withContext(Dispatchers.Default) {
                videoSegmentsManager.getVideoSegmentBitmaps(videoSegmentPos, timelineWidth, thumbWidth, thumbHeight)
            }
            if (curVideoSegmentPos == videoSegmentPos) {
                layout.trimVideoView.setLoading(false)
                layout.trimVideoView.setBitmaps(bitmaps)
                Timber.tag("Timeline").d("Video segment bitmaps finish loading")
            }
        }
    }

    private fun prepareVideo(videoSegments: List<VideoSegment>) {
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
        player.addListener(object : Player.EventListener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    subscribeVideoProgress()
                } else {
                    videoProgressDisposable?.dispose()
                }
            }
        })
    }

    private fun setupVideoSegments(videoSegments: List<VideoSegment>) {
        videoSegmentsAdapter = VideoSegmentsAdapter(constructStory.source == StorySource.FROM_GALLERY)
        videoSegmentsAdapter.setData(videoSegments)

        videoSegmentsLength = videoSegments.size

        layout.rvVideoSegments.adapter = videoSegmentsAdapter
        layout.rvVideoSegments.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val spacing = resources.getDimensionPixelSize(R.dimen.video_segment_image_spacing)
        val divider = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).absoluteAdapterPosition
                if (itemPosition > 0) {
                    outRect.left = spacing
                }
            }
        }
        layout.rvVideoSegments.addItemDecoration(divider)

        val videoSegmentsItemTouchHelper = VideoSegmentsItemTouchHelper(videoSegmentsAdapter)
        videoSegmentsItemTouchHelper.itemMovedCallback = { dragFrom: Int, dragTo: Int ->
            videoSegmentsManager.dragVideoSegment(dragFrom, dragTo)
            videoSegmentsAdapter.setData(videoSegmentsManager.getVideoSegments())
            prepareVideo(videoSegmentsManager.getVideoSegments())
            updateTimeline()
        }
        val itemTouchHelper = ItemTouchHelper(videoSegmentsItemTouchHelper)
        itemTouchHelper.attachToRecyclerView(layout.rvVideoSegments)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListeners() {
        layout.trimVideoView.onTimeChangedListener = { setTime(it) }
        layout.trimVideoView.getTimeLineThumb().setOnTouchListener { _, event -> handleTimeLineThumbTouchEvent(event) }
        layout.btnCloseEditVideoSegment.setOnClickListener { showCancelChangesDialog { closeAdjustingVideoSegment() } }
        layout.btnDoneEditVideoSegment.setOnClickListener { confirmAdjustingVideoSegment() }
        layout.btnDeleteSegment.setOnClickListener { showDeleteVideoSegmentDialog() }
        layout.btnDone.setOnClickListener { onDoneButtonClicked() }
        layout.btnCancel.setOnClickListener { onCancelButtonClicked() }
        layout.btnPlay.setOnClickListener { togglePlaying() }
        videoSegmentsAdapter.onAddVideoSegmentButtonClickListener = ::addVideoSegment
        videoSegmentsAdapter.onItemClickListener = ::openAdjustingVideoSegment

        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false
        layout.playerView.videoSurfaceView?.setOnClickListener { togglePlaying() }
    }

    private fun togglePlaying() {
        player.playWhenReady = !player.playWhenReady
        if (player.isPlaying) {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_pause)
        } else {
            layout.btnPlay.setImageResource(R.drawable.ic_adjust_story_play)
        }
    }

    private fun onDoneButtonClicked() {
        val minTime = layout.trimVideoView.getLeftTrim()
        val maxTime = layout.trimVideoView.getRightTrim()
        Log.d("adjust_story", constructStory.videoSegments.toString())
        constructStory.videoSegments = videoSegmentsManager.getResultVideoSegments(minTime, maxTime)
        Log.d("adjust_story", constructStory.videoSegments.toString())
        handleResult(constructStory)
    }

    private fun onCancelButtonClicked() {
        handleResult(args.story)
    }

    private fun handleResult(constructStory: ConstructStory) {
        if (args.shouldReturnResult) {
            popBackStackWithResult(ResultData(constructStory))
        } else {
            navigateToPreview(constructStory)
        }
    }

    private fun setTime(time: Int) {
        curProgressTime = time
        if (isInEditingSegmentMode) {
            updateVideoSegmentPlayerTime()
        } else {
            updatePlayerTime()
        }
    }

    private fun subscribeVideoProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = videoProgressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    curProgressTime += 16
                    adjustTimeToBounds()
                    layout.trimVideoView.setThumbTime(curProgressTime)

                    val window = videoSegmentsManager?.getVideoSegmentPositionByTotalTime(curProgressTime)
                    if (window != null) {
                        videoSegmentsAdapter.currentPlayingSegmentPos = window
                    }
                }
    }

    private fun adjustTimeToBounds() {
        val minTime = layout.trimVideoView.getLeftTrim().coerceAtLeast(0)
        val maxTime = layout.trimVideoView.getRightTrim()

        if (maxTime < 0) {
            return
        }

        layout.trimAreaLeftTime.text = String.format("%.1f", minTime.toDouble() / 1000.0)
        layout.trimAreaRightTime.text = String.format("%.1f", maxTime.toDouble() / 1000.0)

        layout.trimAreaLeftTime.setPosition(layout.trimVideoView.getLeftTrimPos())
        layout.trimAreaRightTime.setPosition(layout.trimVideoView.getRightTrimPos())

        if (curProgressTime < minTime || curProgressTime > maxTime) {
            curProgressTime = minTime

            if (isInEditingSegmentMode) {
                updateVideoSegmentPlayerTime()
            } else {
                updatePlayerTime()
            }
        }
    }

    private fun updatePlayerTime() {
        val window = videoSegmentsManager.getVideoSegmentPositionByTotalTime(curProgressTime)
        val time = videoSegmentsManager.getTimeInVideoSegmentByTotalTime(window, curProgressTime)
        player.seekTo(window, time.toLong())
        videoSegmentsAdapter.currentPlayingSegmentPos = window
    }

    private fun updateVideoSegmentPlayerTime() {
        player.seekTo(curProgressTime.toLong())
    }

    private fun openAdjustingVideoSegment(videoSegment: VideoSegment, videoSegmentPosition: Int) {
        curProgressTime = 0
        prepareVideoSegmentPlayer(videoSegment)
        layout.rvVideoSegments.visibility = View.GONE
        layout.llTotalVideoActionButtons.visibility = View.GONE
        layout.llEditVideoSegment.visibility = View.VISIBLE
        layout.flVideoSegmentActionButtons.visibility = View.VISIBLE
        isInEditingSegmentMode = true
        curVideoSegmentPos = videoSegmentPosition
        updateTimeline()
    }

    private fun closeAdjustingVideoSegment() {
        curProgressTime = 0
        prepareVideo(videoSegmentsManager.getVideoSegments())
        layout.llEditVideoSegment.visibility = View.GONE
        layout.flVideoSegmentActionButtons.visibility = View.GONE
        layout.rvVideoSegments.visibility = View.VISIBLE
        layout.llTotalVideoActionButtons.visibility = View.VISIBLE
        isInEditingSegmentMode = false
        curVideoSegmentPos = -1
        videoSegmentsAdapter.setData(videoSegmentsManager.getVideoSegments())
        updateTimeline()
    }

    private fun confirmAdjustingVideoSegment() {
        val minTime = layout.trimVideoView.getLeftTrim()
        val maxTime = layout.trimVideoView.getRightTrim()
        videoSegmentsManager.setVideoSegmentBounds(curVideoSegmentPos, minTime, maxTime)
        closeAdjustingVideoSegment()
    }

    private fun handleTimeLineThumbTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            player.playWhenReady = false
        } else if (event.action == MotionEvent.ACTION_UP) {
            player.playWhenReady = true
        }
        return false
    }

    private fun addVideoSegment() {
        if (videoSegmentsLength < MAX_VIDEO_SEGMENTS_LENGTH) {
            val deletedMediaPaths = mutableListOf<String>()
            deletedVideoSegments.forEach {
                if (it.originalFilePath != null) {
                    deletedMediaPaths.add(it.originalFilePath)
                }
            }
            Timber.tag("KEK").d("deleted media ${deletedMediaPaths.joinToString()}")
            popBackStackWithRemovedMediaResult(ResultData(AdjustStoryRemoveMediaResult(deletedMediaPaths)))
        } else {
            ToastUtils.showToastMessage(getString(R.string.you_have_marked_the_maximum_number))
        }

    }

    private fun deleteCurrentVideoSegment() {
        if (curVideoSegmentPos == -1) return
        if (videoSegmentsManager.getVideoSegments().size == 1) {
            findNavController().popBackStack(R.id.createStory, false)
            return
        }
        deletedVideoSegments.add(videoSegmentsManager.getVideoSegments()[curVideoSegmentPos])
        videoSegmentsManager.deleteVideoSegment(curVideoSegmentPos)
        closeAdjustingVideoSegment()
        videoSegmentsAdapter.setData(videoSegmentsManager.getVideoSegments())

    }

    private fun showDeleteVideoSegmentDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.adjust_story_delete_video_segment_dialog_cancel, null)
                .setPositiveButton(R.string.adjust_story_delete_video_segment_dialog_delete) { deleteCurrentVideoSegment() }
                .setMessage(R.string.adjust_story_delete_video_segment_dialog_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun showCancelChangesDialog(positiveButtinCallback: () -> Unit) {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.adjust_story_cancel_changes_dialog_cancel, null)
                .setPositiveButton(R.string.adjust_story_cancel_changes_dialog_ok) { positiveButtinCallback.invoke() }
                .setMessage(R.string.adjust_story_cancel_changes_dialog_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun navigateToPreview(constructStory: ConstructStory) {
        val direction = AdjustStoryFragmentDirections.toPreviewStory(constructStory)
        findNavController().navigate(direction)
    }

    private fun popBackStackWithResult(result: ResultData<ConstructStory>) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<ConstructStory>>(ADJUST_STORY_RESULT)
                ?.value = result
        findNavController().popBackStack()
    }

    private fun popBackStackWithRemovedMediaResult(result: ResultData<AdjustStoryRemoveMediaResult>) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<AdjustStoryRemoveMediaResult>>(ADJUST_STORY_REMOVED_MEDIA_RESULT)
                ?.value = result
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        videoProgressDisposable?.dispose()
        player.release()
        super.onDestroyView()
    }

    companion object {
        const val ADJUST_STORY_RESULT = "adjust_story_result"
        const val ADJUST_STORY_REMOVED_MEDIA_RESULT = "adjust_story_removed_media_result"
    }
}