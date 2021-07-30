package com.gbksoft.neighbourhood.data.network

import android.content.Context
import com.gbksoft.neighbourhood.data.connectivity.ConnectivityManager
import com.gbksoft.neighbourhood.data.network.api.*

object ApiFactory : ApiSettings() {
    @Volatile
    private var apiAnyBody: ApiAnyBody? = null

    val apiUser by lazy { getApi(ApiUser::class.java) }

    val apiEmail by lazy { getApi(ApiEmail::class.java) }

    val apiPost by lazy { getApi(ApiPost::class.java) }

    val apiMyPosts by lazy { getApi(ApiMyPosts::class.java) }

    val apiAllPosts by lazy { getApi(ApiAllPosts::class.java) }

    val apiNews by lazy { getApi(ApiNews::class.java) }

    val apiPostActions by lazy { getApi(ApiPostActions::class.java) }

    val apiMyNeighbors by lazy { getApi(ApiMyNeighbors::class.java) }

    val apiReports by lazy { getApi(ApiReports::class.java) }

    val apiPostMessage by lazy { getApi(ApiPostMessage::class.java) }

    val apiPostMessageAttachment by lazy { getApi(ApiPostMessageAttachment::class.java) }

    val apiCentrifuge by lazy { getApi(ApiCentrifuge::class.java) }

    val apiProfileMessage by lazy { getApi(ApiProfileMessage::class.java) }

    val apiProfileMessageAttachment by lazy { getApi(ApiProfileMessageAttachment::class.java) }

    val apiPayment by lazy { getApi(ApiPayment::class.java) }

    val apiGooglePlaces by lazy { getApi(ApiGooglePlaces::class.java) }

    val apiProfilesSearch by lazy { getApi(ApiProfilesSearch::class.java) }

    val apiAudioSearch by lazy { getApi(ApiAudioSearch::class.java) }

    val apiHashtag by lazy { getApi(ApiHashtag::class.java) }

    val apiPostStory by lazy { getApi(ApiPostStory::class.java) }

    val apiAudio by lazy { getApi(ApiAudio::class.java) }

    val apiFollowers by lazy { getApi(ApiFollowers::class.java) }

    fun init(context: Context?, connectivityManager: ConnectivityManager?, headersProvider: HeadersProvider?) {
        ApiSettings.init(context, connectivityManager, headersProvider)
    }

    private fun <I> getApi(apiClass: Class<I>): I {
        synchronized(ApiFactory::class.java) { return buildRetrofit().create(apiClass) }
    }

    val anyBody: ApiAnyBody
        get() = (if (apiAnyBody != null) apiAnyBody!! else getApi(ApiAnyBody::class.java).also { apiAnyBody = it })

}