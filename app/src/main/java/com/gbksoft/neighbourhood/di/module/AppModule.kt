package com.gbksoft.neighbourhood.di.module

import com.gbksoft.neighbourhood.app.AdvertisingDeviceId
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.domain.DeviceIdProvider
import com.gbksoft.neighbourhood.ui.notifications.FirebaseMessagingTokenStorage
import com.google.gson.Gson
import org.koin.dsl.module

val appModule = module {
    single { Gson() }
    single<DeviceIdProvider> {
        AdvertisingDeviceId(get())
    }
    single<FirebaseMessagingTokenStorage> {
        NApplication.sharedStorage
    }
    single {
        NApplication.sharedStorage
    }
}