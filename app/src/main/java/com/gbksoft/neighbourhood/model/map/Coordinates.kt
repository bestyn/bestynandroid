package com.gbksoft.neighbourhood.model.map

import android.location.Location
import com.google.android.libraries.maps.model.LatLng

data class Coordinates(
    var latitude: Double,
    var longitude: Double
) {
    companion object {
        @JvmStatic
        fun parse(latLng: LatLng?): Coordinates? {
            return latLng?.let {
                Coordinates(it.latitude, it.longitude)
            }
        }

        @JvmStatic
        fun parse(location: Location): Coordinates {
            return Coordinates(location.latitude, location.longitude)
        }
    }

    override fun toString(): String {
        return "$latitude,$longitude"
    }
}