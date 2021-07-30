package com.gbksoft.neighbourhood.data.repositories

import androidx.annotation.WorkerThread
import com.gbksoft.neighbourhood.data.models.request.user.RefreshTokenReq
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.contract.AuthTokensStorage
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import java.io.IOException

class AccessTokenRepository(
    private val authTokensStorage: AuthTokensStorage
) {
    fun getAccessToken(): String? {
        return authTokensStorage.getAccessToken()
    }

    fun getAccessTokenExpiredTime(): Long {
        return authTokensStorage.getAccessTokenExpiredAt()
    }


    @WorkerThread
    fun refreshAccessToken(): String? {
        val refreshToken = authTokensStorage.getRefreshToken() ?: return null
        val req = RefreshTokenReq(refreshToken)
        val call = ApiFactory.apiUser.refreshToken(req)
        val resp = try {
            val resp = call.execute()
            resp
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        val body = resp.body()
        if (resp.isSuccessful && body != null && body.result != null) {
            saveTokenData(body.result)
            return body.result.accessToken
        }
        return null
    }

    @Synchronized
    fun saveTokenData(tokenModel: TokenModel) {
        val expiredAt = TimestampMapper.toAppTimestamp(tokenModel.expiredAt)
        authTokensStorage.setTokenData(
            tokenModel.accessToken,
            expiredAt,
            tokenModel.refreshToken
        )
    }

    fun deleteTokenData() {
        authTokensStorage.deleteTokenData()
    }

}