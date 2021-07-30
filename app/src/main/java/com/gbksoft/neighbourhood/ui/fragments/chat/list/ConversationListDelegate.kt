package com.gbksoft.neighbourhood.ui.fragments.chat.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.model.chat.Conversation
import com.gbksoft.neighbourhood.model.chat.MessageEvent

class ConversationListDelegate(
    private val onConversationNeedUpdateListener: (Conversation) -> Unit,
    private val onNewConversationNeedLoadListener: () -> Unit
) {

    private val uniqueIds = mutableSetOf<Long>()
    private val helperList = mutableListOf<Conversation>()
    private val conversationList = mutableListOf<Conversation>()
    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations = _conversations as LiveData<List<Conversation>>
    var currentProfileId: Long? = null

    fun size() = conversationList.size

    fun setConversations(conversations: List<Conversation>) {
        conversationList.clear()
        conversationList.addAll(conversations)
        _conversations.value = conversationList
    }

    fun addConversations(conversations: List<Conversation>) {
        conversationList.addAll(conversations)
        _conversations.value = onConversationAdded(conversationList)
    }

    fun removeConversation(conversation: Conversation) {
        val pos = conversationList.indexOfFirst { it.id == conversation.id }
        if (pos != -1) {
            conversationList.removeAt(pos)
            _conversations.value = conversationList
        }
    }

    fun handleMessageEvent(event: MessageEvent) {
        val myId = currentProfileId ?: return
        val authorId = event.message.author.id
        val recipientId = event.message.recipient?.id ?: return
        val opponentId = if (authorId != myId) authorId else recipientId
        var containsConversation = false
        for (conversation in conversationList) {
            if (conversation.opponent.id == opponentId) {
                onConversationNeedUpdateListener.invoke(conversation)
                containsConversation = true
                break
            }
        }
        if (!containsConversation && event.eventType == MessageEvent.CREATE) {
            onNewConversationNeedLoadListener.invoke()
        }
    }

    fun onConversationUpdated(updated: Conversation) {
        var position = -1
        for (i in conversationList.indices) {
            val old = conversationList[i]
            if (old.id == updated.id) {
                position = i
                break
            }
        }
        if (position != -1) {
            conversationList[position] = updated
            _conversations.value = sort(conversationList)
        } else {
            addConversation(updated)
        }
    }

    fun addConversation(conversation: Conversation) {
        conversationList.add(0, conversation)
        _conversations.value = onConversationAdded(conversationList)
    }

    private fun onConversationAdded(list: MutableList<Conversation>): List<Conversation> {
        return deleteDuplicates(sort(list))
    }

    private fun sort(list: MutableList<Conversation>): MutableList<Conversation> {
        list.sortByDescending { it.lastMessage.time }
        return list
    }

    private fun deleteDuplicates(list: MutableList<Conversation>): MutableList<Conversation> {
        uniqueIds.clear()
        helperList.clear()
        helperList.addAll(list)
        list.clear()
        for (conversation in helperList) {
            if (uniqueIds.contains(conversation.id)) continue

            list.add(conversation)
            uniqueIds.add(conversation.id)
        }
        return list
    }
}