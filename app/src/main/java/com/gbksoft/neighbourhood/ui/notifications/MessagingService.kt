package com.gbksoft.neighbourhood.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.activities.notification.NotificationActivity
import com.gbksoft.neighbourhood.ui.notifications.worker.FirebaseTokenWorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

const val FOLLOWERS_CHANNEL_ID = "followers"
const val MENTIONS_CHANNEL_ID = "mentions"
const val NEW_MESSAGE_CHANNEL_ID = "new_message"


class MessagingService : FirebaseMessagingService() {
    private val tokenStorage by inject<FirebaseMessagingTokenStorage>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(FOLLOWERS_CHANNEL_ID, "New followers", "Channel for notifications about new followers.")
        createNotificationChannel(MENTIONS_CHANNEL_ID, "Mentions", "Channel for notifications about new profile mentions.")
        createNotificationChannel(NEW_MESSAGE_CHANNEL_ID, "New chat message", "Channel for notifications about new private chat messages")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokenStorage.setFirebaseMessagingToken(token)
        FirebaseTokenWorkManager.checkToken(this)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.tag("MsgTag").d("onMessageReceived")
        val action = remoteMessage.notification?.clickAction
        val bundle = Bundle()
        for ((key, value) in remoteMessage.data) {
            bundle.putString(key, value)
        }
        val intent = Intent(this, NotificationActivity::class.java).apply {
            setAction(action)
            putExtras(bundle)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(this, getChannelId(action))
                .setContentTitle(remoteMessage.notification?.title)
                .setContentText(remoteMessage.notification?.body)
                .setSmallIcon(R.drawable.ic_firebase_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }

    private fun createNotificationChannel(channelId: String, name: String, descriptionText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getChannelId(action: String?): String {
        return when (action) {
            NotificationActivity.ACTION_OPEN_FOLLOWERS_LIST -> FOLLOWERS_CHANNEL_ID
            NotificationActivity.ACTION_OPEN_POST_DETAILS,
            NotificationActivity.ACTION_OPEN_POST_DETAILS_COMMENTS -> MENTIONS_CHANNEL_ID
            NotificationActivity.ACTION_OPEN_CONVERSATION -> NEW_MESSAGE_CHANNEL_ID
            else -> ""
        }
    }
}