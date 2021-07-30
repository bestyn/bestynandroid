package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.duet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arthenica.mobileffmpeg.Config
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentCreateDuetStoryBinding
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record.CameraHelper
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record.VideoRecordingListener
import com.gbksoft.neighbourhood.ui.widgets.stories.progress_bar.RecordProgressView
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.permission.DexterMultiplePermissionListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.karumi.dexter.Dexter
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Flash
import timber.log.Timber
import java.io.File


private const val PROGRESS_STEP = 16L

class CreateDuetStoryFragment : SystemBarsColorizeFragment(), VideoRecordingListener {

    private val args by navArgs<CreateDuetStoryFragmentArgs>()
    private lateinit var layout: FragmentCreateDuetStoryBinding
    private lateinit var player: SimpleExoPlayer

    private var cameraHelper: CameraHelper = CameraHelper().apply {
        onCameraOptionsListener = ::onCameraOptions
    }

    private val handler = Handler()
    private var prevProgressTime = 0L
    private var curProgressHandler: Handler? = null
    private var currentProgressRunnable: Runnable? = null
    private val stopRecordingRunnable = Runnable { stopRecord() }

    private val videoSegmentList = mutableListOf<VideoSegment>()
    private var pauseMarkers: List<RecordProgressView.RecordPause>? = null
    private var recordProgress: Int? = null
    private val metadataRetriever by lazy { MediaMetadataRetriever() }
    private var isRecordingEnd = true

    private var videoDuration: Int? = null

    override fun getStatusBarColor(): Int = R.color.stories_screen_background
    override fun getNavigationBarColor(): Int = R.color.stories_screen_background
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    private var lastRecordStartTime = 0L

    private val runnable = object : Runnable {
        override fun run() {
            val curTime = System.currentTimeMillis()
            Timber.tag("DUET").d("is taking video: ${layout.cameraView.isTakingVideo})")
            layout.recordProgressView.currentProgress += (curTime - prevProgressTime).toInt()
            Timber.tag("DUET").d("current progress: ${layout.recordProgressView.currentProgress}")
            prevProgressTime = curTime
            if (layout.recordProgressView.currentProgress > getTotalVideoDuration()) {
                stopRecord()
            } else {
                handler.postDelayed(this, PROGRESS_STEP)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hideNavigateBar()
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_create_duet_story, container, false)
        setupVideo()
        checkCameraAndMicPermissions()
        setClickListeners()
        setupProgressBar()
        checkSegmentList()
        showHeadphonesMessage()
        return layout.root
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.setOnApplyWindowInsetsListener { v, insets ->
            layout.progressBarPositionHelper.updatePadding(top = insets.systemWindowInsetTop)
            layout.recordButtonPositionHelper.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    private fun setupVideo() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(args.video)
        player.prepare(mediaSource)

        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false
        layout.playerView.player = player
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1){
            layout.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    private fun setupProgressBar() {
        currentProgressRunnable?.let { curProgressHandler?.removeCallbacks(it) }
        layout.recordProgressView.setPauseMarkers(pauseMarkers)
        layout.recordProgressView.currentProgress = recordProgress ?: 0
        layout.recordProgressView.totalProgress = getTotalVideoDuration()
    }

    private fun setClickListeners() {
        layout.ivBack.setOnClickListener { findNavController().popBackStack() }
        layout.ivSwitchCamera.setOnClickListener { cameraHelper.toggleFacing() }
        layout.btnDeleteSegment.setOnClickListener { showConfirmDeleteSegmentDialog() }
        layout.recordButton.setOnClickListener { onRecordButtonClick() }
        layout.btnDone.setOnClickListener { onDoneClick() }
        layout.ivCameraTorch.setOnClickListener { cameraHelper.switchTorch() }
        layout.ivMicrophone.setOnClickListener { onMicrophoneButtonClick() }
        cameraHelper.videoRecordingListener = this
    }

    private fun checkCameraAndMicPermissions() {
        val permissionListener = DexterMultiplePermissionListener()
        permissionListener.onPermissionsChecked = { report ->
            if (report.areAllPermissionsGranted()) {
                layout.cameraView.setLifecycleOwner(viewLifecycleOwner)
                cameraHelper.init(requireContext(), viewLifecycleOwner, layout.cameraView)
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

    private fun onCameraOptions(cameraOptions: CameraOptions) {
        layout.ivCameraTorch.visibility = if (cameraOptions.supportedFlash.contains(Flash.TORCH)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        layout.ivSwitchCamera.visibility = if (cameraOptions.supportedFacing.size > 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun onRecordButtonClick() {
        val cameraPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val micPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionGranted || !micPermissionGranted) {
            checkCameraAndMicPermissions()
            return
        }

        if (layout.recordButton.isRecordingState) {
            stopRecord()
        } else {
            startRecord()
        }
    }

    private fun onMicrophoneButtonClick() {
        if (layout.cameraView.audio == Audio.ON) {
            cameraHelper.disableMicrophone()
            layout.ivMicrophone.setImageResource(R.drawable.ic_microphone_disabled)
        } else {
            cameraHelper.enableMicrophone()
            layout.ivMicrophone.setImageResource(R.drawable.ic_microphone_enabled)
            showHeadphonesMessage()
        }
    }

    private fun startRecord() {
        if (not { isRecordingEnd }) return

        if (layout.recordProgressView.currentProgress >= getTotalVideoDuration()) {
            return
        }
        layout.btnDeleteSegment.visibility = View.GONE
        layout.btnDone.visibility = View.GONE
        layout.options.visibility = View.GONE
        layout.ivBack.visibility = View.GONE
        layout.recordButton.setState(true)
        cameraHelper.startCaptureSnapshot()
    }

    private fun stopRecord() {
        val lastSegmentDuration = if (layout.recordProgressView.pauseMarkers().isNotEmpty()) {
            layout.recordProgressView.currentProgress - layout.recordProgressView.pauseMarkers().last().progressValue
        } else {
            layout.recordProgressView.currentProgress
        }
        if (lastSegmentDuration < 1000) {
            handler.removeCallbacks(stopRecordingRunnable)
            handler.postDelayed(stopRecordingRunnable, (1050 - lastSegmentDuration).toLong())
            return
        }


        if (getTotalVideoDuration() - layout.recordProgressView.currentProgress in 1..1000) {
            return
        }
        cameraHelper.stopCaptureVideo()
    }

    override fun onVideoRecordingStart() {
        Timber.tag("DUET").d("onVideoRecordingStart()")
        isRecordingEnd = false
        Handler().postDelayed({
            player.playWhenReady = true
        }, 1000)
        curProgressHandler = handler
        currentProgressRunnable = runnable
        prevProgressTime = System.currentTimeMillis() + 1000
        handler.postDelayed(runnable, 1000)
        lastRecordStartTime = System.currentTimeMillis()
    }

    override fun onVideoRecordingEnd() {
        isRecordingEnd = true
        player.playWhenReady = false
        handler.removeCallbacks(runnable)

        layout.options.visibility = View.VISIBLE
        layout.ivBack.visibility = View.VISIBLE
        layout.btnDeleteSegment.visibility = View.VISIBLE
        layout.btnDone.visibility = View.VISIBLE

        Timber.tag("DUET").d("calculated duration: ${System.currentTimeMillis() - lastRecordStartTime}")
    }

    override fun onVideoTaken(file: File) {
        Timber.tag("DUET").d("onVideoRecordingEnd()")
        layout.recordButton.setState(false)
        layout.recordProgressView.addPause(layout.recordProgressView.currentProgress)

        val pauseMarkers = layout.recordProgressView.pauseMarkers()
        val calculatedDuration = if (pauseMarkers.size > 1) {
            pauseMarkers[pauseMarkers.size - 1].progressValue - pauseMarkers[pauseMarkers.size - 2].progressValue
        } else {
            layout.recordProgressView.currentProgress
        }

        val actualDuration = fetchSegmentDuration(file)
        val delay = actualDuration - calculatedDuration


        Timber.tag("DUET").d("progress view duration: ${layout.recordProgressView.currentProgress}")
        Timber.tag("DUET").d("calculated duration: ${calculatedDuration}")
        Timber.tag("DUET").d("file duration: ${actualDuration}")

        val videoSegment = VideoSegment(file.toUri(), actualDuration, false).apply {
            if (delay > 0) {
                startTime = delay
            }
        }
        videoSegmentList.add(videoSegment)
    }

    override fun onVideoRecordingError() {
        layout.recordProgressView.deleteLastSegment()
        checkSegmentList()
        isRecordingEnd = true
    }

    private fun fetchSegmentDuration(videoSegmentFile: File): Int {
        try {
            metadataRetriever.setDataSource(videoSegmentFile.absolutePath)
            return metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    private fun fetchSegmentHeight(video: Uri): Int {
        return try {
            metadataRetriever.setDataSource(requireContext(), video)
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun getTotalVideoDuration(): Int {
        return if (videoDuration != null) {
            videoDuration!!
        } else {
            val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(args.video.path) }
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt().also {
                videoDuration = it
                Timber.tag("DUET").d("video duration: $videoDuration")
                mediaMetadataRetriever.release()
            }
        }
    }

    private fun deleteLastSegment() {
        if (videoSegmentList.isNotEmpty()) {
            videoSegmentList.removeAt(videoSegmentList.size - 1)
            layout.recordProgressView.deleteLastSegment()
            val pauseMarkers = layout.recordProgressView.pauseMarkers()
            val curRecordedTime = if (pauseMarkers.isEmpty()) 0 else pauseMarkers.last().progressValue
            player.seekTo(curRecordedTime.toLong())
            checkSegmentList()
        }
    }

    private fun checkSegmentList() {
        if (videoSegmentList.size == 0) {
            layout.btnDone.visibility = View.GONE
            layout.btnDeleteSegment.visibility = View.GONE
        } else {
            layout.btnDone.visibility = View.VISIBLE
            layout.btnDeleteSegment.visibility = View.VISIBLE
        }
    }

    private fun showConfirmDeleteSegmentDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.delete_story_segment_dialog_no, null)
                .setPositiveButton(R.string.delete_story_segment_dialog_yes) {
                    deleteLastSegment()
                }
                .setCanceledOnTouchOutside(true)
                .setMessage(R.string.delete_story_segment_dialog_msg)

        builder.build().show(childFragmentManager, "DeleteMediaPostDialog")
    }

    private fun onDoneClick() {
        if (layout.recordProgressView.currentProgress < 1000) {
            ToastUtils.showToastMessage(R.string.story_min_length_msg)
        } else {
            layout.btnDone.isEnabled = false
            layout.progressBar.visibility = View.VISIBLE
            val story = ConstructStory.fromDuet(args.video, videoSegmentList)
            navigateToPreviewStory(story)
        }
    }

    private fun navigateToPreviewStory(story: ConstructStory) {
        layout.progressBar.visibility = View.GONE
        player.playWhenReady = false
        val direction = CreateDuetStoryFragmentDirections.toPreviewStory(story)
        findNavController().navigate(direction)
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
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", Config.getPackageName(), null)
        intent.data = uri
        startActivity(intent)
    }

    private fun checkVideoHasAudioTrack(videoUri: Uri): Boolean {
        val mediaMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(requireContext(), videoUri) }
        val hasAudioStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        val hasAudio = hasAudioStr != null && hasAudioStr == "yes"
        mediaMetadataRetriever.release()
        return hasAudio
    }

    private fun showHeadphonesMessage() {
        ToastUtils.showToastMessage(R.string.duet_headphones_message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recordProgress = layout.recordProgressView.currentProgress
        pauseMarkers = layout.recordProgressView.pauseMarkers()
    }

    override fun onDestroy() {
        super.onDestroy()
        metadataRetriever.release()
        player.release()
    }
}