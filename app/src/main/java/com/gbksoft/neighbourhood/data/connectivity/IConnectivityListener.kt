package com.gbksoft.neighbourhood.data.connectivity

interface IConnectivityListener {
    fun onNetworkStateChanged(isOnline: Boolean)
}