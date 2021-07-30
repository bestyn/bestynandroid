package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.preview

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.mappers.media.MediaMapper.getAudioDuration
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.ui.widgets.stories.audio.TrimAudioLargeView
import com.gbksoft.neighbourhood.ui.widgets.stories.audio.TrimAudioView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AudioThumbHelper(val audioPlayer: SimpleExoPlayer, val trimAudioView: View, val context: Context, val playAuto: Boolean = true) {

    private val progressObservable = Observable.interval(16, TimeUnit.MILLISECONDS)
    private var audioProgressDisposable: Disposable? = null

    val audioDurationLiveData = SingleLiveEvent<Int>()

    var audio: Media? = null
    var audioUri: Uri? = null
    var startTime = 0
    var audioLength = -1


    fun init(media: Media, durationCallback: ((Int) -> Unit)? = null) {
        audio = media
        prepareAudioPlayer(durationCallback = durationCallback)
    }

    fun initUri(mediaUri: Uri, durationCallback: ((Int) -> Unit)? = null) {
        audioUri = mediaUri
        prepareAudioPlayerUri(durationCallback = durationCallback)
    }

    private fun prepareAudioPlayer(autoPlay: Boolean = false, start: Int = startTime, durationCallback: ((Int) -> Unit)? = null) {
        val uri =
                if (audio != null)
                    audio?.origin
                else
                    return


        CoroutineScope(Dispatchers.IO + Job()).launch {
            val audioLength = if (audio?.length == 0) {
                120000
            } else {
                audio?.length
            }
            audioLength?.let { duration ->

                durationCallback?.invoke(duration)

                Log.d("sdvsvsvs", "prepare audio start $start")
                Log.d("sdvsvsvs", "prepare audio duration $audioLength")

                val dataSourceFactory = DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, context.getString(R.string.app_name)))
                val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri)
                val clippingSource = ClippingMediaSource(audioSource, start * 1000L, duration * 1000L)
                val loopingSource = LoopingMediaSource(clippingSource)

                audioPlayer.prepare(loopingSource)
                if (autoPlay) {
                    audioPlayer.playWhenReady = true
                } else {
                    resetProgress()
                }
                subscribeAudioProgress(false)

                audioDurationLiveData.postValue(audio?.origin?.getAudioDuration())
            }
        }
    }

    private fun prepareAudioPlayerUri(autoPlay: Boolean = false, start: Int = startTime, durationCallback: ((Int) -> Unit)? = null) {
        val uri = if (audioUri != null)
            audioUri
        else
            return


        CoroutineScope(Dispatchers.IO + Job()).launch {
            if (audioLength == -1) {
                audioLength = audioUri?.getAudioDuration() ?: 0
            }

            audioLength.let { duration ->

                Log.d("sdvsvsvs", "prepare audio start uri $start")
                Log.d("sdvsvsvs", "prepare audio duration uri $audioLength")

                durationCallback?.invoke(duration)
                audioDurationLiveData.postValue(duration)

                val dataSourceFactory = DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, context.getString(R.string.app_name)))
                val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri)
                val clippingSource = ClippingMediaSource(audioSource, start * 1000L, duration * 1000L)
                val loopingSource = LoopingMediaSource(clippingSource)
                audioPlayer.prepare(loopingSource)
                /*  val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
                  val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
                  audioPlayer.prepare(audioSource)*/

                subscribeAudioProgress(true)

                if (autoPlay) {
                    audioPlayer.playWhenReady = true
                } else {
                    resetProgress()
                }
            }
        }
    }

    fun onAudioTimeThumbChanged(timeInMs: Int, audio1: Media? = audio) {
        audio = audio1
        startTime = timeInMs
        Log.d("sdvsvsvs", "thumb change $timeInMs")
        audioPlayer.seekTo(timeInMs.toLong())
        //prepareAudioPlayer(true, timeInMs)
        if (trimAudioView is TrimAudioView) {
            trimAudioView.skipFirstLevels(timeInMs)
        } else if (trimAudioView is TrimAudioLargeView) {
            trimAudioView.skipFirstLevels(timeInMs)
        }
    }

    fun onAudioTimeThumbChangedUri(timeInMs: Int, audio1: Uri? = audioUri) {
        audioUri = audio1
        Log.d("sdvsvsvs", "thumb change uri $timeInMs")
        startTime = timeInMs
        audioPlayer.seekTo(timeInMs.toLong())
        //prepareAudioPlayerUri(true, timeInMs)
        if (trimAudioView is TrimAudioView) {
            trimAudioView.skipFirstLevels(timeInMs)
        } else if (trimAudioView is TrimAudioLargeView) {
            trimAudioView.skipFirstLevels(timeInMs)
        }
    }

    fun resetProgress() {
        startTime = 0
        audioPlayer.seekTo(0)
        if (trimAudioView is TrimAudioView) {
            //trimAudioView.skipFirstLevels(0)
            trimAudioView.resetTimer()
        } else if (trimAudioView is TrimAudioLargeView) {
            // trimAudioView.skipFirstLevels(0)
        }
    }

    fun subscribeAudioProgress(isUri: Boolean) {
        if (isUri && audioUri == null || isUri.not() && audio == null) {
            return
        }
        audioProgressDisposable?.dispose()
        audioProgressDisposable = progressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val audioStartTime = startTime ?: return@subscribe
                    val progress = audioPlayer.currentPosition + audioStartTime

                    if (trimAudioView is TrimAudioView) {
                        trimAudioView.setPlayingProgress(progress)
                    } else if (trimAudioView is TrimAudioLargeView) {
                        trimAudioView.setPlayingProgress(progress)
                    }
                }
    }
}