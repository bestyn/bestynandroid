package com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.databinding.FragmentMapBinding
import com.gbksoft.neighbourhood.model.map.Coordinates
import com.gbksoft.neighbourhood.model.map.MyNeighbor
import com.gbksoft.neighbourhood.model.profile.Avatar
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.cluster.ClusterHelper
import com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.cluster.MarkerCluster
import com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.cluster.MarkerClusterRenderer
import com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.marker.MapMarkerIconFactory
import com.gbksoft.neighbourhood.utils.Constants
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithmAdapter
import com.google.maps.android.collections.MarkerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import java.lang.Math.sqrt
import java.util.concurrent.ExecutionException


class MyNeighborsFragment : SystemBarsColorizeFragment(), OnMapReadyCallback {
    private lateinit var layout: FragmentMapBinding
    private lateinit var viewModel: MyNeighborsViewModel
    private lateinit var mapMarkerIconFactory: MapMarkerIconFactory
    private lateinit var clusterHelper: ClusterHelper

    private var markerAvatarImageSize: Int = 0

    private var currentProfile: CurrentProfile? = null
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<MarkerCluster>? = null
    private var myMarkerManagerCollection: MarkerManager.Collection? = null

    private var mapOverlaysJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Analytics.onOpenedMapView()
        clusterHelper = ClusterHelper(requireContext())
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
            .get(MyNeighborsViewModel::class.java)
        mapMarkerIconFactory = MapMarkerIconFactory(requireContext())
        markerAvatarImageSize = resources.getDimensionPixelSize(R.dimen.marker_avatar_image_size)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)

        resetFields()

        layout.mapView.onCreate(savedInstanceState)
        layout.mapView.getMapAsync(this)

        subscribeToCurrentProfile()

        return layout.root
    }

    override fun setOnApplyWindowInsetsListener(view: View) {

    }

    override fun onStart() {
        super.onStart()
        showNavigateBar()
    }

    override fun onDestroy() {
        if (this::layout.isInitialized) {
            layout.mapView.onDestroy()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        layout.mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onResume() {
        layout.mapView.onResume()
        super.onResume()
        viewModel.checkMyNeighborUpdates()
    }

    override fun onPause() {
        super.onPause()
        layout.mapView.onPause()
    }

    private fun resetFields() {
        currentProfile = null
        googleMap = null
        clusterManager = null
        myMarkerManagerCollection = null
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap == null) return
        layout.mapView.onResume()
        this.googleMap = googleMap
        currentProfile?.let { profile -> setupMap(googleMap, profile) }
    }

    private fun subscribeToCurrentProfile() {
        viewModel.currentProfile.observe(viewLifecycleOwner, Observer {
            checkIfNeedReopen(it)
            currentProfile = it
            googleMap?.let { map -> setupMap(map, it) }
        })
    }

    private fun checkIfNeedReopen(reloadedProfile: CurrentProfile) {
        currentProfile?.let { currentProfile ->
            if (currentProfile.id != reloadedProfile.id) {
                val direction =
                    MyNeighborsFragmentDirections.reopen()
                findNavController().navigate(direction)
            }
        }
    }

    private fun setupMap(googleMap: GoogleMap, currentProfile: CurrentProfile) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_json))
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false
        val loc = currentProfile.location

        centerMap(googleMap, LatLng(loc.latitude, loc.longitude))

        val markerManager = MarkerManager(googleMap)
        clusterManager = ClusterManager<MarkerCluster>(requireContext(), googleMap, markerManager).apply {
            renderer = MarkerClusterRenderer(requireContext(), googleMap, this, mapMarkerIconFactory)
            algorithm = ScreenBasedAlgorithmAdapter(PreCachingAlgorithmDecorator(
                NonHierarchicalDistanceBasedAlgorithm()))
            algorithm.maxDistanceBetweenClusteredItems = 100
            googleMap.setOnCameraIdleListener(this)
            googleMap.setOnMarkerClickListener(this)
            setOnClusterItemClickListener {
                onNeighborMarkerClick(it.neighbor)
                true
            }
        }
        clusterHelper.clusterManager = clusterManager
        myMarkerManagerCollection = markerManager.newCollection().apply {
            setOnMarkerClickListener {
                onMyMarkerClick()
                true
            }
        }

        subscribeToMyNeighbors()
    }

    private fun subscribeToMyNeighbors() {
        viewModel.myNeighbors.observe(viewLifecycleOwner, Observer { fillMap(it) })
    }

    //call when map is ready
    private fun centerMap(googleMap: GoogleMap, center: LatLng) {
        val radius = Constants.MY_NEIGHBORS_RADIUS_IN_METERS * 1.1 //plus 10%
        val bounds = calcNeighborsBounds(center, radius)
        val cameraUpdates = CameraUpdateFactory.newLatLngBounds(bounds, 0)
        googleMap.moveCamera(cameraUpdates)
    }


    private fun calcNeighborsBounds(center: LatLng, radiusInMeters: Double): LatLngBounds {
        val distanceFromCenterToCorner = radiusInMeters * sqrt(2.0)
        val southwestCorner =
            SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northeastCorner =
            SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(southwestCorner, northeastCorner)
    }

    private fun fillMap(myNeighbors: List<MyNeighbor>) {
        val map = googleMap
        val profile = currentProfile
        val markerManagerCollection = myMarkerManagerCollection
        val clusterManager = clusterManager
        if (map == null || profile == null || clusterManager == null || markerManagerCollection == null) return

        val myNeighborsList = myNeighbors.toImmutableList()
        mapOverlaysJob?.cancel()
        mapOverlaysJob = lifecycleScope.launchWhenCreated {
            clusterManager.clearItems()
            markerManagerCollection.clear()
            map.clear()
            async { drawNeighborhoodRadius(map, profile.location, myNeighborsList) }
            addMyMarker(markerManagerCollection, profile)
            addNeighborsMarkers(clusterManager, myNeighborsList)
        }
    }

    private suspend fun drawNeighborhoodRadius(googleMap: GoogleMap,
                                               myLocation: Coordinates,
                                               myNeighbors: List<MyNeighbor>) {

        val circleOptions = withContext(Dispatchers.IO) {
            val center = LatLng(myLocation.latitude, myLocation.longitude)
            val circleOptions = CircleOptions()
            circleOptions.center(center)
            var radius = Constants.MY_NEIGHBORS_RADIUS_IN_METERS
            circleOptions.fillColor(resources.getColor(R.color.map_neighbors_area_color))
            circleOptions.strokeColor(resources.getColor(R.color.transparent))
            circleOptions.strokeWidth(0f)
            for (neighbor in myNeighbors) {
                val latLng = LatLng(neighbor.location.latitude, neighbor.location.longitude)
                val distance = SphericalUtil.computeDistanceBetween(center, latLng)
                if (distance > radius) radius = distance
            }
            circleOptions.radius(radius)
        }
        googleMap.addCircle(circleOptions)
    }

    private suspend fun addMyMarker(markerManagerCollection: MarkerManager.Collection, currentProfile: CurrentProfile) {
        val markerOptions = MarkerOptions()
        val title = currentProfile.title ?: ""
        val latLng = LatLng(currentProfile.location.latitude, currentProfile.location.longitude)
        markerOptions.position(latLng)
        var markerIcon = mapMarkerIconFactory.createMyMarkerIcon(title, null)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
        markerOptions.anchor(0.5f, 0.5f)

        val marker = markerManagerCollection.addMarker(markerOptions)

        if (currentProfile.avatar == null) return

        loadAvatar(currentProfile.avatar.getSmall())?.let {
            markerIcon = mapMarkerIconFactory.createMyMarkerIcon(title, it)
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon))
            marker.setAnchor(0.5f, 0.5f)
        }
    }

    private suspend fun addNeighborsMarkers(
        clusterManager: ClusterManager<MarkerCluster>,
        myNeighbors: List<MyNeighbor>) = withContext(Dispatchers.Main) {

        for (neighbor in myNeighbors) {
            async { addNeighborMarker(clusterManager, neighbor) }
        }
    }

    private suspend fun addNeighborMarker(clusterManager: ClusterManager<MarkerCluster>,
                                          neighbor: MyNeighbor) {
        val title = neighbor.title
        var markerIcon = mapMarkerIconFactory.createNeighborMarkerIcon(neighbor.isBusiness, title, null)
        val markerCluster = MarkerCluster(neighbor, BitmapDescriptorFactory.fromBitmap(markerIcon))
        clusterManager.addItem(markerCluster)
        clusterHelper.recluster()

        if (neighbor.avatarUrl == null) return

        loadAvatar(neighbor.avatarUrl)?.let {
            markerIcon = mapMarkerIconFactory.createNeighborMarkerIcon(neighbor.isBusiness, title, it)
            markerCluster.iconDescriptor = BitmapDescriptorFactory.fromBitmap(markerIcon)
            clusterManager.updateItem(markerCluster)
            clusterHelper.recluster()
        }
    }


    private suspend fun loadAvatar(avatarUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            Glide.with(requireContext())
                .asBitmap()
                .load(avatarUrl)
                .apply(RequestOptions().override(markerAvatarImageSize, markerAvatarImageSize))
                .submit()
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is ExecutionException, is InterruptedException -> null
                else -> throw e
            }
        }
    }

    private fun onMyMarkerClick() {
        currentProfile?.run {
            openMyProfile(isBusiness)
        }
    }

    private fun onNeighborMarkerClick(neighbor: MyNeighbor) {
        val publicProfile = neighbor.run {
            val avatar = if (avatarUrl != null) Avatar(avatarUrl) else null
            PublicProfile(id, isBusiness, avatar, title)
        }

        openStrangerProfile(publicProfile)
    }

    private fun openMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) {
            MyNeighborsFragmentDirections.toMyBusinessProfileFragment()
        } else {
            MyNeighborsFragmentDirections.toMyProfileFragment()
        }
        findNavController().navigate(direction)
    }

    private fun openStrangerProfile(profile: PublicProfile) {
        val direction = if (profile.isBusiness) {
            MyNeighborsFragmentDirections.toPublicBusinessProfileFragment(profile.id)
        } else {
            MyNeighborsFragmentDirections.toPublicProfileFragment(profile.id)
        }
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clusterHelper.release()
    }

}