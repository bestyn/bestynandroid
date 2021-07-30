package com.gbksoft.neighbourhood.app

import android.content.Context
import com.gbksoft.neighbourhood.domain.DeviceIdProvider
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class AdvertisingDeviceId(
    private val context: Context
) : DeviceIdProvider {

    override suspend fun getDeviceId(): String {
        return try {
            withContext(Dispatchers.IO) {
                val info = AdvertisingIdClient.getAdvertisingIdInfo(context.applicationContext)
                info.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UUID.randomUUID().toString()
        }
    }

}