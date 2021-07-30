package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog

import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class ReactionsDiffUtil(oldData: List<PostReaction>, newData: List<PostReaction>)
    : SimpleDiffUtilCallback<PostReaction>(oldData, newData) {

    override fun areItemsTheSame(oldItem: PostReaction, newItem: PostReaction): Boolean {
        return oldItem.postId == newItem.postId && oldItem.profile.id == newItem.profile.id
    }

    override fun areContentsTheSame(oldItem: PostReaction, newItem: PostReaction): Boolean {
        return oldItem == newItem
    }
}