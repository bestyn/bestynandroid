package com.gbksoft.neighbourhood.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gbksoft.neighbourhood.data.local.dao.AudioMessageHeardStatusDao
import com.gbksoft.neighbourhood.data.local.entity.AudioMessageHeardStatusEntity


@Database(
    entities = [
        AudioMessageHeardStatusEntity::class
    ], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioMessageHeardStatusDao(): AudioMessageHeardStatusDao
}