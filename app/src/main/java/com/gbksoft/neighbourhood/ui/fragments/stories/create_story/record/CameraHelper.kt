package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record

import android.content.Context
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Flash
import timber.log.Timber
import java.io.File

class CameraHelper : LifecycleObserver {
    var onCameraOptionsListener: ((CameraOptions) -> Unit)? = null
    var videoRecordingListener: VideoRecordingListener? = null

    private var context: Context? = null
    private var cameraView: CameraView? = null

    fun init(context: Context, lifecycleOwner: LifecycleOwner, cameraView: CameraView) {
        lifecycleOwner.lifecycle.addObserver(this)
        this.context = context
        this.cameraView = cameraView
        setupCameraView()
    }

    private var currentCameraOptions: CameraOptions? = null

    private fun setupCameraView() {
        cameraView?.apply {
            addCameraListener(object : CameraListener() {
                override fun onCameraOpened(options: CameraOptions) {
                    onCameraOptionsChanged(options)
                }

                override fun onVideoRecordingStart() {
                    videoRecordingListener?.onVideoRecordingStart()
                }

                override fun onCameraError(exception: CameraException) {
                    if (exception.reason == CameraException.REASON_VIDEO_FAILED) {
                        videoRecordingListener?.onVideoRecordingError()
                    }
                }

                override fun onVideoRecordingEnd() {
                    videoRecordingListener?.onVideoRecordingEnd()
                }

                override fun onVideoTaken(result: VideoResult) {
                    videoRecordingListener?.onVideoTaken(result.file)
                }
            })
        }
    }

    private fun onCameraOptionsChanged(options: CameraOptions) {
        currentCameraOptions = options
        onCameraOptionsListener?.invoke(options)
    }

    fun toggleFacing() {
        cameraView?.toggleFacing()
    }

    fun switchTorch() {
        val options = currentCameraOptions ?: return
        if (options.supportedFlash.not { contains(Flash.TORCH) }) return

        cameraView?.let {
            it.flash = if (it.flash == Flash.TORCH) Flash.OFF else Flash.TORCH
        }
    }

    fun enableMicrophone() {
        cameraView?.let {
            it.audio = Audio.ON
        }
    }

    fun disableMicrophone() {
        cameraView?.let {
            it.audio = Audio.OFF
        }
    }

    fun startCaptureVideo() {
        val filesDir = context?.filesDir ?: return
        val dir = File(filesDir, "story")
        if (!dir.exists()) {
            dir.mkdir()
        }
        val file = File(dir, MediaUtils.generateVideoFileName())
        Timber.tag("CameraTag").d("startCaptureVideo")
        cameraView?.takeVideo(file)
    }

    fun startCaptureSnapshot() {
        val filesDir = context?.filesDir ?: return
        val dir = File(filesDir, "story")
        if (!dir.exists()) {
            dir.mkdir()
        }
        val file = File(dir, MediaUtils.generateVideoFileName())
        Timber.tag("CameraTag").d("startCaptureVideo")
        cameraView?.takeVideoSnapshot(file)
    }

    fun stopCaptureVideo() {
        Timber.tag("CameraTag").d("stopCaptureVideo")
        cameraView?.stopVideo()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        context = null
        cameraView = null
    }
}


interface FFmpegListener {
    fun onSuccess(resFileUri: Uri)
    fun onError()
}