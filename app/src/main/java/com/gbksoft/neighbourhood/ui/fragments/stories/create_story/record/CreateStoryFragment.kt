package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record

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
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentCreateStoryBinding
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.widgets.stories.progress_bar.RecordProgressView
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.permission.DexterMultiplePermissionListener
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.controls.Flash
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

private const val PROGRESS_STEP = 50L

class CreateStoryFragment : SystemBarsColorizeFragment(), VideoRecordingListener {
    private val viewModel by viewModel<CreateStoryViewModel>()
    private lateinit var layout: FragmentCreateStoryBinding
    private var cameraHelper: CameraHelper = CameraHelper().apply {
        onCameraOptionsListener = ::onCameraOptions
    }

    private var curProgressHandler: Handler? = null
    private var currentProgressRunnable: Runnable? = null
    private val stopRecordingRunnable = Runnable { stopRecord() }

    val videoSegmentList = mutableListOf<VideoSegment>()
    private var pauseMarkers: List<RecordProgressView.RecordPause>? = null
    private var recordProgress: Int? = null
    private var recordDuration: Int? = null
    private val metadataRetriever by lazy { MediaMetadataRetriever() }
    private var isRecordingEnd = true

    override fun getStatusBarColor(): Int = R.color.stories_screen_background
    override fun getNavigationBarColor(): Int = R.color.stories_screen_background
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hideNavigateBar()
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_create_story, container, false)
        setupView()
        checkCameraAndMicPermissions()
        setClickListeners()
        setupProgressBar()
        checkSegmentList()
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

    private fun checkGalleryPermissions() {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = {
            val direction = CreateStoryFragmentDirections.toImageVideoPicker(false)
            findNavController().navigate(direction)
        }

        permissionListener.onPermissionDenied = {
            if (it.isPermanentlyDenied) {
                showAllowGalleryDialog()
            }
        }
        permissionListener.onPermissionToken = {
            it.continuePermissionRequest()
        }
        Dexter.withContext(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(permissionListener)
                .withErrorListener { error: DexterError ->
                    ToastUtils.showToastMessage(requireActivity(), "Error occurred: $error")
                }
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

    private fun showAllowGalleryDialog() {
        val builder = YesNoDialog.Builder()
                .setTitle(R.string.story_media_storage_permission_title)
                .setMessage(R.string.story_media_storage_permission_msg)
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

    private fun setupView() {
        if (videoSegmentList.size == 0) {
            layout.btnDone.visibility = View.GONE
            layout.btnDeleteSegment.visibility = View.GONE
            layout.recordTimeSwitcher.visibility = View.VISIBLE
        } else {
            layout.btnDone.visibility = View.VISIBLE
            layout.btnDeleteSegment.visibility = View.VISIBLE
            layout.recordTimeSwitcher.visibility = View.GONE
        }
        layout.recordProgressView.totalProgress = viewModel.storyTime.millis
        layout.recordTimeSwitcher.currentTime = viewModel.storyTime
    }

    private fun setClickListeners() {
        layout.ivBack.setOnClickListener { findNavController().popBackStack() }
        layout.ivSwitchCamera.setOnClickListener { cameraHelper.toggleFacing() }
        layout.btnDeleteSegment.setOnClickListener { showConfirmDeleteSegmentDialog() }
        layout.recordButton.setOnClickListener { onRecordButtonClick() }
        layout.btnDone.setOnClickListener { onDoneClick() }
        layout.ivTextStory.setOnClickListener { navigateToCreateTextStory() }
        layout.ivCameraTorch.setOnClickListener { cameraHelper.switchTorch() }
        layout.ivFromGallery.setOnClickListener { checkGalleryPermissions() }
        layout.recordTimeSwitcher.timeSwitchListener = { time ->
            layout.recordProgressView.totalProgress = time.millis
            viewModel.storyTime = time
        }
        cameraHelper.videoRecordingListener = this
    }

    private fun onDoneClick() {
        if (layout.recordProgressView.currentProgress < 1000) {
            ToastUtils.showToastMessage(R.string.story_min_length_msg)
        } else {
            layout.btnDone.isEnabled = false
            layout.progressBar.visibility = View.VISIBLE
            navigateToPreviewStory(videoSegmentList)
        }
    }

    private fun setupProgressBar() {
        currentProgressRunnable?.let { curProgressHandler?.removeCallbacks(it) }
        layout.recordProgressView.setPauseMarkers(pauseMarkers)
        layout.recordProgressView.currentProgress = recordProgress ?: 0
        layout.recordProgressView.totalProgress = viewModel.storyTime.millis
    }

    private val handler = Handler()
    private var prevProgressTime = 0L
    private val runnable = object : Runnable {
        override fun run() {
            val curTime = System.currentTimeMillis()
            layout.recordProgressView.currentProgress += (curTime - prevProgressTime).toInt()
            prevProgressTime = curTime
            if (layout.recordProgressView.currentProgress > viewModel.storyTime.millis) {
                stopRecord()
            } else {
                handler.postDelayed(this, PROGRESS_STEP)
            }
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
            if (videoSegmentList.isEmpty()) {
                viewModel.storyTime = layout.recordTimeSwitcher.currentTime
            }
            startRecord()
        }
    }

    private fun startRecord() {
        if (not { isRecordingEnd }) return

        layout.btnDeleteSegment.visibility = View.GONE
        layout.btnDone.visibility = View.GONE
        layout.ivTextStory.visibility = View.GONE
        layout.options.visibility = View.GONE
        layout.ivBack.visibility = View.GONE
        layout.recordTimeSwitcher.visibility = View.GONE
        layout.ivFromGallery.visibility = View.GONE
        layout.recordButton.setState(true)
        curProgressHandler = handler
        currentProgressRunnable = runnable
        prevProgressTime = System.currentTimeMillis()
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

        cameraHelper.stopCaptureVideo()
    }

    override fun onVideoRecordingStart() {
        isRecordingEnd = false
        handler.postDelayed(runnable, 50)
    }

    override fun onVideoRecordingError() {
        layout.recordProgressView.deleteLastSegment()
        checkSegmentList()
        isRecordingEnd = true
    }

    override fun onVideoRecordingEnd() {

    }

    override fun onVideoTaken(file: File) {
        val duration = fetchSegmentDuration(file)
        if (duration > 0) {
            videoSegmentList.add(VideoSegment(file.toUri(), duration, false))
        } else {
            layout.recordProgressView.deleteLastSegment()
            checkSegmentList()
        }
        layout.options.visibility = View.VISIBLE
        layout.ivBack.visibility = View.VISIBLE
        layout.btnDeleteSegment.visibility = View.VISIBLE
        layout.btnDone.visibility = View.VISIBLE

        handler.removeCallbacks(runnable)
        layout.recordButton.setState(false)
        layout.recordProgressView.addPause(layout.recordProgressView.currentProgress)
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

    private fun deleteLastSegment() {
        videoSegmentList.removeAt(videoSegmentList.size - 1)
        layout.recordProgressView.deleteLastSegment()
        checkSegmentList()
    }

    private fun checkSegmentList() {
        if (videoSegmentList.size == 0) {
            layout.btnDeleteSegment.visibility = View.GONE
            layout.btnDone.visibility = View.GONE
            layout.recordTimeSwitcher.visibility = View.VISIBLE
            layout.ivTextStory.visibility = View.VISIBLE
            layout.ivFromGallery.visibility = View.VISIBLE
        }
    }

    private fun navigateToPreviewStory(videoSegments: List<VideoSegment>) {
        val story = ConstructStory.fromCamera(videoSegments)
        val direction = CreateStoryFragmentDirections.toPreviewStory(story)
        findNavController().navigate(direction)
    }

    private fun navigateToCreateTextStory() {
        val direction = CreateStoryFragmentDirections.toCreateTextStory()
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recordProgress = layout.recordProgressView.currentProgress
        pauseMarkers = layout.recordProgressView.pauseMarkers()
        recordDuration = layout.recordProgressView.totalProgress
    }
}