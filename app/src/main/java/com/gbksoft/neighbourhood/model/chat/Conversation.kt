package com.gbksoft.neighbourhood.model.chat

data class Conversation(
    val id: Long,
    val opponent: Opponent,
    var unreadMessages: Int,
    val firstUnreadMessageId: Long?,
    var lastMessage: LastMessage,
    var isOpponentOnline: Boolean?
) {
    data class LastMessage(
        var isVoice: Boolean,
        var text: String,
        var attachmentTitle: String?,
        var time: Long,
        var isMine: Boolean,
        var isRead: Boolean
    )

    data class Opponent(
        val id: Long,
        val name: String,
        val avatar: String?,
        val isBusiness: Boolean
    )
}