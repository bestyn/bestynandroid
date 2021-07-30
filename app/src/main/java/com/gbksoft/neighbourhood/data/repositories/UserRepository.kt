package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.request.user.*
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import com.gbksoft.neighbourhood.data.network.api.ApiGooglePlaces
import com.gbksoft.neighbourhood.data.network.api.ApiUser
import com.gbksoft.neighbourhood.mappers.auth.GeocodeMapper
import com.gbksoft.neighbourhood.model.PlaceAddress
import com.gbksoft.neighbourhood.model.map.Coordinates
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import retrofit2.HttpException

class UserRepository(
    private val apiUser: ApiUser,
    private val apiGooglePlaces: ApiGooglePlaces
) : BaseRepository() {
    fun signInWithEmail(email: String, password: String, deviceId: String?): Observable<TokenModel> {
        val signInReq = SignInReq(email, password, deviceId)
        return apiUser
            .login(signInReq)
            .map { it.result }
    }

    fun signUp(addressPlaceId: String, //From Google Maps
               fullName: String,
               email: String,
               password: String): Maybe<BaseResponse<Any?>> {

        return apiUser
            .signUp(SignUpReq(addressPlaceId, fullName, email, password))
    }

    fun getAddress(coordinates: Coordinates,
                   googleApiKey: String): Observable<PlaceAddress> {
        return Observable.create { emitter ->
            val call = apiGooglePlaces.getAddress(coordinates, googleApiKey)
            val response = call.execute()
            if (response.isSuccessful) {
                val geocodeResponse = response.body()
                if (geocodeResponse?.results?.isNotEmpty() == true) {
                    emitter.onNext(GeocodeMapper.toGeocodeAddress(geocodeResponse.results[0]))
                } else {
                    emitter.onError(IllegalStateException("Empty results for coordinates: $coordinates"))
                }
            } else {
                emitter.onError(HttpException(response))
            }
        }
    }

    fun recoveryPassword(email: String): Maybe<BaseResponse<Any?>> {
        val recoveryPasswordReq = RecoveryPasswordReq(email)
        return apiUser
            .recoveryPassword(recoveryPasswordReq)
    }

    fun setNewPassword(resetToken: String, newPassword: String, confirmNewPassword: String): Maybe<BaseResponse<Any?>> {
        val newPasswordReq = NewPasswordReq(resetToken, newPassword, confirmNewPassword)
        return apiUser
            .setNewPassword(newPasswordReq)
    }

    fun validateResetPasswordToken(resetToken: String): Completable {
        val body = ValidateResetPasswordTokenReq(resetToken)
        return apiUser
            .validateResetPasswordToken(body)
            .ignoreElement()
    }

    fun setFirebaseMessagingToken(token: String): Completable {
        val body = FirebasePushTokenModel(token)
        return apiUser.setFirebasePushToken(body)
    }

    fun logout(): Maybe<BaseResponse<Any?>> {
        return apiUser.logout()
    }
}