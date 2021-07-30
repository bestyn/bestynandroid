package com.gbksoft.neighbourhood.model.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatRoomData(
    val conversationId: Long?,
    val opponentId: Long,
    val opponentName: String,
    val opponentAvatar: String?,
    val isBusinessOpponent: Boolean
) : Parcelable