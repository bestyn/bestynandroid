package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterAudioAttachmentBinding
import com.gbksoft.neighbourhood.mappers.media.MediaMapper.getAudioDuration
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.activities.main.MainActivity
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.preview.AudioThumbHelper
import com.google.android.exoplayer2.SimpleExoPlayer

class AudioAttachmentAdapter(var context: Context, val player: SimpleExoPlayer) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list: ArrayList<Media.Audio> = arrayListOf()
    var onHideAttachButton: ((Boolean) -> Unit)? = null
    var onAudioRemoved: ((Media.Audio) -> Unit)? = null
    var selectedItemPos = -1
    var lastItemSelectedPos = -1
    var currentMediaId = -1L
    var shouldMarkInActive = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AudioAttachmentViewHolder(
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.adapter_audio_attachment,
                        parent,
                        false
                ), player)
    }

    fun setData(items: ArrayList<Media.Audio>) {
        list = arrayListOf()
        list = items
        notifyDataSetChanged()
    }

    fun addItem(uri: Media.Audio) {
        list.add(uri)
        notifyItemInserted(list.size - 1)
    }

    fun clear() = list.clear()

    override fun getItemCount(): Int {
        onHideAttachButton?.invoke(list.size >= 10)
        return list.size
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, position: Int) {
        if (parent is AudioAttachmentViewHolder) {
            val media = list[position]
            if (position == selectedItemPos) {
                parent.selectedBg(media)
            } else {
                parent.defaultBg(shouldMarkInActive)
                if (shouldMarkInActive) shouldMarkInActive = false
            }
            parent.bind(media)
        }
    }

    inner class AudioAttachmentViewHolder(val layout: AdapterAudioAttachmentBinding, val player: SimpleExoPlayer) :
            RecyclerView.ViewHolder(layout.root) {

        var anno: Media.Audio? = null

        var audioThumbHelper: AudioThumbHelper = AudioThumbHelper(player, layout.trimAudioView, context)

        fun bind(announcement: Media.Audio) {
            setUpView(announcement)
            setClickListener(announcement)
        }

        private fun setUpView(announcement: Media.Audio) {
            anno = announcement

            audioThumbHelper = AudioThumbHelper(player, layout.trimAudioView, context)

            if (announcement.length == 0){
                announcement.length = announcement.origin.getAudioDuration()
            }
            layout.trimAudioView.setAudioDuration(announcement.length)

            layout.trimAudioView.onTimeChangedListener = {
                if (announcement.origin.toString().startsWith("http")) {
                    audioThumbHelper.onAudioTimeThumbChanged(it, announcement)
                } else {
                    audioThumbHelper.onAudioTimeThumbChangedUri(it, announcement.origin)
                }
            }

            audioThumbHelper.audioDurationLiveData.observe((context as MainActivity), androidx.lifecycle.Observer {
                layout.trimAudioView.setAudioDuration(it)
            })

        }

        private fun setClickListener(audioPath: Media.Audio) {

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

            layout.btnRemoveAudioAttachment.setOnClickListener {
                if (adapterPosition == lastItemSelectedPos) {
                    player.playWhenReady = false
                }
                notifyItemRemoved(list.indexOf(audioPath))
                list.remove(audioPath)
                onAudioRemoved?.invoke(audioPath)
            }
        }

        fun defaultBg(play: Boolean) {
            layout.btnPlayPause.setImageResource(R.drawable.ic_play_audio)
            player.playWhenReady = play
            audioThumbHelper.resetProgress()
        }

        fun selectedBg(media: Media.Audio) {
            if (player.isPlaying) {
                if (media.id == currentMediaId) {
                    layout.btnPlayPause.setImageResource(R.drawable.ic_play_audio)
                    player.playWhenReady = false
                    audioThumbHelper.resetProgress()
                    audioThumbHelper.init(media){}
                } else {
                    layout.btnPlayPause.setImageResource(R.drawable.ic_pause_audio)
                    player.playWhenReady = true
                    audioThumbHelper.init(media){}
                    currentMediaId = media.id
                }
            } else {
                layout.btnPlayPause.setImageResource(R.drawable.ic_pause_audio)
                player.playWhenReady = true
                audioThumbHelper.init(media){}
                currentMediaId = media.id
            }

        }

    }

}
