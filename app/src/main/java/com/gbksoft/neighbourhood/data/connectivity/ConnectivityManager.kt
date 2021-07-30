package com.gbksoft.neighbourhood.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber

class ConnectivityManager(context: Context) : IConnectivityManager {
    private var isConnectionAvailable = false

    private var wifiConnectionAvailable = false
    private var cellularConnectionAvailable = false

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val isOnlineLiveData = MutableLiveData<Boolean>()

    init {
        checkOnStart()
        registerConnectivityListener()
    }

    override fun isOnline(): Boolean {
        return isConnectionAvailable
    }

    private fun registerConnectivityListener() {
        registerConnectivityListener(NetworkCapabilities.TRANSPORT_WIFI) { wifiConnectionAvailable = it }
        registerConnectivityListener(NetworkCapabilities.TRANSPORT_CELLULAR) { cellularConnectionAvailable = it }
    }

    private fun registerConnectivityListener(transport: Int, transportAvailabilityChanged: (Boolean) -> Unit) {

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(transport)
            .build()

        val networkCallback = object : NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Timber.tag(NETWORK_TAG).d("${transport.name()} is available")
                transportAvailabilityChanged(true)
                checkInternetAvailability()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Timber.tag(NETWORK_TAG).d("${transport.name()} is lost")
                transportAvailabilityChanged(false)
                checkInternetAvailability()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Timber.tag(NETWORK_TAG).d("${transport.name()} is unavailable")
                transportAvailabilityChanged(false)
                checkInternetAvailability()
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }


    private fun checkInternetAvailability() {
        isConnectionAvailable = wifiConnectionAvailable || cellularConnectionAvailable
        onStateChanged()
    }

    private fun checkOnStart() {
        isOnlineLiveData.postValue(checkInternetConnection())
    }

    @Suppress("DEPRECATION")
    private fun checkInternetConnection(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkAvailability =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkAvailability != null &&
                networkAvailability.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkAvailability.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        } else {
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    private fun onStateChanged() {
        isOnlineLiveData.postValue(isConnectionAvailable)
    }

    override fun setConnectivityListener(lifecycleOwner: LifecycleOwner, connectivityListener: IConnectivityListener) {
        addListener(lifecycleOwner, connectivityListener)
    }

    private fun addListener(lifecycleOwner: LifecycleOwner, connectivityListener: IConnectivityListener) {
        isOnlineLiveData.observe(lifecycleOwner, Observer { isOnline: Boolean? -> connectivityListener.onNetworkStateChanged(isOnline!!) })
    }

    private fun Int.name(): String {
        return when (this) {
            NetworkCapabilities.TRANSPORT_WIFI -> "TRANSPORT_WIFI"
            NetworkCapabilities.TRANSPORT_CELLULAR -> "TRANSPORT_CELLULAR"
            else -> "UNDEFINED"
        }
    }

    companion object {
        const val NETWORK_TAG = "NetworkTag"
    }
}