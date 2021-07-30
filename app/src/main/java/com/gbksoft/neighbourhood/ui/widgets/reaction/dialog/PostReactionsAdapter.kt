package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterReactionListBinding
import com.gbksoft.neighbourhood.model.reaction.PostReaction

class PostReactionsAdapter : RecyclerView.Adapter<PostReactionsAdapter.PostReactionViewHolder>() {
    var onProfileAvatarClickListener: ((PostReaction) -> Unit)? = null
    var onChatClickListener: ((PostReaction) -> Unit)? = null

    val reactionList = mutableListOf<PostReaction>()

    fun setData(reactions: List<PostReaction>) {
        val result = DiffUtil.calculateDiff(ReactionsDiffUtil(reactionList, reactions))
        reactionList.clear()
        reactionList.addAll(reactions)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostReactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout: AdapterReactionListBinding = DataBindingUtil.inflate(inflater,
            R.layout.adapter_reaction_list, parent, false)
        return PostReactionViewHolder(layout)
    }

    override fun getItemCount(): Int = reactionList.size

    override fun onBindViewHolder(holder: PostReactionViewHolder, position: Int) {
        holder.setPostReaction(reactionList[position])
    }

    inner class PostReactionViewHolder(val layout: AdapterReactionListBinding) : RecyclerView.ViewHolder(layout.root) {
        private lateinit var postReaction: PostReaction

        init {
            layout.ivAuthorAvatar.setOnClickListener { onProfileAvatarClickListener?.invoke(postReaction) }
            layout.tvAuthorName.setOnClickListener { onProfileAvatarClickListener?.invoke(postReaction) }
            layout.ivChat.setOnClickListener { onChatClickListener?.invoke(postReaction) }
        }

        fun setPostReaction(postReaction: PostReaction) {
            this.postReaction = postReaction
            setup()
        }

        private fun setup() {
            layout.reaction = postReaction
            layout.ivReaction.setImageResource(postReaction.reaction.icon)
        }
    }
}

