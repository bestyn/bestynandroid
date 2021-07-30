package com.gbksoft.neighbourhood.data.network

interface HeadersProvider {
    fun getCurrentProfileId(): Long?
    fun getAppVersion(): String
}