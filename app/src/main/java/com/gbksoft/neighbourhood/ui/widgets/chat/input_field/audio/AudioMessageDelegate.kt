package com.gbksoft.neighbourhood.ui.widgets.chat.input_field.audio

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Handler
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutInputMessageBinding
import com.gbksoft.neighbourhood.model.LocalFile
import com.gbksoft.neighbourhood.model.chat.Attachment
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import com.gbksoft.neighbourhood.utils.DimensionUtil
import com.gbksoft.neighbourhood.utils.LocalFileFactory
import com.gbksoft.neighbourhood.utils.ToastUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AudioMessageDelegate(
    private val context: Context,
    private val layout: LayoutInputMessageBinding,
    private val audioRecordPermissionRequestListener: () -> Unit,
    private val audioRecordListener: (localFile: LocalFile<Int>) -> Unit
) {
    private lateinit var pulseAnimation: AnimatorSet
    private lateinit var swipeAnimation: AnimatorSet

    private val localFileFactory = LocalFileFactory(context)

    private var canceled = false
    private var audioFileName: String? = null
    private var audioFile: File? = null
    private var mediaRecorder: MediaRecorder? = null
    private val timerTickDuration = 1000L
    private val minRecordTime = 1000L
    private var timerFormatter = SimpleDateFormat("mm:ss", Locale.US)
    private var startRecordTime: Long = 0
    private var stopRecordTime: Long = 0
    private var recordingStoppedCorrect = false
    private val handler = Handler()
    private val timerRunnable = object : Runnable {
        override fun run() {
            tikTimer()
            handler.postDelayed(this, timerTickDuration)
        }
    }

    init {
        initPulseAnimation()
        initSwipeAnimation()
        setupAudioRecordButton()
    }

    private fun initPulseAnimation() {
        val scaleX = ObjectAnimator.ofFloat(layout.ivVoiceMessagePulse, "scaleX", 1.5f)
        val scaleY = ObjectAnimator.ofFloat(layout.ivVoiceMessagePulse, "scaleY", 1.5f)
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatCount = ValueAnimator.INFINITE
        pulseAnimation = AnimatorSet()
        pulseAnimation.play(scaleX).with(scaleY)
        pulseAnimation.duration = 500
        pulseAnimation.interpolator = DecelerateInterpolator()
    }

    private fun initSwipeAnimation() {
        val translationXValue = -DimensionUtil.dpToPx(10f, context).toFloat()
        val translationX = ObjectAnimator.ofFloat(layout.swipeToCancel, "translationX", translationXValue)
        translationX.repeatMode = ValueAnimator.REVERSE
        translationX.repeatCount = ValueAnimator.INFINITE
        translationX.interpolator = LinearInterpolator()
        swipeAnimation = AnimatorSet()
        swipeAnimation.duration = 1500
        swipeAnimation.play(translationX)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAudioRecordButton() {
        layout.btnVoiceMessage.cancelBarrierXProvider = {
            Timber.tag("AudioTag").d("layout.cancelRecordBarrier.x: ${layout.cancelRecordBarrier.x}")

            layout.cancelRecordBarrier.x
        }
        layout.btnVoiceMessage.onActionDownListener = {
            if (isRecordPermissionGranted()) {
                showRecordingUI()
                startRecord()
                canceled = false
                true
            } else {
                audioRecordPermissionRequestListener.invoke()
                false
            }
        }
        layout.btnVoiceMessage.onActionUpListener = {
            hideRecordingUI()
            stopRecord()
            if (!canceled) onAudioRecordDone()
        }
        layout.btnVoiceMessage.onActionCancelListener = {
            hideRecordingUI()
            stopRecord()
            canceled = true
        }
        layout.btnVoiceMessage.onPositionXChangedListener = { btnX ->
            val center = btnX + layout.btnVoiceMessage.width / 2
            layout.ivVoiceMessagePulse.x = center - layout.ivVoiceMessagePulse.width / 2
        }
    }

    private fun isRecordPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(layout.root.context,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRecordingUI() {
        layout.messageField.visibility = View.GONE
        layout.btnAddAttachment.visibility = View.GONE
        layout.groupAudioRecord.visibility = View.VISIBLE
        layout.ivVoiceMessagePulse.visibility = View.VISIBLE
        pulseAnimation.start()
        swipeAnimation.start()
    }

    private fun startRecord() {
        startRecording()
        recordingStoppedCorrect = false
        startRecordTime = System.currentTimeMillis()
        tikTimer()
        handler.postDelayed(timerRunnable, timerTickDuration)
    }

    private fun startRecording() {
        val file = createAudioFile()
        audioFile = file
        audioFileName = "Record_${DateTimeUtils.timeForTempFile}.aac"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(file.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
                start()
                Timber.tag("AudioTag").d("start()")
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }

        }
    }

    private fun createAudioFile(): File {
        return File.createTempFile("AudioRecord_", null)
    }

    private fun tikTimer() {
        layout.tvAudioRecordTimer.text = timerFormatter.format(System.currentTimeMillis() - startRecordTime)
    }

    private fun hideRecordingUI() {
        layout.messageField.visibility = View.VISIBLE
        layout.btnAddAttachment.visibility = View.VISIBLE
        layout.groupAudioRecord.visibility = View.GONE
        pulseAnimation.end()
        swipeAnimation.end()
        layout.ivVoiceMessagePulse.visibility = View.GONE
    }

    private fun stopRecord() {
        handler.removeCallbacks(timerRunnable)
        layout.tvAudioRecordTimer.text = ""
        stopRecording()
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                stopRecordTime = System.currentTimeMillis()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                release()
                recordingStoppedCorrect = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
        Timber.tag("AudioTag").d("stop()")
    }

    private fun onAudioRecordDone() {
        if (!recordingStoppedCorrect) return
        if (isTooShortRecord()) {
            showHoldToRecordMessage()
            return
        }
        Timber.tag("AudioTag").d("onAudioRecordDone(), file: ${audioFile?.absolutePath}")
        val file = audioFile ?: return
        val localFile = localFileFactory.fromFile(file, "audio/x-aac", Attachment.TYPE_AUDIO, audioFileName)
        audioRecordListener.invoke(localFile)
    }

    private fun isTooShortRecord(): Boolean {
        val time = stopRecordTime - startRecordTime
        return time < minRecordTime
    }

    private fun showHoldToRecordMessage() {
        ToastUtils.showToastMessage(context, R.string.toast_hold_to_record)
    }
}