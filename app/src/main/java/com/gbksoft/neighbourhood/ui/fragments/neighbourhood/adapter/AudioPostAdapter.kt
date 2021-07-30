package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterAudioPostBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.activities.main.MainActivity
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.preview.AudioThumbHelper
import com.google.android.exoplayer2.SimpleExoPlayer

class AudioPostAdapter(var context: Context,
                       val player: SimpleExoPlayer,
                       val downloadAudioClickListener: ((String) -> Unit)? = null,
                       private val audioCounterListener: ((Int) -> Unit)? = null,
                       private val audioPlayerListener: (() -> Unit)? = null) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list: ArrayList<Media> = arrayListOf()
    var onHideAttachButton: ((Boolean) -> Unit)? = null
    var selectedItemPos = -1
    var lastItemSelectedPos = -1
    var currentMediaId = -1L
    var shouldMarkInActive = false

    companion object {
        @JvmStatic
        var lastAudioId = -1L
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AudioPostViewHolder(
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.adapter_audio_post,
                        parent,
                        false
                ), player)
    }

    fun setData(items: ArrayList<Media>) {
        Log.d("listudsdsdf", "lastAudioId $lastAudioId")
       // if (lastAudioId == -1L) {
            list = arrayListOf()
            list = items
            notifyDataSetChanged()
            Log.d("listudsdsdf", "list update")
       /* } else {
            list.indexOfFirst { it.id == lastAudioId }.let { index ->
                if (index != -1) {
                    items.firstOrNull { it.id == lastAudioId }?.let { list[index].views = it.views }
                   //notifyItemChanged(index)
                    //notifyDataSetChanged()
                    Log.d("listudsdsdf", "item inner")
                }
            }
            Log.d("listudsdsdf", "item outer")
        }*/
    }

    override fun getItemCount(): Int {
        onHideAttachButton?.invoke(list.size >= 10)
        return list.size
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, position: Int) {
        if (parent is AudioPostViewHolder) {
            val media = list[position]
            if (position == selectedItemPos) {
                parent.selectedBg(media)
            } else {
                parent.defaultBg(media, shouldMarkInActive)
                if (shouldMarkInActive) shouldMarkInActive = false
            }
            parent.bind(media)
        }
    }

    inner class AudioPostViewHolder(val layout: AdapterAudioPostBinding, val player: SimpleExoPlayer) :
            RecyclerView.ViewHolder(layout.root) {

        var mMedia: Media? = null

        var audioThumbHelper: AudioThumbHelper = AudioThumbHelper(player, layout.trimAudioView, context)

        fun bind(media: Media) {
            setUpView(media)
            setClickListener(media.origin.toString())
        }

        private fun setUpView(media: Media) {
            mMedia = media
            val views = if (media.views > 999) "999+" else media.views.toString()
            layout.textCount.text = views
            layout.trimAudioView.setAudioDuration(media.length)

            layout.trimAudioView.onTimeChangedListener = { audioThumbHelper.onAudioTimeThumbChanged(it, media) }

            audioThumbHelper.audioDurationLiveData.observe((context as MainActivity), androidx.lifecycle.Observer {
                media.length = it
                layout.trimAudioView.setAudioDuration(it)
            })
        }

        private fun setClickListener(audioPath: String) {

            layout.btnPlayPause.setOnClickListener {
                selectedItemPos = adapterPosition
                when {
                    lastItemSelectedPos == -1 -> {
                        lastItemSelectedPos = selectedItemPos
                        notifyItemChanged(selectedItemPos)
                    }
                    lastItemSelectedPos == selectedItemPos -> {
                        notifyItemChanged(selectedItemPos)
                    }
                    lastItemSelectedPos != selectedItemPos -> {
                        if (lastItemSelectedPos < selectedItemPos) {
                            notifyItemChanged(lastItemSelectedPos)
                        } else {
                            shouldMarkInActive = true
                            notifyItemChanged(lastItemSelectedPos)
                        }
                        notifyItemChanged(selectedItemPos)

                        lastItemSelectedPos = selectedItemPos
                    }
                }
            }

            layout.btnDownloadAudioAttachment.setOnClickListener {
                if (adapterPosition == lastItemSelectedPos) {
                    player.playWhenReady = false
                }
                downloadAudioClickListener?.invoke(audioPath)

            }
        }

        fun defaultBg(media: Media, play: Boolean) {
            layout.btnPlayPause.setImageResource(R.drawable.ic_play_audio)
            player.playWhenReady = play
            audioThumbHelper.resetProgress()
            Log.d("defrgrg", "def ${media.id}")
        }

        fun selectedBg(media: Media) {
            if (player.isPlaying) {
                if (media.id == currentMediaId) {
                    layout.btnPlayPause.setImageResource(R.drawable.ic_play_audio)
                    player.playWhenReady = false
                    audioThumbHelper.resetProgress()
                    audioThumbHelper.init(media) {}
                    audioCounterListener?.invoke(media.id.toInt())
                    Log.d("defrgrg", "pause ${media.id}")
                    lastAudioId = media.id
                } else {
                    layout.btnPlayPause.setImageResource(R.drawable.ic_pause_audio)
                    player.playWhenReady = true
                    audioThumbHelper.init(media) {}
                    audioPlayerListener?.invoke()
                    //audioCounterListener?.invoke(currentMediaId.toInt())
                    //lastAudioId = media.id
                    currentMediaId = media.id
                    Log.d("defrgrg", "played ${media.id}")
                }
            } else {
                layout.btnPlayPause.setImageResource(R.drawable.ic_pause_audio)
                player.playWhenReady = true

                audioThumbHelper.init(media) {}
                audioPlayerListener?.invoke()
                currentMediaId = media.id
                Log.d("defrgrg", "not playing play ${media.id}")
            }

        }
    }

}
