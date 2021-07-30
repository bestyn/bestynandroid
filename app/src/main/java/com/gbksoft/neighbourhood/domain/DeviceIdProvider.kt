package com.gbksoft.neighbourhood.domain

interface DeviceIdProvider {
    suspend fun getDeviceId(): String
}