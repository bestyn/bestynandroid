package com.gbksoft.neighbourhood.model.chat

sealed class Message(
    open val id: Long,
    open val author: MessageAuthor,
    open val recipient: MessageAuthor?,
    open val createdAt: Long,
    open val editedAt: Long
) {
    fun isEdited() = createdAt != editedAt

    var isRead: Boolean? = null

    data class Text(
        override val id: Long,
        override val author: MessageAuthor,
        override val recipient: MessageAuthor? = null,
        override val createdAt: Long,
        override val editedAt: Long,
        var text: String,
        val attachment: Attachment?
    ) : Message(id, author, recipient, createdAt, editedAt)

    data class Audio(
        override val id: Long,
        override val author: MessageAuthor,
        override val recipient: MessageAuthor? = null,
        override val createdAt: Long,
        override val editedAt: Long,
        val attachment: Attachment
    ) : Message(id, author, recipient, createdAt, editedAt) {
        var isHeard: Boolean = false
    }
}
