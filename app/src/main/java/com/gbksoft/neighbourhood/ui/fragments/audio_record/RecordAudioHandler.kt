package com.gbksoft.neighbourhood.ui.fragments.audio_record

interface RecordAudioHandler {
    fun startRecording()
    fun stopRecording(callBack: ((String) -> Unit))
    fun minimizeRecord(callBack: ((String) -> Unit))

    fun addAudioRecordListener(listener: AudioRecorderListener)
}