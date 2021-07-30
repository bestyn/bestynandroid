package com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.cluster

import android.content.Context
import com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.marker.MapMarkerIconFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.BitmapDescriptor
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MarkerClusterRenderer(context: Context?,
                            map: GoogleMap?,
                            clusterManager: ClusterManager<MarkerCluster>?,
                            private val iconFactory: MapMarkerIconFactory)
    : DefaultClusterRenderer<MarkerCluster>(context, map, clusterManager) {

    init {
        minClusterSize = 2
    }

    override fun onBeforeClusterItemRendered(item: MarkerCluster, markerOptions: MarkerOptions) {
        markerOptions.icon(item.iconDescriptor)
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun getDescriptorForCluster(cluster: Cluster<MarkerCluster>): BitmapDescriptor {
        val size = cluster.size
        return BitmapDescriptorFactory.fromBitmap(iconFactory.createClusterIcon(size))
    }

    override fun onClusterItemUpdated(item: MarkerCluster, marker: Marker) {
        marker.setIcon(item.iconDescriptor)
    }

}