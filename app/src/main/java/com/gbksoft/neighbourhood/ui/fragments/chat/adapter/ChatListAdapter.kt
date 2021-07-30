package com.gbksoft.neighbourhood.ui.fragments.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterChatListBinding
import com.gbksoft.neighbourhood.model.chat.Conversation
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import java.util.*


class ChatListAdapter : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    var onConversationClickListener: ((Conversation) -> Unit)? = null
    var onConversationDeleteClickListener: ((Conversation) -> Unit)? = null
    private val conversations: MutableList<Conversation> = mutableListOf()
    private val viewBinderHelper = ViewBinderHelper()

    fun setData(data: List<Conversation>) {
        val callback = ConversationDiffUtilCallback(conversations, data)
        val result = DiffUtil.calculateDiff(callback)
        conversations.clear()
        conversations.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = conversations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = DataBindingUtil.inflate<AdapterChatListBinding>(
            inflater, R.layout.adapter_chat_list, parent, false)
        return if (viewType == 0) {
            HeaderChatListViewHolder(layout)
        } else {
            ChatListViewHolder(layout)
        }
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.layout.swipeRevealLayout, conversations[position].id.toString())
        holder.setConversation(conversations[position])
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    open inner class ChatListViewHolder(val layout: AdapterChatListBinding) : RecyclerView.ViewHolder(layout.root) {
        private val calendar = Calendar.getInstance()
        private lateinit var conversation: Conversation

        init {
            layout.itemRoot.setOnClickListener { onConversationClickListener?.invoke(conversation) }
            layout.btnDelete.setOnClickListener {
                onConversationDeleteClickListener?.invoke(conversation)
                layout.swipeRevealLayout.close(true)
            }
        }

        fun setConversation(conversation: Conversation) {
            this.conversation = conversation
            setup()
        }

        protected open fun setup() {
            layout.tvOpponentName.text = conversation.opponent.name
            layout.ivAvatar.setFullName(conversation.opponent.name)
            layout.ivAvatar.setImage(conversation.opponent.avatar)
            layout.tvTime.text = DateTimeUtils.formatConversationTime(calendar, conversation.lastMessage.time)
            layout.ivAvatar.setBusiness(conversation.opponent.isBusiness)
            layout.ivOnlineIndicator.visibility = if (conversation.isOpponentOnline == true) View.VISIBLE else View.GONE
            if (conversation.lastMessage.isMine) {
                layout.ivMyLastMessageStatus.setImageResource(
                    if (conversation.lastMessage.isRead) R.drawable.ic_my_last_message_status_read
                    else R.drawable.ic_my_last_message_status_sent
                )
                layout.ivMyLastMessageStatus.visibility = View.VISIBLE
            } else {
                layout.ivMyLastMessageStatus.visibility = View.GONE
            }
            setUnreadMessages(conversation.unreadMessages)
            setLastMessage(conversation)
        }

        private fun setUnreadMessages(count: Int) {
            when {
                count > 99 -> {
                    layout.tvUnread.text = "99+"
                    layout.tvUnread.visibility = View.VISIBLE
                }
                count > 0 -> {
                    layout.tvUnread.text = count.toString()
                    layout.tvUnread.visibility = View.VISIBLE
                }
                else -> {
                    layout.tvUnread.visibility = View.GONE
                }
            }
        }

        private fun setLastMessage(conversation: Conversation) {
            when {
                conversation.lastMessage.isVoice -> {
                    layout.ivAttachment.visibility = View.GONE
                    layout.tvLastMessage.setText(R.string.chat_last_msg_audio_type)
                }
                conversation.lastMessage.attachmentTitle?.isNotEmpty() == true -> {
                    layout.ivAttachment.visibility = View.VISIBLE
                    layout.tvLastMessage.text = conversation.lastMessage.attachmentTitle
                }
                else -> {
                    layout.ivAttachment.visibility = View.GONE
                    layout.tvLastMessage.text = conversation.lastMessage.text
                }
            }
        }
    }

    inner class HeaderChatListViewHolder(layout: AdapterChatListBinding) : ChatListViewHolder(layout) {

        override fun setup() {
            super.setup()
            setHeaderBackgrounds()
        }

        private fun setHeaderBackgrounds() {
            layout.btnDelete.setBackgroundResource(R.drawable.bg_delete_chat)
            layout.itemRoot.setBackgroundResource(R.drawable.bg_chat_list)
        }
    }
}