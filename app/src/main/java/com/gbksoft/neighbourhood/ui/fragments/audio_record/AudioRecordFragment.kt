package com.gbksoft.neighbourhood.ui.fragments.audio_record

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Chronometer
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentAudioRecordBinding
import com.gbksoft.neighbourhood.model.post.GeneralPost
import com.gbksoft.neighbourhood.ui.activities.main.FloatingMenuDelegate
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.preview.AudioThumbHelper
import com.gbksoft.neighbourhood.utils.permission.DexterMultiplePermissionListener
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.karumi.dexter.Dexter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit

class AudioRecordFragment : SystemBarsColorizeFragment(), Player.EventListener {

    private lateinit var layout: FragmentAudioRecordBinding

    private val viewModel by viewModel<AudioRecordViewModel>()

    private val args by navArgs<AudioRecordFragmentArgs>()

    private lateinit var scaleUpOuter: Animation
    private lateinit var scaleUpInner: Animation
    private lateinit var timerTask: TimerTask

    private var hasPermissions = false

    var currentState = RecordState.INIT

    private var hasAudio = false

    var fileName: String = ""

    private var playbackPosition: Long = 0

    private lateinit var player: SimpleExoPlayer

    var audioThumbHelper: AudioThumbHelper? = null

    var timeDelta = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = SimpleExoPlayer.Builder(requireContext()).build()
        player.addListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_record, container, false)
        return layout.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestMicrophonePermission()
        setOnClickListeners()
        setOnChronometerTickListener()

        onInit()


        audioThumbHelper = AudioThumbHelper(player, layout.trimAudioRecord, requireContext(), playAuto = false)

        audioThumbHelper?.audioDurationLiveData?.observe(this, androidx.lifecycle.Observer {
            layout.textAudioRecordedTime.text = it.toLong().toTimeString()
        })

        layout.trimAudioRecord.onTimeChangedListener = {
            audioThumbHelper?.onAudioTimeThumbChangedUri(it, fileName.toUri())
            Log.d("timtimtitmin", it.toString())
            timeDelta = it
            //layout.textRecordTime.base = SystemClock.elapsedRealtime()
      /*      val time = layout.textRecordTime.base - it/1000
            layout.textRecordTime.base = time
            layout.textRecordTime.text = time.toTimeString()*/
        }

        scaleUpOuter = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up_outer)
        scaleUpInner = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up_inner)

        when {
            args.wasHidden && args.filePath.isEmpty() -> {
                layout.textRecordTime.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                //audioPlayer.recordStateLiveData.postValue(AudioPlayer.RecordState.RECORDING)
                onRecordingStarted()
                layout.textRecordTime.restore(viewModel.getChronometerBase())
                hasAudio = true
            }
            args.wasHidden && args.filePath.isNotEmpty() -> {
                onRecordingStopped(args.filePath)
                audioThumbHelper?.initUri(args.filePath.toUri())
                layout.trimAudioRecord.setAudioUri(args.filePath.toUri())
            }
            args.wasHidden.not() -> {
                layout.textRecordTime.reset()
            }
        }
    }

    private fun setOnClickListeners() {
        layout.btnRecord.setOnClickListener {
            if (hasPermissions) {
                when (currentState) {
                    RecordState.INIT -> {
                        layout.textRecordTime.reset()
                        layout.textRecordTime.start()
                        layout.textRecordTime.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                        (getParentActivity() as? RecordAudioHandler)?.startRecording()
                        //audioPlayer.onRecordingStarted()
                        onRecordingStarted()
                        hasAudio = false
                    }
                    RecordState.RECORDING -> {
                        (getParentActivity() as? RecordAudioHandler)?.stopRecording { fileName1 ->
                            fileName = fileName1
                        }

                        onRecordingStopped()
                    }
                    RecordState.RECORDED -> {
                        hasAudio = true
                        startPlaying()
                    }
                    RecordState.PLAYING -> {
                        stopPlaying()
                    }
                    RecordState.STOPPED -> {
                        startPlaying()
                    }
                }
            } else {
                requestMicrophonePermission()
            }
        }

        layout.btnRecordRemove.setOnClickListener {
            stopPlaying()
            onInit()
            timeDelta = 0
        }

        layout.btnRecordDone.setOnClickListener {
            setFragmentResult("audioRecordAttachment", bundleOf("audioRecordAttachment" to fileName))
            goBack()
            timeDelta = 0
        }

        layout.btnMinimizeRecord.setOnClickListener {
            minimizeRecord()
            viewModel.saveChronometerBase(layout.textRecordTime.base)
            currentTime = System.currentTimeMillis() - layout.textRecordTime.base
        }

        layout.btnMinimizeRecordActionBar.setOnClickListener {
            layout.btnRecord.performClick()
            minimizeRecord()
            viewModel.saveChronometerBase(layout.textRecordTime.base)
        }

        layout.actionBar.setEventHandler {
            if (FloatingMenuDelegate.userIsRecordingAudio) {
                val builder = YesNoDialog.Builder()
                        .setNegativeButton(R.string.create_story_from_gallery_dialog_cancel, null)
                        .setPositiveButton(R.string.adjust_story_cancel_changes_dialog_ok) {
                            (getParentActivity() as? RecordAudioHandler)?.stopRecording { fileName1 ->
                                fileName = fileName1
                            }
                            onRecordingStopped()
                            goBack()
                        }
                        .setMessage(R.string.add_text_cancel_changes_dialog_msg)
                        .setCanceledOnTouchOutside(true)
                        .build()
                builder.show(parentFragmentManager, "AudioRecordCloseDialog")

            } else {
                goBack()
            }
        }
    }

    fun startPlaying() {
        onStartPlaying()
        player.playWhenReady = true
        player.addListener(this)
    }

    private fun setOnChronometerTickListener() {
        layout.textRecordTime.setOnChronometerTickListener {
            val time = SystemClock.elapsedRealtime() - layout.textRecordTime.base + timeDelta
            layout.textRecordTime.text = time.toTimeString()
            if (TimeUnit.MILLISECONDS.toMillis(time) >= MAX_RECORD_LENGTH_MILLIS) {
                (getParentActivity() as? RecordAudioHandler)?.stopRecording { fileName1 ->
                    fileName = fileName1
                }

                currentTime = TimeUnit.SECONDS.toMillis(0)

                onRecordingStopped()
            }
            audioThumbHelper?.audioDurationLiveData?.value?.let {
                if (TimeUnit.MILLISECONDS.toMillis(time) >= it) {
                    stopPlaying()
                    layout.trimAudioRecord.setPlayingProgress(0)
                }
            }

        }
    }

    private fun requestMicrophonePermission() {
        val permissionListener = DexterMultiplePermissionListener()
        permissionListener.onPermissionsChecked = { report ->
            if (report.areAllPermissionsGranted()) {
                hasPermissions = true
            }
            if (report.isAnyPermissionPermanentlyDenied) {
                showAllowCameraAndMicDialog()
            }
        }
        permissionListener.onPermissionToken = {
            it.continuePermissionRequest()
        }
        Dexter.withContext(context)
                .withPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(permissionListener)
                .onSameThread()
                .check()
    }

    private fun showAllowCameraAndMicDialog() {
        val builder = YesNoDialog.Builder()
                .setTitle("Microphone and Storage Permissions required")
                .setMessage(R.string.story_media_camera_permission_msg)
                .setNegativeButton("Please go to settings to enable Microphone and Storage so that you can record a Story on Bestyn.", null)
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

    fun onRecordingStopped(file: String = fileName) {
        fileName = file
        onRecordingStopped1()

        audioThumbHelper?.initUri(fileName.toUri())
        layout.trimAudioRecord.setAudioUri(fileName.toUri())
      //  layout.trimAudioRecord.setPlayingProgress(1500)
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    fun stopPlaying() {
        player.playWhenReady = false
        onStopPlaying()
        //layout.trimAudioRecord.setAudioUri(fileName.toUri())
        //audioThumbHelper?.init(fileName.toUri())
    }

    fun releasePlayer() {
        playbackPosition = player.currentPosition
        player.release()
    }


    private fun onInit() {
        currentState = RecordState.INIT
        layout.textRecordTime.reset()
        layout.textRecordTime.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_green, null))
        layout.btnRecord.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_microphone, null))
        layout.btnMinimizeRecordActionBar.visibility = View.VISIBLE
        hideButtons()
    }


    private fun onRecordingStarted() {
        currentState = RecordState.RECORDING
        layout.btnRecord.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_stop_recording, null))
        layout.btnMinimizeRecord.visibility = View.VISIBLE
        layout.btnMinimizeRecordActionBar.visibility = View.GONE
        showRecordingAnimation()
        hideButtons()
    }

    private fun onRecordingStopped1() {
        currentState = RecordState.RECORDED
        layout.textRecordTime.stop()
        layout.textRecordTime.reset()
        layout.btnRecord.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_start_play, null))
        layout.btnMinimizeRecord.visibility = View.GONE
        hideRecordingAnimation()
        showButtons()
    }

    private fun onStartPlaying() {
        currentState = RecordState.PLAYING
        layout.btnRecord.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_stop_play, null))
        layout.textRecordTime.reset()
        layout.textRecordTime.start()
    }

    private fun onStopPlaying() {
        currentState = RecordState.STOPPED
        layout.btnRecord.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_start_play, null))
        layout.textRecordTime.stop()
        layout.textRecordTime.reset()
    }

    private fun minimizeRecord() {
        (getParentActivity() as? RecordAudioHandler)?.addAudioRecordListener(object : AudioRecorderListener {
            override fun onAudioRecorded(fileName2: String) {
                (getParentActivity() as? RecordAudioHandler)?.stopRecording { fileName1 ->
                    fileName = fileName1
                }

                onRecordingStopped()
            }

        })
        (getParentActivity() as? RecordAudioHandler)?.minimizeRecord({})
    }

    private fun showRecordingAnimation() {
        timerTask = object : TimerTask() {
            override fun run() {
                CoroutineScope(Dispatchers.Main).launch {
                    layout.imageRecordShapeOuter.startAnimation(scaleUpOuter)
                    layout.imageRecordShapeInner.startAnimation(scaleUpInner)
                }
            }
        }
        val timer = Timer().scheduleAtFixedRate(timerTask, 0, 1000)
    }

    private fun hideRecordingAnimation() {
        layout.imageRecordShapeOuter.visibility = View.GONE
        layout.imageRecordShapeInner.visibility = View.GONE
        if (this::timerTask.isInitialized) {
            timerTask.cancel()
        }
    }

    private fun showButtons() {
        layout.btnRecordRemove.visibility = View.VISIBLE
        layout.btnRecordDone.visibility = View.VISIBLE
        layout.textAudioRecordedTime.visibility = View.VISIBLE
        layout.trimAudioRecord.visibility = View.VISIBLE
    }

    private fun hideButtons() {
        layout.btnRecordRemove.visibility = View.INVISIBLE
        layout.btnRecordDone.visibility = View.INVISIBLE
        layout.textAudioRecordedTime.visibility = View.INVISIBLE
        layout.trimAudioRecord.visibility = View.INVISIBLE
    }

    private fun goBack() {
        val direction = AudioRecordFragmentDirections.toCreatePost(GeneralPost.empty())
        findNavController().navigate(direction)
    }

    private fun Chronometer.reset() {
        base = SystemClock.elapsedRealtime()
        text = "00:00:00"
    }

    private fun Chronometer.restore(time: Long) {
        base = time
        text = (SystemClock.elapsedRealtime() - time).toTimeString()
        start()
    }

    override fun onPlayerError(error: ExoPlaybackException) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playWhenReady) {
            onStartPlaying()
        } else {
            onStopPlaying()
        }

        if (playbackState == ExoPlayer.STATE_ENDED) {
            onStopPlaying()
        }
    }


    private fun Long.toTimeString(): String {
        val hours = (this / 3600000).toInt()
        val minutes = ((this - hours * 3600000) / 60000).toInt()
        val seconds = ((this - hours * 3600000 - minutes * 60000) / 1000).toInt()
        return (if (hours < 10) "0$hours" else hours.toString()) + ":" + (if (minutes < 10) "0$minutes" else minutes) + ":" + (if (seconds < 10) "0$seconds" else seconds)
    }

    companion object {
        val MAX_RECORD_LENGTH_MILLIS = TimeUnit.HOURS.toMillis(5)
        //val MAX_RECORD_LENGTH_MILLIS = TimeUnit.SECONDS.toMillis(120)
        var currentTime = TimeUnit.SECONDS.toMillis(0)
    }
}

enum class RecordState {
    INIT, RECORDING, RECORDED, PLAYING, STOPPED
}