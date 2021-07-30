package com.gbksoft.neighbourhood.ui.activities.notification

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gbksoft.neighbourhood.data.shared_prefs.SharedStorage
import com.gbksoft.neighbourhood.domain.utils.isNotNullOrEmpty
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.ui.activities.main.MainActivity
import org.koin.android.ext.android.inject

class NotificationActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CHAT_ROOM_DATA = "chat_room_data"
        const val EXTRA_POST_ID = "postId"
        const val EXTRA_PROFILE_ID = "profileId"
        const val EXTRA_TO_FOLLOWERS = "extra_to_followers"

        const val ACTION_OPEN_CONVERSATION = "com.bestyn.app.NewMessage"
        const val ACTION_OPEN_POST_DETAILS_COMMENTS = "com.bestyn.app.NewMentionPostMessage"
        const val ACTION_OPEN_POST_DETAILS = "com.bestyn.app.NewMentionPost"
        const val ACTION_OPEN_FOLLOWERS_LIST = "com.bestyn.app.NewFollower"

        private const val DATA_NOTIFICATION_PROFILE_ID = "profileId"
        private const val DATA_OPPONENT_ID = "senderProfileId"
        private const val DATA_OPPONENT_NAME = "senderFullName"
        private const val DATA_OPPONENT_AVATAR = "senderAvatar"
        private const val DATA_OPPONENT_PROFILE_TYPE = "senderType"
        private const val DATA_POST_ID = "postId"
    }

    private val sharedStorage by inject<SharedStorage>()
    private var currentProfileId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentProfileId = sharedStorage.getCurrentProfile()?.id

        if (sharedStorage.isTokenAlive()) {
            handleIntent(intent)
        }
        finish()
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_OPEN_CONVERSATION -> onOpenConversationAction(intent.extras)
            ACTION_OPEN_FOLLOWERS_LIST -> onOpenFollowersListAction(intent.extras)
            ACTION_OPEN_POST_DETAILS -> onOpenPostDetailsAction(intent.extras)
            ACTION_OPEN_POST_DETAILS_COMMENTS -> onOpenPostDetailsAction(intent.extras)
        }
    }

    private fun onOpenConversationAction(extras: Bundle?) {
        if (extras == null) return
        val notificationProfileId = extras.getString(DATA_NOTIFICATION_PROFILE_ID)
                ?.toLongOrNull()
                ?: return

        val opponentId = extras.getString(DATA_OPPONENT_ID)?.toLongOrNull() ?: return
        val opponentName = extras.getString(DATA_OPPONENT_NAME) ?: return
        val opponentAvatar = extras.getString(DATA_OPPONENT_AVATAR)
        val isBusinessOpponent = extras.getString(DATA_OPPONENT_PROFILE_TYPE) == "business"

        val chatRoomData = ChatRoomData(
                null,
                opponentId,
                opponentName,
                if (opponentAvatar.isNotNullOrEmpty()) opponentAvatar else null,
                isBusinessOpponent
        )
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(EXTRA_CHAT_ROOM_DATA, chatRoomData)
        intent.putExtra(EXTRA_PROFILE_ID, notificationProfileId)
        startActivity(intent)
    }

    private fun onOpenPostDetailsAction(extras: Bundle?) {
        if (extras == null) return

        val notificationProfileId = extras.getString(DATA_NOTIFICATION_PROFILE_ID)
                ?.toLongOrNull()
                ?: return
        val postId = extras.getString(DATA_POST_ID)?.toLong() ?: return

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(EXTRA_PROFILE_ID, notificationProfileId)
        intent.putExtra(EXTRA_POST_ID, postId)
        startActivity(intent)
    }

    private fun onOpenFollowersListAction(extras: Bundle?) {
        if (extras == null) return

        val notificationProfileId = extras.getString(DATA_NOTIFICATION_PROFILE_ID)
                ?.toLongOrNull()
                ?: return

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(EXTRA_TO_FOLLOWERS, true)
        intent.putExtra(EXTRA_PROFILE_ID, notificationProfileId)
        startActivity(intent)
    }

    private fun String?.toBooleanOrNull(): Boolean? {
        if (this == null) return null
        if (this.equals("true", ignoreCase = true)) return true
        if (this.equals("false", ignoreCase = true)) return false
        return null
    }
}