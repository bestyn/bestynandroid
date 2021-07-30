package com.gbksoft.neighbourhood.ui.notifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gbksoft.neighbourhood.ui.notifications.FirebaseMessagingTokenStorage
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
class DeleteTokenWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context.applicationContext, workerParams), KoinComponent {

    private val tokenStorage by inject<FirebaseMessagingTokenStorage>()

    override fun doWork(): Result {
        Timber.tag("MsgTag").d("DeleteTokenWorker doWork")
        val deleteTask = FirebaseMessaging.getInstance().deleteToken()
        while (!deleteTask.isComplete) Thread.sleep(50)
        val createTask = FirebaseMessaging.getInstance().token
        while (!createTask.isComplete) Thread.sleep(50)
        if (createTask.isSuccessful) {
            createTask.result?.let { tokenStorage.setFirebaseMessagingToken(it) }
        }

        return Result.success()
    }

}