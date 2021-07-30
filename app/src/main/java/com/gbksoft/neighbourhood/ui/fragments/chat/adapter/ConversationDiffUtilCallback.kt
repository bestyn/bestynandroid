package com.gbksoft.neighbourhood.ui.fragments.chat.adapter

import com.gbksoft.neighbourhood.model.chat.Conversation
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class ConversationDiffUtilCallback(
    oldData: List<Conversation>,
    newData: List<Conversation>
) : SimpleDiffUtilCallback<Conversation>(oldData, newData) {

    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem
    }

}