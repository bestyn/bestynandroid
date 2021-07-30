package com.gbksoft.neighbourhood.data.repositories.contract

interface AuthTokensStorage {
    fun setTokenData(token: String, expiredAt: Long, refreshToken: String)

    fun getAccessToken(): String?
    fun getAccessTokenExpiredAt(): Long
    fun getRefreshToken(): String?

    fun deleteTokenData()
}