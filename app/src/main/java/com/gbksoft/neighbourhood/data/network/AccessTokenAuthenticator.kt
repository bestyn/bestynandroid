package com.gbksoft.neighbourhood.data.network

import com.gbksoft.neighbourhood.data.repositories.AccessTokenRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
    private val accessTokenRepository: AccessTokenRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = accessTokenRepository.getAccessToken()
        if (!isRequestWithAccessToken(response) || accessToken == null) {
            return null
        }
        synchronized(AccessTokenAuthenticator::class.java) {
            val newAccessToken = accessTokenRepository.getAccessToken()
            //Access token is refreshed in another thread.
            if (newAccessToken != null && accessToken != newAccessToken) {
                return newRequestWithAccessToken(response.request, newAccessToken)
            }

            // Need to refresh an access token
            val updatedAccessToken = accessTokenRepository.refreshAccessToken()
            return if (updatedAccessToken != null) {
                newRequestWithAccessToken(response.request, updatedAccessToken)
            } else {
                null
            }
        }
    }

    private fun isRequestWithAccessToken(response: Response): Boolean {
        val header = response.request.header("Authorization")
        return header != null && header.startsWith("Bearer")
    }

    private fun newRequestWithAccessToken(request: Request, accessToken: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }
}