package com.gbksoft.neighbourhood.data.shared_prefs

import android.content.Context
import android.text.TextUtils
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.gbksoft.neighbourhood.data.models.response.ConfigModel
import com.gbksoft.neighbourhood.data.models.response.VersionModel
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.repositories.contract.AuthTokensStorage
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.AppType
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.profile.BasicProfile
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.MyProfile
import com.gbksoft.neighbourhood.ui.notifications.FirebaseMessagingTokenStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class SharedStorage(context: Context) : AuthTokensStorage, FirebaseMessagingTokenStorage {
    private val SHARED_PREFERENCES_NAME = "neighbourhood"

    //=== Config
    private val PREF_API_VERSION = "api_version"
    private val PREF_PARAMETERS = "parameters"
    private val PREF_ERRORS = "errors"

    //=== Token
    private val PREF_TOKEN = "token"
    private val PREF_EXPIRED_AT = "expired_at"
    private val PREF_REFRESH_TOKEN = "refresh_token"

    //=== User
    private val PREF_USER = "user"
    private val PREF_NEW_EMAIL = "new_email"
    private val CURRENT_PROFILE_JSON = "current_profile_id"
    private val PREF_HAS_NEW_CHAT_MESSAGES = "has_new_chat_messages"
    private val PREF_NEED_SELECT_INTEREST_AFTER_LOGIN = "need_select_interest_after_login"

    //=== Chat
    private val PREF_CHAT_BACKGROUND_POSITION = "chat_background_position"

    //=== Search
    private val PREF_RECENT_SEARCHES = "recent_searches"

    //=== Search
    private val PREF_FIREBASE_PUSH_TOKEN = "firebase_push_token"
    private val PREF_UPLOADED_FIREBASE_PUSH_TOKEN = "uploaded_firebase_push_token"

    //=== App
    private val PREF_CURRENT_APP_TYPE = "current_app_type"

    //=== Story
    private val PREF_IS_FIRST_STORY_CREATING = "is_first_story_creating"

    //=== Story for unAuthorized users
    private val PREF_UNATHORIZED_STORY_ID = "unauthorized_story_id"

    private val PREF_CHRONOMETER_BASE = "chronometer_base"

    //ADD YOUR KEY TO LIST
    private val arrOfKeysToRemoveOnSingOut = arrayOf(
            //            PREF_API_VERSION,
            //            PREF_PARAMETERS,
            //            PREF_ERRORS,    //excluding information from config request
            PREF_HAS_NEW_CHAT_MESSAGES,
            PREF_USER,
            PREF_TOKEN,
            PREF_EXPIRED_AT,
            CURRENT_PROFILE_JSON,
            PREF_NEED_SELECT_INTEREST_AFTER_LOGIN,
            PREF_RECENT_SEARCHES,
            PREF_UPLOADED_FIREBASE_PUSH_TOKEN,
            PREF_CURRENT_APP_TYPE
    )

    private val context: WeakReference<Context> = WeakReference(context)
    private val gson: Gson = Gson()
    private val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private var cachedCurrentProfile: CurrentProfile? = null
    private val rxPrefs = RxSharedPreferences.create(prefs)

    fun signOut(): Boolean {
        val editor = prefs.edit()
        for (s in arrOfKeysToRemoveOnSingOut) {
            editor.remove(s)
        }
        clearCachedFields()
        return editor.commit()
    }

    private fun clearCachedFields() {
        cachedCurrentProfile = null
    }

    //===== Config =================================================================================
    fun saveApiVersion(version: VersionModel?): Boolean {
        val json = gson.toJson(version)
        return prefs.edit().putString(PREF_API_VERSION, json).commit()
    }

    fun saveParametersConfig(parameters: Map<String, Any>): Boolean {
        val json = gson.toJson(parameters)
        return prefs.edit().putString(PREF_PARAMETERS, json).commit()
    }

    private var lastErrorsConfigJson: String? = null
    fun saveErrorsConfig(errors: Map<String, String>): Boolean {
        val json = gson.toJson(errors)
        if (lastErrorsConfigJson == json) return false

        lastErrorsConfigJson = json
        return prefs.edit().putString(PREF_ERRORS, json).commit()
    }

    // load ErrorsConfig from asset file if ErrorsConfig in SP is empty
    fun getErrorsConfig(): Map<String, String> {
        var json = prefs.getString(PREF_ERRORS, "")
        lastErrorsConfigJson = json

        // load ErrorsConfig from asset file if ErrorsConfig in SP is empty
        return if (json?.isNotEmpty() == true) {
            toErrorsConfigMap(json)
        } else {
            val sb = StringBuilder()
            try {
                val `is` = context.get()!!.assets.open("config.json")
                val br = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
                var str: String?
                while (br.readLine().also { str = it } != null) {
                    sb.append(str)
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            json = sb.toString()
            if (TextUtils.isEmpty(json)) return HashMap()
            val configModel = gson.fromJson(json, ConfigModel::class.java)
            configModel.errors
        }
    }

    fun subscribeErrorsConfig(): Observable<Map<String, String>> {
        return rxPrefs.getString(PREF_ERRORS, "")
                .asObservable()
                .filter { it.isNotEmpty() }
                .map { json -> toErrorsConfigMap(json) }
    }

    private fun toErrorsConfigMap(json: String): Map<String, String> {
        val typeToken = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson<Map<String, String>>(json, typeToken)
    }

    //start-block {Token} ==========================================================================
    private fun getExpiredTokenAt(): Long {
        return prefs.getLong(PREF_EXPIRED_AT, 0)
    }

    fun isTokenExpired(): Boolean {
        val timeInMillis = Calendar.getInstance(TimeZone.getDefault()).timeInMillis
        val nowDateTime = TimestampMapper.toServerTimestamp(timeInMillis)
        return nowDateTime >= getExpiredTokenAt()
    }

    fun isTokenAlive(): Boolean {
        return isTokenExpired().not()
    }

    override fun setTokenData(token: String, expiredAt: Long, refreshToken: String) {
        prefs.edit()
                .putString(PREF_TOKEN, token)
                .putLong(PREF_EXPIRED_AT, expiredAt)
                .putString(PREF_REFRESH_TOKEN, refreshToken)
                .apply()
    }

    override fun getAccessToken(): String? = prefs.getString(PREF_TOKEN, null)

    override fun getAccessTokenExpiredAt(): Long = prefs.getLong(PREF_EXPIRED_AT, 0)

    override fun getRefreshToken(): String? = prefs.getString(PREF_REFRESH_TOKEN, null)

    override fun deleteTokenData() {
        prefs.edit()
                .remove(PREF_TOKEN)
                .remove(PREF_REFRESH_TOKEN)
                .remove(PREF_EXPIRED_AT)
                .apply()
    }

    //end-block {Token} ============================================================================

    //start-block {Firebase Push Token} ============================================================
    override fun getFirebaseMessagingToken(): String? {
        return prefs.getString(PREF_FIREBASE_PUSH_TOKEN, null)
    }

    override fun getUploadedFirebaseMessagingToken(): String? {
        return prefs.getString(PREF_UPLOADED_FIREBASE_PUSH_TOKEN, null)
    }

    override fun setFirebaseMessagingToken(token: String) {
        prefs.edit().putString(PREF_FIREBASE_PUSH_TOKEN, token).apply()
    }

    override fun setUploadedFirebaseMessagingToken(token: String) {
        prefs.edit().putString(PREF_UPLOADED_FIREBASE_PUSH_TOKEN, token).apply()
    }
    //end-block {Firebase Push Token} ==============================================================

    //start-block {User} ===========================================================================
    fun saveUser(userModel: UserModel?): Boolean {
        val json = gson.toJson(userModel)
        return prefs.edit().putString(PREF_USER, json).commit()
    }

    fun getUserModel(): UserModel? {
        val json = prefs.getString(PREF_USER, null) ?: return null
        return gson.fromJson(json, UserModel::class.java)
    }

    fun saveNewEmailForConfirm(email: String?) {
        prefs.edit().putString(PREF_NEW_EMAIL, email).apply()
    }

    fun getNewEmailForConfirm() = prefs.getString(PREF_NEW_EMAIL, null)

    fun getCurrentProfile(): CurrentProfile? {
        return cachedCurrentProfile ?: run {
            val json = prefs.getString(CURRENT_PROFILE_JSON, "")
            Timber.tag("SwitchTag").d("getCurrentProfile: $json")
            if (!TextUtils.isEmpty(json)) {
                val model = getCurrentProfileModel(json)
                cachedCurrentProfile = ProfileMapper.toCurrentProfile(model)
            }
            cachedCurrentProfile
        }
    }

    fun requireCurrentProfile() = getCurrentProfile()!!

    fun setCurrentProfile(currentProfile: CurrentProfile?): Boolean {
        cachedCurrentProfile = currentProfile
        return saveCurrentProfileModel(ProfileMapper.toCurrentProfileModel(currentProfile!!))
    }

    fun setCurrentProfile(basicProfile: BasicProfile?): Boolean {
        val currentProfile = ProfileMapper.toCurrentProfile(basicProfile!!)
        cachedCurrentProfile = currentProfile
        return saveCurrentProfileModel(ProfileMapper.toCurrentProfileModel(currentProfile))
    }

    fun setCurrentProfile(myProfile: MyProfile?): Boolean {
        cachedCurrentProfile = ProfileMapper.toCurrentProfile(myProfile!!)
        return saveCurrentProfileModel(ProfileMapper.toCurrentProfileModel(cachedCurrentProfile!!))
    }

    fun setCurrentProfile(businessProfile: BusinessProfile?): Boolean {
        cachedCurrentProfile = ProfileMapper.toCurrentProfile(businessProfile!!)
        return saveCurrentProfileModel(ProfileMapper.toCurrentProfileModel(cachedCurrentProfile!!))
    }

    private fun saveCurrentProfileModel(model: CurrentProfileModel?): Boolean {
        return prefs.edit().putString(CURRENT_PROFILE_JSON, gson.toJson(model)).commit()
    }

    fun subscribeCurrentProfile(): Observable<CurrentProfile> {
        return rxPrefs.getString(CURRENT_PROFILE_JSON, "")
                .asObservable()
                .filter { !TextUtils.isEmpty(it) }
                .map { json ->
                    val model = getCurrentProfileModel(json)
                    ProfileMapper.toCurrentProfile(model)
                }
    }

    private fun getCurrentProfileModel(json: String?): CurrentProfileModel {
        val model = gson.fromJson(json, CurrentProfileModel::class.java)
        if (model.avatarUrl != null) { //clear deprecated field
            model.avatarUrl = null
            saveCurrentProfileModel(model)
        }
        return model
    }

    fun setNeedSelectInterestsAfterLogin(needSelect: Boolean) {
        prefs.edit().putBoolean(PREF_NEED_SELECT_INTEREST_AFTER_LOGIN, needSelect).apply()
    }

    fun needSelectInterestsAfterLogin(): Boolean {
        return prefs.getBoolean(PREF_NEED_SELECT_INTEREST_AFTER_LOGIN, false)
    }

    //end-block {User} =============================================================================
    fun setChatBackgroundPosition(position: Int) {
        prefs.edit().putInt(PREF_CHAT_BACKGROUND_POSITION, position).apply()
    }

    fun getChatBackgroundPosition(defaultPosition: Int): Int {
        return prefs.getInt(PREF_CHAT_BACKGROUND_POSITION, defaultPosition)
    }

    fun setResendEmailVerificationLastTime(email: String, currentTimeMillis: Long) {
        prefs.edit().putLong(md5(email), currentTimeMillis).apply()
    }

    fun getResendEmailVerificationLastTime(email: String): Long {

        return prefs.getLong(md5(email), 0)
    }

    private fun md5(s: String): String? {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return s
    }

    fun saveRecentSearches(recentSearches: List<String>) {
        val json = gson.toJson(recentSearches)
        prefs.edit().putString(PREF_RECENT_SEARCHES, json).apply()
    }

    private val stringTypeToken by lazy { object : TypeToken<List<String>>() {}.type }
    fun getRecentSearches(): List<String> {
        val json = prefs.getString(PREF_RECENT_SEARCHES, "[]")
        return gson.fromJson(json, stringTypeToken)
    }

    fun saveCurrentAppType(appType: AppType) {
        prefs.edit().putString(PREF_CURRENT_APP_TYPE, appType.toString()).apply()
    }

    fun getCurrentAppType(): AppType {
        val type = prefs.getString(PREF_CURRENT_APP_TYPE, null)
        return AppType.fromString(type)
    }

    fun isFirstStoryCreating(): Boolean {
        return prefs.getBoolean(PREF_IS_FIRST_STORY_CREATING, true)
    }

    fun setFirstStoryCreating(value: Boolean) {
        prefs.edit().putBoolean(PREF_IS_FIRST_STORY_CREATING, value).apply()
    }

    fun getUnAuthorizedStoryId(): Int {
        return prefs.getInt(PREF_UNATHORIZED_STORY_ID, -1)
    }

    fun setUnAuthorizedStoryId(value: Int) {
        prefs.edit().putInt(PREF_UNATHORIZED_STORY_ID, value).commit()
    }

    fun getChronometerBase(): Long {
        return prefs.getLong(PREF_CHRONOMETER_BASE, -1)
    }

    fun setChronometerBase(value: Long) {
        prefs.edit().putLong(PREF_CHRONOMETER_BASE, value).commit()
    }

}