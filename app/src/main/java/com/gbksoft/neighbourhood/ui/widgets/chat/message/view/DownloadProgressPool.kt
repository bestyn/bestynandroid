package com.gbksoft.neighbourhood.ui.widgets.chat.message.view

class DownloadProgressPool {
    private val progressMap = mutableMapOf<Long, Progress>()

    /**
     * return is progress changed
     */
    fun setProgress(messageId: Long, total: Int, current: Int): Boolean {
        progressMap[messageId]?.let { progress ->
            val isChanged = progress.current != current
            progress.total = total
            progress.current = current
            return isChanged
        } ?: run {
            progressMap[messageId] = Progress(total, current)
            return true
        }
    }

    fun getProgress(messageId: Long): Progress? {
        return progressMap[messageId]
    }
}