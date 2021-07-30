package com.gbksoft.neighbourhood.app

import android.annotation.SuppressLint
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Resources
import androidx.multidex.MultiDexApplication
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.connectivity.ConnectivityManager
import com.gbksoft.neighbourhood.data.connectivity.IConnectivityManager
import com.gbksoft.neighbourhood.data.maintenance.MaintenanceManager
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.shared_prefs.SharedStorage
import com.gbksoft.neighbourhood.di.Koin
import com.gbksoft.neighbourhood.domain.utils.safe
import com.gbksoft.neighbourhood.ui.fragments.chat.background.component.ChatBackgroundManager
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorsMessageUtils
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils
import com.google.android.libraries.places.api.Places
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import timber.log.Timber.DebugTree


class NApplication : MultiDexApplication() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var maintenanceManager: MaintenanceManager

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        context = this
        Koin.setup(this)

        registerLocaleReceiver()
        sharedStorage = SharedStorage(this)
        errorsMessageUtils = ErrorsMessageUtils(sharedStorage.getErrorsConfig())
        validationUtils = ValidationUtils(errorsMessageUtils)
        connectivityManager = ConnectivityManager(this)
        maintenanceManager = MaintenanceManager()

        ApiFactory.init(this, connectivityManager, HeadersProviderImpl(context, sharedStorage))

        //places sdk (google map)
        Places.initialize(applicationContext, getString(R.string.google_api_key))

        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())

        subscribeToErrorsConfig()
        GlideSetup.setup(this)
        ChatBackgroundManager.getInstance().checkOrigins()
    }

    @SuppressLint("CheckResult")
    private fun subscribeToErrorsConfig() {
        sharedStorage.subscribeErrorsConfig()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Companion::errorsMessageUtils.safe()?.setErrorsMap(it)
            }
    }

    private fun registerLocaleReceiver() {
        registerReceiver(DateTimeUtils, DateTimeUtils.localeChangedIntentFilter)
    }

    override fun onTerminate() {
        unregisterReceiver(DateTimeUtils)
        super.onTerminate()
    }

    fun getConnectivityManager(): IConnectivityManager {
        return connectivityManager
    }

    fun getMaintenanceManager(): MaintenanceManager {
        return maintenanceManager
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                ChatBackgroundManager.getInstance().cancelAllDownload()
            }
        }
    }

    companion object {
        @JvmStatic
        lateinit var context: Context
            private set

        @JvmStatic
        lateinit var sharedStorage: SharedStorage
            private set

        @JvmStatic
        lateinit var errorsMessageUtils: ErrorsMessageUtils
            private set

        @JvmStatic
        lateinit var validationUtils: ValidationUtils
            private set

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels


    }
}