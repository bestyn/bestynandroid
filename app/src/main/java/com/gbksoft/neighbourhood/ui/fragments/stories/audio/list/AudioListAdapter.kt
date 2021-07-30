package com.gbksoft.neighbourhood.ui.fragments.stories.audio

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterAudioListBinding
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackListener
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import timber.log.Timber

class AudioListAdapter(private val audioPlaybackManager: AudioPlaybackManager) : RecyclerView.Adapter<AudioListAdapter.AudioListViewHolder>() {

    private val audioList = mutableListOf<Audio>()
    private var selectedAudioPos = -1

    var onApplyButtonClickListener: ((Audio) -> Unit)? = null
    var onStarButtonClickListener: ((Audio) -> Unit)? = null
    var onReportOptionClickListener: ((Audio) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val layout = DataBindingUtil.inflate<AdapterAudioListBinding>(layoutInflater, R.layout.adapter_audio_list, parent, false)
        return AudioListViewHolder(layout)
    }

    override fun onBindViewHolder(holder: AudioListViewHolder, position: Int) {
        val audio = audioList[position]
        holder.bind(audio)
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    fun setData(data: List<Audio>) {
        audioList.clear()
        audioList.addAll(data)
        notifyDataSetChanged()
    }

    inner class AudioListViewHolder(val layout: AdapterAudioListBinding) : RecyclerView.ViewHolder(layout.root), AudioPlaybackListener,
        PopupMenu.OnMenuItemClickListener {

        private lateinit var audio: Audio
        private val popupMenu: PopupMenu = PopupMenu(layout.root.context, layout.btnMenu)

        init {
            popupMenu.inflate(R.menu.audio_menu)
            popupMenu.setOnMenuItemClickListener(this)
        }

        fun bind(audio: Audio) {
            this.audio = audio

            layout.tvDescription.text = audio.description
            layout.tvDuration.text = audio.duration
            setAddedBy()
            setIsFavorite()
            checkIsSelected()
            setClickListeners()
        }

        private fun setClickListeners() {
            layout.btnPlayPause.setOnClickListener { handlePlayOrStopAudioClick() }
            layout.btnApply.setOnClickListener { onApplyButtonClickListener?.invoke(audio) }
            layout.btnStar.setOnClickListener { onStarButtonClickListener?.invoke(audio) }
            layout.btnMenu.setOnClickListener { popupMenu.show() }
        }

        private fun setAddedBy() {
            if (audio.addedBy != null) {
                layout.tvAddedBy.visibility = View.VISIBLE
                layout.tvAddedBy.text = String.format(layout.root.context.getString(R.string.audio_list_added_by), audio.addedBy)
            } else {
                layout.tvAddedBy.visibility = View.GONE
            }
        }

        private fun setIsFavorite() {
            if (audio.isFavorite) {
                layout.btnStar.setImageResource(R.drawable.ic_audio_star_selected)
            } else {
                layout.btnStar.setImageResource(R.drawable.ic_audio_star_unselected)
            }
        }

        private fun checkIsSelected() {
            if (absoluteAdapterPosition == selectedAudioPos) {
                layout.clRoot.setBackgroundResource(R.drawable.bg_selected_audio)
                layout.btnApply.visibility = View.VISIBLE
            } else {
                layout.clRoot.setBackgroundResource(0)
                layout.btnApply.visibility = View.GONE
            }
        }

        private fun handlePlayOrStopAudioClick() {
            audioPlaybackManager.playOrPauseAudio(audio.url, audio.id)
            audioPlaybackManager.audioPlaybackListener = this
        }

        private fun setPlayingState() {
            layout.btnPlayPause.setImageResource(R.drawable.ic_pause_audio)
            layout.btnPlayPause.setBackgroundResource(R.drawable.bg_audio_pause_btn)
        }

        private fun setStoppedState() {
            layout.btnPlayPause.setImageResource(R.drawable.ic_play_audio)
            layout.btnPlayPause.setBackgroundResource(R.drawable.bg_audio_play_btn)
        }

        private fun updateSelectedAudio() {
            val prevSelectedAudioPos = selectedAudioPos
            selectedAudioPos = absoluteAdapterPosition
            notifyItemChanged(prevSelectedAudioPos)
            checkIsSelected()
        }

        override fun onAudioPlaybackStateChanged(id: Long, isPlaying: Boolean) {
            if (audio.id != id) {
                return
            }

            if (isPlaying) {
                setPlayingState()
                updateSelectedAudio()
            } else {
                setStoppedState()
            }

            Timber.tag("KEK").d("onAudioPlaybackStateChanged, id: $id, isPlaying: $isPlaying")
        }

        override fun onAudioPlaybackStopped(id: Long) {
            Timber.tag("KEK").d("onAudioPlaybackStopped, id: $id")
            setStoppedState()
        }

        override fun onAudioPlaybackProgressChanged(id: Long, totalMs: Long, currentMs: Long) {}

        override fun onMenuItemClick(item: MenuItem): Boolean {
            if (item.itemId == R.id.actionReport) {
                onReportOptionClickListener?.invoke(audio)
            }
            return true
        }
    }
}