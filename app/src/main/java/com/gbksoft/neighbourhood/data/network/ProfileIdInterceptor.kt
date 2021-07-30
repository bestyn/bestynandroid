package com.gbksoft.neighbourhood.data.network

import okhttp3.Interceptor
import okhttp3.Response

class ProfileIdInterceptor(private val headersProvider: HeadersProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val profileId = headersProvider.getCurrentProfileId()

        val original = chain.request()
        val requestBuilder = original.newBuilder()

        if (isNotEmpty(profileId)) {
            requestBuilder.addHeader("ProfileId", "$profileId")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    private fun isNotEmpty(profileId: Long?): Boolean = profileId != null && profileId > 0

}