package com.gbksoft.neighbourhood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gbksoft.neighbourhood.data.local.entity.AudioMessageHeardStatusEntity
import io.reactivex.Completable

@Dao
interface AudioMessageHeardStatusDao {
    @Query("SELECT * FROM audio_messages_heard_statuses WHERE msg_id IN (:msgIds)")
    fun getStatuses(msgIds: List<Long>): List<AudioMessageHeardStatusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setStatus(entity: AudioMessageHeardStatusEntity): Completable
}