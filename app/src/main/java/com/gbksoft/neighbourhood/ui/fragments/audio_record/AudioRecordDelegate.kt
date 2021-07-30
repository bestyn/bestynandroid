package com.gbksoft.neighbourhood.ui.fragments.audio_record

import android.media.MediaRecorder
import com.gbksoft.neighbourhood.app.NApplication.Companion.context
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.google.android.exoplayer2.Player
import java.io.File
import java.io.IOException

class AudioRecordDelegate : Player.EventListener {

    var fileName: String = "tempAudioRecord${CreateEditPostFragment.audioAttachmentCount}"

    private var recorder: MediaRecorder? = null

    init {
        val file = File(context.cacheDir, "/$fileName")
        fileName = file.absolutePath
    }


    fun startRecording() {
        fileName = "tempAudioRecord${CreateEditPostFragment.audioAttachmentCount}"
        val file = File(context.cacheDir, "/$fileName")
        fileName = file.absolutePath
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
                start()
            } catch (e: IOException) {

            }


        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()

            recorder = null

        } catch (e: Exception){

        }
    }
}