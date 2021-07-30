package com.gbksoft.neighbourhood.data.centrifuge

import com.google.gson.Gson
import io.github.centrifugal.centrifuge.Subscription
import timber.log.Timber

class CentrifugeLogger(private val isEnabled: Boolean) {
    private val gson by lazy { Gson() }

    fun log(eventName: String, vararg data: Any?) {
        log(eventName, null, data)
    }

    fun log(eventName: String, sub: Subscription?, vararg data: Any?) {
        if (!isEnabled) return

        val text = StringBuilder()
        sub?.let {
            text.append("channel: '${it.channel}'   ")
        }

        text.append(data.joinToString(transform = {
            it.toJson()
        }))
        Timber.tag("Centrifuge").d("$eventName -->  $text")
    }

    private fun Any?.toJson(): String {
        var json = gson.toJson(this)
        if (json == "null") {
            json = this.toString()
        }
        return json
        /*return this?.let {
            val filedMap = mutableMapOf<String, Any>()
            for (df in it.javaClass.declaredFields) {
                df.isAccessible = true
                df.get(it)?.let { fieldValue ->
                    filedMap.put(df.name, fieldValue)
                }
            }
            gson.toJson(filedMap)
        } ?: run {
            gson.toJson(this)
        }*/
    }
}