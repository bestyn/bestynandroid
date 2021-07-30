package com.gbksoft.neighbourhood.data.network.api

import com.gbksoft.neighbourhood.data.models.request.user.*
import com.gbksoft.neighbourhood.data.models.response.base.BaseResponse
import com.gbksoft.neighbourhood.data.models.response.email.TokenModel
import com.gbksoft.neighbourhood.data.models.response.user.BusinessProfileModel
import com.gbksoft.neighbourhood.data.models.response.user.ProfileModel
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

interface ApiUser {
    @POST(UrlsWithoutToken.LOGIN)
    fun login(@Body signInRequestBody: SignInReq): Observable<BaseResponse<TokenModel>>

    @POST(UrlsWithoutToken.REFRESH_TOKEN)
    fun refreshToken(@Body refreshTokenReq: RefreshTokenReq): Call<BaseResponse<TokenModel>>

    @POST(UrlsWithoutToken.SIGN_UP)
    fun signUp(@Body signUpRequestBody: SignUpReq): Maybe<BaseResponse<Any?>>

    @POST(UrlsWithoutToken.RECOVERY_PASSWORD)
    fun recoveryPassword(@Body recoveryPasswordRequestBody: RecoveryPasswordReq): Maybe<BaseResponse<Any?>>

    @POST(UrlsWithoutToken.NEW_PASSWORD)
    fun setNewPassword(@Body newPasswordRequestBody: NewPasswordReq): Maybe<BaseResponse<Any?>>

    @POST(UrlsWithoutToken.VALIDATE_RESET_PASSWORD_TOKEN)
    fun validateResetPasswordToken(@Body body: ValidateResetPasswordTokenReq): Maybe<BaseResponse<Any?>>

    @PATCH("v1/user/change-password")
    fun changePassword(@Body changePasswordReq: ChangePasswordReq): Maybe<BaseResponse<Any?>>

    @POST("v1/user/logout")
    fun logout(): Maybe<BaseResponse<Any?>>

    @GET("v1/user/current")
    fun getCurrentUser(@Query("expand") expand: String): Observable<BaseResponse<UserModel>>

    @Multipart
    @PATCH("v1/user/profile")
    fun updateProfile(@PartMap updateProfileReq: UpdateProfileReq,
                      @Query("expand") expand: String): Observable<BaseResponse<ProfileModel>>

    @GET("v1/user/profile/{profileId}")
    fun getPublicProfile(@Path("profileId") profileId: Long,
                         @Query("expand") expand: String): Observable<BaseResponse<ProfileModel>>

    @Multipart
    @POST("v1/user/business-profile")
    fun createBusinessProfile(@PartMap req: CreateBusinessProfileReq,
                              @Query("expand") expand: String): Observable<BaseResponse<BusinessProfileModel>>

    @Multipart
    @PATCH("v1/user/business-profile/{profileId}")
    fun updateBusinessProfile(@Path("profileId") profileId: Long,
                              @PartMap req: UpdateBusinessProfileReq,
                              @Query("expand") expand: String): Observable<BaseResponse<BusinessProfileModel>>

    @GET("v1/user/business-profile/{profileId}")
    fun getPublicBusinessProfile(@Path("profileId") profileId: Long,
                                 @Query("expand") expand: String): Observable<BaseResponse<BusinessProfileModel>>

    @POST("v1/user/push-token")
    fun setFirebasePushToken(@Body model: FirebasePushTokenModel): Completable


}