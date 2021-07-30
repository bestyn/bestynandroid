package com.gbksoft.neighbourhood.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_messages_heard_statuses")
class AudioMessageHeardStatusEntity(
    @PrimaryKey
    @ColumnInfo(name = "msg_id")
    val id: Long,

    @ColumnInfo(name = "status")
    val status: Int //1 - heard, 0 - unheard
)