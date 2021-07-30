package com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter

import android.content.Context
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.chat.Message
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import java.util.*

class ChatRoomContent(
        val context: Context,
        private val currentProfileId: Long
) {
    private val calendar = Calendar.getInstance(Locale.getDefault())
    private val dateMap = mutableMapOf<Int, String>()
    private val incomingMap = mutableMapOf<Int, Message>()
    private val outcomingMap = mutableMapOf<Int, Message>()
    private val allMessageMap = mutableMapOf<Message, Int>()
    private val messageRealPositionMap = mutableMapOf<Int, Int>()

    private var unreadMsgHighlighterIndex: Int? = null
    private var firstUnreadMessage: Message? = null

    private val todayTitle: String = context.getString(R.string.today_title)
    private val yesterdayTitle: String = context.getString(R.string.yesterday_title)

    var isReverseLayout = false
    var isUnreadMessageHighlighterEnabled = false

    fun clear() {
        dateMap.clear()
        incomingMap.clear()
        outcomingMap.clear()
        allMessageMap.clear()
        messageRealPositionMap.clear()
        unreadMsgHighlighterIndex = null
        firstUnreadMessage = null
    }

    fun setData(messages: List<Message>, firstUnreadMessageId: Long? = null) {
        clear()
        var prevDate = ""
        for (i in messages.indices) {
            val message = messages[i]
            val date = DateTimeUtils.formatChatDate(calendar, message.createdAt, todayTitle, yesterdayTitle)
            val isLastMessage = i == messages.lastIndex
            if (isReverseLayout) {
                addMessage(message, i)
                prevDate = addDate(prevDate, date, isLastMessage)
                checkUnreadMsgHighlighter(message, firstUnreadMessageId)
            } else {
                checkUnreadMsgHighlighter(message, firstUnreadMessageId)
                prevDate = addDate(prevDate, date, isLastMessage)
                addMessage(message, i)
            }
        }
    }

    //tested only for isReverseLayout=true
    private fun checkUnreadMsgHighlighter(message: Message, firstUnreadMessageId: Long?) {
        if (isUnreadMessageHighlighterEnabled.not()) return
        if (firstUnreadMessageId == null || firstUnreadMessage != null) return
        if (message.id == firstUnreadMessageId) {
            firstUnreadMessage = message
            unreadMsgHighlighterIndex = getItemCount()
        }
    }

    //return new prevDate
    private fun addDate(prevDate: String, currentDate: String, isLastMessage: Boolean): String {
        return if (isReverseLayout) {
            when {
                prevDate.isEmpty() -> currentDate
                currentDate != prevDate || isLastMessage -> {
                    dateMap[getItemCount()] = prevDate
                    currentDate
                }
                else -> prevDate
            }
        } else {
            return if (currentDate != prevDate) {
                dateMap[getItemCount()] = currentDate
                currentDate
            } else {
                prevDate
            }
        }
    }

    private fun addMessage(message: Message, messageIndex: Int) {
        val pos = getItemCount()
        if (message.author.id != currentProfileId) {
            incomingMap[pos] = message
        } else {
            outcomingMap[pos] = message
        }
        allMessageMap[message] = pos
        messageRealPositionMap[pos] = messageIndex
    }

    fun getItemCount(): Int {
        return dateMap.size +
                incomingMap.size +
                outcomingMap.size +
                if (unreadMsgHighlighterIndex != null) 1 else 0
    }

    fun getItemViewType(position: Int): Int {
        return when {
            dateMap.containsKey(position) -> TYPE_DATE
            incomingMap.containsKey(position) -> TYPE_INCOMING_MESSAGE
            outcomingMap.containsKey(position) -> TYPE_OUTCOMING_MESSAGE
            unreadMsgHighlighterIndex == position -> TYPE_UNREAD_MESSAGE_HIGHLIGHTER
            else -> throw IllegalStateException("Undefined view type for position $position")
        }
    }

    fun getDate(position: Int): String {
        return dateMap[position]!!
    }

    fun getMessageRealPosition(position: Int): Int {
        return messageRealPositionMap[position]!!
    }

    fun getIncomingMessage(position: Int): Message {
        return incomingMap[position]!!
    }

    fun getOutcomingMessage(position: Int): Message {
        return outcomingMap[position]!!
    }

    fun getMessagePosition(message: Message): Int? {
        return allMessageMap[message]
    }

    fun getFirstUnreadMessagePosition(): Int? {
        return firstUnreadMessage?.let { getMessagePosition(it) }
    }

    fun getMessagesBetween(firstPosition: Int, lastPosition: Int): MutableList<Message> {
        val messages = mutableListOf<Message>()
        for (i in firstPosition..lastPosition) {
            if (i >= 0) {
                when (getItemViewType(i)) {
                    TYPE_INCOMING_MESSAGE -> {
                        messages.add(getIncomingMessage(i))
                    }
                    TYPE_OUTCOMING_MESSAGE -> {
                        messages.add(getOutcomingMessage(i))
                    }
                }
            }
        }
        return messages
    }

    companion object {
        const val TYPE_DATE = 1
        const val TYPE_INCOMING_MESSAGE = 2
        const val TYPE_OUTCOMING_MESSAGE = 3
        const val TYPE_UNREAD_MESSAGE_HIGHLIGHTER = 4
    }
}