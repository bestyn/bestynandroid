package com.gbksoft.neighbourhood.ui.notifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.ui.notifications.FirebaseMessagingTokenStorage
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
class CheckTokenWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context.applicationContext, workerParams), KoinComponent {

    private val tokenStorage by inject<FirebaseMessagingTokenStorage>()
    private val userRepository by inject<UserRepository>()
    private var disposable: Disposable? = null

    override fun doWork(): Result {
        Timber.tag("MsgTag").d("CheckTokenWorker doWork")

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                uploadToken(it.result)
            }
        }
        return Result.success()
    }

    private fun uploadToken(deviceToken: String) {
        Timber.tag("MsgTag").d("uploadToken $deviceToken")
        disposable = userRepository
                .setFirebaseMessagingToken(deviceToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.tag("MsgTag").d("token successfully updated $deviceToken")
                    tokenStorage.setUploadedFirebaseMessagingToken(deviceToken)
                }, {
                    Timber.tag("MsgTag").d("token updating error ${it.message}")
                    it.printStackTrace()
                })
    }
}