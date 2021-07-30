package com.gbksoft.neighbourhood.ui.fragments.chat.background

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterChatBackgroundBinding
import com.gbksoft.neighbourhood.model.chat.ChatBackground
import com.gbksoft.neighbourhood.ui.fragments.chat.background.component.ChatBackgroundManager
import kotlinx.coroutines.Job

class ChatBackgroundAdapter(
    private val backgrounds: List<ChatBackground> //@DrawableRes
) : RecyclerView.Adapter<ChatBackgroundAdapter.ChatBackgroundViewHolder>() {

    var defaultBackgroundPosition: Int = -1
    var selectedBackgroundPosition: Int = -1
    var onBackgroundClickListener: ((position: Int) -> Unit)? = null

    override fun getItemCount(): Int = backgrounds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatBackgroundViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = DataBindingUtil.inflate<AdapterChatBackgroundBinding>(
            inflater, R.layout.adapter_chat_background, parent, false
        )
        return ChatBackgroundViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ChatBackgroundViewHolder, position: Int) {
        holder.setBackground(backgrounds[position], position)
    }

    inner class ChatBackgroundViewHolder(val layout: AdapterChatBackgroundBinding)
        : RecyclerView.ViewHolder(layout.root) {

        var pos: Int = -1

        lateinit var background: ChatBackground
        private var loadingPreviewJob: Job? = null

        init {
            layout.root.setOnClickListener {
                onBackgroundClickListener?.invoke(pos)
            }
        }

        fun setBackground(background: ChatBackground, position: Int) {
            loadingPreviewJob?.cancel()
            this.pos = position
            this.background = background
            setup()
        }

        private fun setup() {
            ChatBackgroundManager
                .getInstance()
                .loadPreview(backgrounds[pos])
                .into(layout.image)

            layout.noBackgroundLabel.visibility = if (pos == defaultBackgroundPosition) {
                View.VISIBLE
            } else {
                View.GONE
            }
            layout.selected.visibility = if (pos == selectedBackgroundPosition) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}