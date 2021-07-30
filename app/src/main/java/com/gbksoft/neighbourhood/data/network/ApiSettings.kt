package com.gbksoft.neighbourhood.data.network

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.data.connectivity.ConnectivityInterceptor
import com.gbksoft.neighbourhood.data.connectivity.ConnectivityManager
import com.gbksoft.neighbourhood.data.network.logging.MyHttpLoggingInterceptor
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.google.gson.GsonBuilder
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class ApiSettings {
    companion object {
        private var okHttpClient: OkHttpClient? = null
        private var connectivityManager: ConnectivityManager? = null
        private var headersProvider: HeadersProvider? = null

        @JvmStatic
        fun init(context: Context?, connManager: ConnectivityManager?, headersProvider: HeadersProvider?) {
            if (BuildConfig.DEBUG) {
                Stetho.initializeWithDefaults(context)
            }
            Companion.headersProvider = headersProvider
            connectivityManager = connManager
        }

        @JvmStatic
        fun buildRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(String.format("%s%s", BuildConfig.API_BASE_URL, BuildConfig.API_BASE_PATH))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .client(client)
                .build()
        }

        private val client: OkHttpClient
            private get() {
                var client = okHttpClient
                if (client == null) {
                    synchronized(ApiFactory::class.java) {
                        client = okHttpClient
                        if (client == null) {
                            okHttpClient = buildClient()
                            client = okHttpClient
                        }
                    }
                }
                return client!!
            }

        private fun buildClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
            connectivityManager?.let { builder.addInterceptor(ConnectivityInterceptor(it)) }
            val accessTokenRepository = RepositoryProvider.accessTokenRepository
            builder.addInterceptor(AccessTokenInterceptor(accessTokenRepository))
            builder.authenticator(AccessTokenAuthenticator(accessTokenRepository))
            builder.addInterceptor(ProfileIdInterceptor(headersProvider!!))
            builder.addInterceptor(VersionInterceptor(headersProvider!!))
            //Warning!!! When uploading file, this load entire file in memory for logging
            //(may produce OutOfMemoryException)
            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = MyHttpLoggingInterceptor(skipBodySize = 5 * 1024)
                httpLoggingInterceptor.level = MyHttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(httpLoggingInterceptor)
                builder.addNetworkInterceptor(StethoInterceptor())
                builder.addInterceptor(OkHttpProfilerInterceptor())
            }
            if (BuildConfig.DEBUG) {
                builder.addInterceptor(OkHttpProfilerInterceptor())
            }
            return builder.build()
        }
    }
}