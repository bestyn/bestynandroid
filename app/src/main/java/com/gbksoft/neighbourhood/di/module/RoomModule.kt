package com.gbksoft.neighbourhood.di.module

import androidx.room.Room
import com.gbksoft.neighbourhood.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "app_database"
        ).build()
    }

    single { get<AppDatabase>().audioMessageHeardStatusDao() }

}