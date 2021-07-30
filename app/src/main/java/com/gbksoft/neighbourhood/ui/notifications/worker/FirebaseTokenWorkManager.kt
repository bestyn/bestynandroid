package com.gbksoft.neighbourhood.ui.notifications.worker

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest

object FirebaseTokenWorkManager {

    fun checkToken(context: Context) {
        val workRequest: WorkRequest =
            OneTimeWorkRequestBuilder<CheckTokenWorker>()
                .build()

        WorkManager
            .getInstance(context)
            .enqueue(workRequest)
    }

    fun deleteToken(context: Context) {
        val workRequest: WorkRequest =
            OneTimeWorkRequestBuilder<DeleteTokenWorker>()
                .build()

        WorkManager
            .getInstance(context)
            .enqueue(workRequest)
    }
}