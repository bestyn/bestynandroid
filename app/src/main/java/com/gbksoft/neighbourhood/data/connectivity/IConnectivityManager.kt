package com.gbksoft.neighbourhood.data.connectivity

import androidx.lifecycle.LifecycleOwner

interface IConnectivityManager {
    fun isOnline(): Boolean
    fun setConnectivityListener(lifecycleOwner: LifecycleOwner, connectivityListener: IConnectivityListener)
}