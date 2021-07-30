package com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.cluster

import com.gbksoft.neighbourhood.model.map.MyNeighbor
import com.google.android.libraries.maps.model.BitmapDescriptor
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MarkerCluster(
    val neighbor: MyNeighbor,
    var iconDescriptor: BitmapDescriptor
) : ClusterItem {
    private val latLng = LatLng(neighbor.location.latitude, neighbor.location.longitude)

    override fun getSnippet(): String? = null

    override fun getTitle(): String? = null

    override fun getPosition(): LatLng = latLng

}