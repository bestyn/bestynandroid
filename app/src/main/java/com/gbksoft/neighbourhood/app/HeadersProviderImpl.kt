package com.gbksoft.neighbourhood.app

import android.content.Context
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.data.network.HeadersProvider
import com.gbksoft.neighbourhood.data.shared_prefs.SharedStorage

class HeadersProviderImpl(
    private var context: Context,
    private var sharedStorage: SharedStorage
) : HeadersProvider {
    override fun getCurrentProfileId(): Long? = sharedStorage.getCurrentProfile()?.id
    override fun getAppVersion(): String {
        val packageInfo = context.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
        return packageInfo.versionName
    }
}