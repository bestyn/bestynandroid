package com.gbksoft.neighbourhood.di

import android.app.Application
import com.gbksoft.neighbourhood.di.module.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

object Koin {
    private val moduleList = listOf(
        activityModule,
        appModule,
        repositoryModule,
        roomModule,
        viewModelModule,
        utilsModule,
        cacheStoryModule
    )

    fun setup(application: Application) {
        startKoin {
            androidLogger()
            androidContext(application)
            modules(moduleList)
        }
    }
}