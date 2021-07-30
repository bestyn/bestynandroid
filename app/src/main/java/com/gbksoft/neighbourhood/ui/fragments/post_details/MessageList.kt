package com.gbksoft.neighbourhood.ui.fragments.post_details

import com.gbksoft.neighbourhood.model.chat.Message
import timber.log.Timber


class MessageList {
    private val map = mutableMapOf<Long, Int>()
    private val list = mutableListOf<Message>()


    fun size(): Int = list.size
    fun isEmpty() = size() == 0

    //Need a more efficient way to sort messages by date and remove/update by id
    fun addSingle(message: Message) {
        add(message.id, message)
        sort()
    }

    //Need a more efficient way to sort messages by date and remove/update by id
    fun add(messages: List<Message>) {
        for (message in messages) {
            add(message.id, message)
        }
        sort()
    }

    private fun add(id: Long, message: Message) {
        if (map.contains(id)) {
            list[map[id]!!] = message
        } else {
            list.add(message)
        }
    }

    private fun MutableMap<Long, Int>.putAll(list: List<Message>) {
        for (i in list.indices) this[list[i].id] = i
    }

    fun delete(message: Message): Boolean {
        return this.delete(message.id)
    }

    fun delete(messageId: Long): Boolean {
        map[messageId]?.let { pos ->
            list.removeAt(pos).let {
                Timber.tag("UpdateTag").d("deleteMessage, removeAt id: ${it.id}")
            }
            map.remove(messageId)
            sort()
            return true
        }
        return false
    }

    fun clear() {
        map.clear()
        list.clear()
    }

    private fun sort() {
        list.sortByDescending {
            it.createdAt
        }
        map.clear()
        map.putAll(list)
    }

    fun update(message: Message) {
        update(message.id, message)
    }

    fun update(messageId: Long, message: Message) {
        map[messageId]?.let { pos ->
            list[pos] = message
        }
    }

    fun last(): Message = list.last()

    fun getMessagePosition(message: Message): Int? {
        val index = list.indexOf(message)
        return if (index >= 0) index else null
    }

    fun asList(): List<Message> = list
}