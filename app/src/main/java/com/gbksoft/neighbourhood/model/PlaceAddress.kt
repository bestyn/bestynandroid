package com.gbksoft.neighbourhood.model

import com.google.android.libraries.places.api.model.AddressComponents

data class PlaceAddress(
    val addressPlaceId: String,
    val addressComponents: AddressComponents
)