package com.gbksoft.neighbourhood.ui.widgets.chat.audio

interface AudioPlaybackListener {
    fun onAudioPlaybackStateChanged(id: Long, isPlaying: Boolean)

    /**
     * @param totalMs    in milliseconds
     * @param currentMs    in milliseconds
     */
    fun onAudioPlaybackProgressChanged(id: Long, totalMs: Long, currentMs: Long)
    fun onAudioPlaybackStopped(id: Long)
}