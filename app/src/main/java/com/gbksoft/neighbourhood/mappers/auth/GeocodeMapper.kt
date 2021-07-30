package com.gbksoft.neighbourhood.mappers.auth

import com.gbksoft.neighbourhood.data.models.response.geocode.GeocodeResult
import com.gbksoft.neighbourhood.model.PlaceAddress
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.AddressComponents

object GeocodeMapper {

    fun toGeocodeAddress(geocodeResult: GeocodeResult): PlaceAddress {
        val components = mutableListOf<AddressComponent>()
        for (geoComponent in geocodeResult.addressComponents) {
            components.add(AddressComponent.builder(geoComponent.longName, geoComponent.types)
                .setShortName(geoComponent.shortName)
                .build())
        }
        val placeId = geocodeResult.placeId
        val addressComponents = AddressComponents.newInstance(components)

        return PlaceAddress(placeId, addressComponents)
    }

}