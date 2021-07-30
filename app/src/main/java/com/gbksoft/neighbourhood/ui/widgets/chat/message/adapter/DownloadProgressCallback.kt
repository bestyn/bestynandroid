package com.gbksoft.neighbourhood.ui.widgets.chat.message.adapter

interface DownloadProgressCallback {
    fun onProgressChanged(total: Int, current: Int)
}