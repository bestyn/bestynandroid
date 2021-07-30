package com.gbksoft.neighbourhood.di.module

import com.gbksoft.neighbourhood.ui.fragments.base.utils.BackgroundBitmapPool
import org.koin.dsl.module

val utilsModule = module {
    single { BackgroundBitmapPool(get()) }
}