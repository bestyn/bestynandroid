package com.gbksoft.neighbourhood.ui.components

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.download.AppDownloader

class DownloadViewModel(private val context: Context) : BaseViewModel() {
    private val appDownloader by lazy { AppDownloader(context) }

    private val _downloading = MutableLiveData<Boolean>()
    val downloading = _downloading as LiveData<Boolean>

    private val _downloadComplete = SingleLiveEvent<Boolean>()
    val downloadComplete = _downloadComplete as LiveData<Boolean>

    private val downloadSet = mutableSetOf<Long>()

    init {
        _downloading.value = false
    }

    fun download(uri: Uri) {
        val downloadId = appDownloader.download(uri.toString())
        _downloading.value = true
        downloadSet.add(downloadId)
        appDownloader.subscribeOnDownloadComplete(downloadId) {
            if (downloadSet.contains(downloadId)) {
                downloadSet.remove(downloadId)
                _downloading.value = false
                _downloadComplete.value = true
            }
        }
    }
}