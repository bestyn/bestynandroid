package com.gbksoft.neighbourhood.model.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConversationIds(
    val conversationId: Long,
    val opponentId: Long
) : Parcelable