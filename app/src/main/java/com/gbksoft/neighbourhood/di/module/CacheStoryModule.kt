package com.gbksoft.neighbourhood.di.module

import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val exoPlayerCacheSize: Long = 100 * 1024 * 1024

val cacheStoryModule = module {

    single { LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize) }
    single { ExoDatabaseProvider(androidContext()) }
    single { SimpleCache(androidApplication().cacheDir, get<LeastRecentlyUsedCacheEvictor>(), get<ExoDatabaseProvider>()) }
}