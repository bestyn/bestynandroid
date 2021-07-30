package com.gbksoft.neighbourhood.data.network

import com.gbksoft.neighbourhood.data.network.api.UrlsWithoutToken
import com.gbksoft.neighbourhood.data.repositories.AccessTokenRepository
import okhttp3.Interceptor
import okhttp3.Response

class AccessTokenInterceptor(
    private val accessTokenRepository: AccessTokenRepository
) : Interceptor {
    private val tokenExpiryTimeBuffer = 3_600_000L //1 hour

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = accessTokenRepository.getAccessToken() ?: return proceed(chain)

        val expiredAt = accessTokenRepository.getAccessTokenExpiredTime()
        val isWithoutAccessToken = UrlsWithoutToken.isWithoutAccessToken(chain.request().url.toString())
        if (isWithoutAccessToken || expiredAt - System.currentTimeMillis() > tokenExpiryTimeBuffer) {
            return proceed(chain, token)
        }

        synchronized(AccessTokenInterceptor::class.java) {
            val newToken = accessTokenRepository.getAccessToken()
            //Access token is refreshed in another thread.
            if (newToken != null && token != newToken) {
                return proceed(chain, newToken)
            }

            val updatedAccessToken = accessTokenRepository.refreshAccessToken()
            return if (updatedAccessToken != null) {
                return proceed(chain, updatedAccessToken)
            } else {
                proceed(chain)
            }
        }
    }

    private fun proceed(chain: Interceptor.Chain, accessToken: String? = null): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }

}