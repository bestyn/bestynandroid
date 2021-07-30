package com.gbksoft.neighbourhood.ui.widgets.chat.audio

import android.content.Context
import android.os.Handler
import androidx.core.net.toUri
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.widgets.chat.speech.TextToSpeechManager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class AudioPlaybackManager(private val context: Context) : AnalyticsListener {
    var textToSpeechManager: TextToSpeechManager? = null
    var audioPlaybackListener: AudioPlaybackListener? = null

    companion object {
        const val PROGRESS_UPDATE_PERIOD = 300L
    }

    private var onPlay = false
    private var currentId: Long? = null
    private val handler = Handler()
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (onPlay) handler.postDelayed(this, PROGRESS_UPDATE_PERIOD)
            checkProgress()
        }

    }

    private val player = SimpleExoPlayer.Builder(context)
        .build()
        .apply {
            addAnalyticsListener(this@AudioPlaybackManager)
        }

    fun playOrPauseAudio(url: String, id: Long) {
        Timber.tag("AudioTag").d("playOrPauseAudio() textToSpeechManager: ${textToSpeechManager}")
        textToSpeechManager?.stop()
        if (currentId == id) {
            playOrPause(url)
            return
        }

        stop()
        currentId = id
        preparePlayer(url)
    }

    private fun preparePlayer(url: String) {
        val dataSourceFactory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(context, context.getString(R.string.app_name)))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(url.toUri())
        onPlay = true
        player.playWhenReady = false
        player.prepare(audioSource)
    }

    private fun playOrPause(url: String) {
        when {
            player.isPlaying -> {
                pause()
            }
            player.playbackState == Player.STATE_READY -> {
                onPlay = true
                play()
            }
            else -> {
                preparePlayer(url)
            }
        }
    }

    fun play() {
        if (player.playbackState == Player.STATE_READY && isNotPlaying()) {
            handler.post(progressRunnable)
            onPlayingStateChanged(true)
            player.playWhenReady = true
        }
    }

    fun pause() {
        onPlay = false
        if (player.playbackState == Player.STATE_READY && player.isPlaying) {
            resetState()
        }
        onPlayingStateChanged(false)
    }

    private fun onPlayingStateChanged(isPlaying: Boolean) {
        Timber.tag("AudioTag").d("onPlayingStateChanged: $isPlaying    currentId: $currentId")
        val id = currentId ?: return
        audioPlaybackListener?.onAudioPlaybackStateChanged(id, isPlaying)
    }

    private fun onAudioPlaybackStopped() {
        val id = currentId ?: return
        audioPlaybackListener?.onAudioPlaybackStopped(id)
    }

    override fun onPlayerStateChanged(eventTime: AnalyticsListener.EventTime, playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                if (onPlay) play()
            }
            Player.STATE_ENDED -> {
                checkProgress()
                stop()
            }
        }
    }

    fun checkProgress() {
        when (player.playbackState) {
            Player.STATE_READY -> onProgressChanged(player.duration, player.currentPosition)
            Player.STATE_ENDED -> onProgressChanged(player.duration, player.duration)
        }
    }

    fun stop() {
        onPlay = false
        resetState()
        player.stop()
        onAudioPlaybackStopped()
    }

    fun releasePlayer() {
        player.release()
    }


    private fun resetState() {
        handler.removeCallbacks(progressRunnable)
        player.playWhenReady = false
    }

    private fun onProgressChanged(total: Long, current: Long) {
        val id = currentId ?: return
        audioPlaybackListener?.onAudioPlaybackProgressChanged(id, total, current)
    }

    private fun isNotPlaying(): Boolean = !player.isPlaying
}