package com.gbksoft.neighbourhood.data.network

import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.domain.exceptions.VersionIncompatibilityException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class VersionInterceptor(headersProvider: HeadersProvider) : Interceptor {
    private val headerName = "X-Version"
    private val appVersion = headersProvider.getAppVersion()
    private val appMajor = fetchMajor(appVersion)
    private val gson = Gson()
    private val type = object : TypeToken<BaseResponse<Any>>() {}.type

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = addVersionHeader(chain)
        val response = chain.proceed(request)

        check403Error(response)
        checkCompatibility(response)

        return response
    }

    private fun addVersionHeader(chain: Interceptor.Chain): Request {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        requestBuilder.addHeader(headerName, appVersion)
        return requestBuilder.build()
    }

    private fun check403Error(response: Response) {
        if (response.code != 403) return
        val body = response.peekBody(2048).string()
        try {
            val errorResponse: BaseResponse<Any> = gson.fromJson(body, type)
            val errorMessage = errorResponse.message ?: return
            if (errorMessage.startsWith("Please update")) {
                throw VersionIncompatibilityException()
            }
        } catch (e: Exception) {
        }
    }

    private fun checkCompatibility(response: Response) {
        val serverVersion = response.header(headerName, appVersion) ?: return
        val serverMajor = fetchMajor(serverVersion) ?: return
        if (appMajor != serverMajor) {
            throw VersionIncompatibilityException()
        }
    }

    private fun fetchMajor(version: String): String? {
        val dotIndex = version.indexOf(".")
        if (dotIndex <= 0) return null
        return version.substring(0, dotIndex)
    }
}