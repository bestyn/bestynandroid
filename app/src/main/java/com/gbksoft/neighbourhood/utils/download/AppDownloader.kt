package com.gbksoft.neighbourhood.utils.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import com.gbksoft.neighbourhood.utils.FileUtils

class AppDownloader(context: Context) {
    private val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    //return downloadId
    fun download(url: String, fileName: String? = null): Long {
        val suitableFileName = prepareFileName(url, fileName)
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, suitableFileName)
        return manager.enqueue(request)
    }

    private fun prepareFileName(url: String, fileName: String? = null): String {
        return if (fileName != null) {
            FileUtils.removeInvalidCharacters(fileName)
        } else {
            val lastSlash = url.lastIndexOf("/")
            FileUtils.removeInvalidCharacters(url.substring(lastSlash))
        }
    }

    fun subscribeOnDownloadComplete(downloadId: Long, downloadCompleteCallback: () -> Unit) {
        return observeDownloadProgress(downloadId) { total, current ->
            if (total in 1..current) {
                downloadCompleteCallback.invoke()
            }
        }
    }

    fun observeDownloadProgress(downloadId: Long, progressCallback: (total: Int, current: Int) -> Unit) {
        val handler = Handler { msg ->
            progressCallback.invoke(msg.arg2, msg.arg1)
            true
        }
        Thread(Runnable {
            try {
                var downloading = true
                while (downloading) {
                    val query = DownloadManager.Query()
                    query.setFilterById(downloadId)
                    val cursor = manager.query(query)
                    cursor.moveToFirst()
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                    }
                    //Post message to UI Thread
                    val msg = handler.obtainMessage()
                    msg.arg1 = bytesDownloaded
                    msg.arg2 = bytesTotal
                    handler.sendMessage(msg)
                    cursor.close()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                val msg = handler.obtainMessage()
                msg.arg1 = 0
                msg.arg2 = 1
                handler.sendMessage(msg)
            }
        }
        ).start()
    }
}