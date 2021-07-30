package com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.cluster

import android.content.Context
import android.os.Handler
import com.google.maps.android.clustering.ClusterManager
import timber.log.Timber

class ClusterHelper(context: Context) {
    var clusterManager: ClusterManager<MarkerCluster>? = null
    private val handler = Handler(context.mainLooper)
    private val reclusteringDelay = 500L
    private var runnable = Runnable {
        Timber.tag("MapTag2").d("reclustering: ${clusterManager}")
        clusterManager?.cluster()
    }

    fun recluster() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, reclusteringDelay)
    }

    fun release() {
        Timber.tag("MapTag2").d("release")
        handler.removeCallbacks(runnable)
        clusterManager = null
    }
}